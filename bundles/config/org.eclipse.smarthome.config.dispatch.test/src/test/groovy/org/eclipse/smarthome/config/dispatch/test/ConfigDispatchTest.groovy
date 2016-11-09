/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.dispatch.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.eclipse.smarthome.config.core.ConfigConstants
import org.eclipse.smarthome.config.dispatch.internal.ConfigDispatcher
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.osgi.service.cm.Configuration
import org.osgi.service.cm.ConfigurationAdmin
import org.osgi.service.component.ComponentContext

/**
 *
 * Tests for {@link ConfigDispatcher}
 *
 * @author Petar Valchev
 *
 */
class ConfigDispatchTest extends OSGiTest {
    private ConfigurationAdmin configAdmin
    // The ComponentContext is static as we expect that it is the same for all tests.
    private static ComponentContext context

    private def final CONFIGURATION_WATCH_DIRECTORY = "configurations"
    private def final CONFIG_DISPATCHER_COMPONENT_ID = "org.eclipse.smarthome.config.dispatch"

    private def static defaultConfigDir
    private def static defaultServiceDir
    private def static defaultConfigFile

    private Configuration configuration

    protected void activate(ComponentContext componentContext){
        /* 
         * The files in the root configuration directory are read only on the 
         * activate() method of the ConfigDispatcher. So before every test method 
         * the component is disabled using the component context, the watched 
         * directories are set and then the component is enabled again using the 
         * component context. In that way we can be sure that the files in the 
         * root directory will be read.
         */
        context = componentContext
    }

    @BeforeClass
    public static void setUpClass(){
        // Store the default values in order to restore them after all the tests are finished.
        defaultConfigDir = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT)
        defaultServiceDir = System.getProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT)
        defaultConfigFile = System.getProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT)
    }

    @Before
    public void setUp(){

        /* 
         * Disable the component of the ConfigDispatcher,so we can 
         * set the properties for the directories to be watched,
         * before the ConfigDispatcher is started.
         */
        context.disableComponent(CONFIG_DISPATCHER_COMPONENT_ID)

        /* 
         * After we disable the component we have to wait for the
         * AbstractWatchService to stop watching the previously set 
         * directories, because we will be setting a new ones.
         */
        sleep(300)

        configAdmin = getService(ConfigurationAdmin)
        assertThat "ConfigurationAdmin service cannot be found", configAdmin, is(notNullValue())
    }

    @After
    public void tearDown(){
        // Clear the configuration with the current pid from the persistent store.
        if(configuration != null){
            configuration.delete()
        }
    }

    @AfterClass
    public static void tearDownClass(){
        // Set the system properties to their initial values.
        setSystemProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, defaultConfigDir)
        setSystemProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT, defaultServiceDir)
        setSystemProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT, defaultConfigFile)
    }

    @Test
    public void 'all configuration files with local PIDs are processed and configuration is updated'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_conf"
        def servicesDirectory = "local_pid_services"
        def defaultConfigFileName = "$configDirectory/local.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFileName)

        // Assert that a file with local pid from the root configuration directory is processed.
        verifyValueOfConfigurationProperty("local.default.pid", "default.property", "default.value")

        // Assert that a file with local pid from the services directory is processed.
        verifyValueOfConfigurationProperty("local.service.first.pid", "service.property", "service.value")

        // Assert that more than one file with local pid from the services directory is processed.
        verifyValueOfConfigurationProperty("local.service.second.pid", "service.property", "service.value")
    }

    @Test
    public void 'when the configuration file name contains dot and no PID is defined, the file name becomes PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/no_pid_conf"
        def servicesDirectory = "no_pid_services"
        // In this test case we need configuration files, which names contain dots.
        def defaultConfigFilePath = "$configDirectory/no.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* Assert that the configuration is updated with the name of the file in the root directory as a pid.*/
        verifyValueOfConfigurationProperty("no.pid.default.file", "default.property", "default.value")

        /* Assert that the configuration is updated with the name of the file in the services directory as a pid.*/
        verifyValueOfConfigurationProperty("no.pid.service.file", "service.property", "service.value")
    }

    @Test
    public void 'if no PID is defined in configuration file with no-dot name, the default namespace plus the file name becomes PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/no_pid_no_dot_conf"
        def servicesDirectory = "no_pid_no_dot_services"
        // In this test case we need configuration files, which names don't contain dots.
        def defaultConfigFilePath = "$configDirectory/default.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* 
         * Assert that the configuration is updated with the default
         * namespace the no-dot name of the file in the root directory as pid.
         */
        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.default",
                "no.dot.default.property",
                "no.dot.default.value")

        /* 
         * Assert that the configuration is updated with the default namespace
         * the no-dot name of the file in the services directory as pid.
         */
        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.service",
                "no.dot.service.property",
                "no.dot.service.value")
    }

    @Test
    public void 'when local PID does not contain dot, the default namespace plus that PID is the overall PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_no_dot_conf"
        def servicesDirectory = "local_pid_no_dot_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.no.dot.default.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* 
         * Assert that the configuration is updated with the default namespace
         * the no-dot pid from the file in the the root directory.
         */
        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.default",
                "default.property",
                "default.value")

        /*
         * Assert that the configuration is updated with the default namespace
         * the no-dot pid from the file in the the services directory.
         */
        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.service",
                "service.property",
                "service.value")
    }

    @Test
    public void 'when local PID is an empty string, the default namespace plus dot is the overall PID'(){

        // This test case is a corner case for the above test.
        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_empty_conf"
        def servicesDirectory = "local_pid_empty_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.empty.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.",
                "default.property",
                "default.value")

        verifyValueOfConfigurationProperty("${ConfigDispatcher.SERVICE_PID_NAMESPACE}.",
                "service.property",
                "service.value")
    }

    @Test
    public void 'value is still valid if it is left empty'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_no_value_conf"
        def servicesDirectory = "local_pid_no_value_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.no.value.default.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* 
         * Assert that the configuration is updated with
         * an empty value for a file in the root directory.
         */
        verifyValueOfConfigurationProperty("default.pid", "default.property", "")

        /*
         * Assert that the configuration is updated with
         * an empty value for a file in the services directory.
         */
        verifyValueOfConfigurationProperty("service.pid", "service.property", "")
    }

    @Test
    public void 'if property is left empty, a configuration with the given PID will not exist'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_no_property_conf"
        def servicesDirectory = "local_pid_no_property_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.no.property.default.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that the configuration is not updated with an
         * empty-string property from the file in the root directory.
         * */
        verifyNoPropertiesForConfiguration("no.property.default.pid")

        /* 
         * Assert that the configuration is not updated with an
         * empty-string property from the file in the services directory.
         */
        verifyNoPropertiesForConfiguration("no.property.service.pid")
    }

    @Test
    public void 'properties for local PID can be overridden'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_conflict_conf"
        def servicesDirectory = "local_pid_conflict_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.conflict.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* 
         * Assert that the second processed property from the
         * file in the root directory overrides the first one.
         */
        verifyValueOfConfigurationProperty("same.default.local.pid", "default.property", "default.value2")

        /*
         * Assert that the second processed property from the file
         * in the services directory overrides the first one.
         */
        verifyValueOfConfigurationProperty("same.service.local.pid", "service.property", "service.value2")
    }

    @Test
    public void 'comment lines are not processed'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/comments_conf"
        def servicesDirectory = "comments_services"
        def defaultConfigFilePath = "$configDirectory/comments.default.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that the configuration is not updated with a
         * pid from a comment from a file in the root directory.
         */
        verifyNoPropertiesForConfiguration("comment.default.pid")

        /*
         * Assert that the configuration is not updated with a
         * pid from a comment from a file in the services directory.
         */
        verifyNoPropertiesForConfiguration("comment.service.pid")
    }

    @Test
    public void 'txt files are not processed'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/txt_conf"
        def servicesDirectory = "txt_services"
        def defaultConfigFilePath = "$configDirectory/txt.default.txt"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that the configuration is not updated with
         * pid from a txt file in the root directory.
         */
        verifyNoPropertiesForConfiguration("txt.default.pid")

        /*
         * Assert that the configuration is not updated with
         *  pid from a txt file in the services directory.
         */
        verifyNoPropertiesForConfiguration("txt.service.pid")
    }

    @Test
    public void 'all configuration files with global PIDs are processed and configuration is updated'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_pid_conf"
        def servicesDirectory = "global_pid_services"
        def defaultConfigFilePath = "$configDirectory/global.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        // Assert that a file with global pid from the root configuration directory is processed.
        verifyValueOfConfigurationProperty("global.default.pid", "default.property", "default.value")

        // Assert that a file with global pid from the services directory is processed.
        verifyValueOfConfigurationProperty("global.service.first.pid", "service.property", "service.value")

        // Assert that more than one file with global pid from the services directory is processed.
        verifyValueOfConfigurationProperty("global.service.second.pid", "service.property", "service.value")
    }

    @Test
    public void 'if the property=value pair is prefixed with local PID in the same file, the global PID is ignored'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/ignored_global_pid_conf"
        def servicesDirectory = "ignored_global_pid_services"
        def defaultConfigFilePath = "$configDirectory/ignored.global.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        // Assert that the configuration is not updated with the global pid in the root directory.
        verifyNoPropertiesForConfiguration("ignored.global.default.pid")

        // Assert that the configuration is updated with the local pid in the root directory.
        verifyValueOfConfigurationProperty("not.ignored.local.default.pid", "local.default.property", "local.default.value")

        // Assert that the configuration is not updated with the global pid in the services directory.
        verifyNoPropertiesForConfiguration("ignored.global.service.pid")

        // Assert that the configuration is updated with the local pid in the services directory.
        verifyValueOfConfigurationProperty("not.ignored.local.service.pid", "local.service.property", "local.service.value")
    }

    @Test
    public void 'if the property is not prefixed with local PID, the last PID becomes PID for that property'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/last_pid_conf"
        def servicesDirectory = "last_pid_services"
        def defaultConfigFilePath = "$configDirectory/last.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that a property=value pair is associated with the global pid,
         * defined in the above line in a file in the root directory
         * rather than with the local pid, defined in the below line.
         */
        verifyValueOfConfigurationProperty("first.global.default.pid", "global.default.property1", "global.default.value1")
        verifyNotExistingConfigurationProperty("last.local.default.pid", "global.default.property1")

        /*
         * Assert that a property=value pair is associated with the last defined local pid,
         * rather than with the global pid, defined in the first line of a file in the root directory.
         */
        verifyValueOfConfigurationProperty("last.local.default.pid", "global.default.property2", "global.default.value2")
        verifyNotExistingConfigurationProperty("first.global.default.pid", "global.default.property2")

        /*
         * Assert that a property=value pair is associated with the global pid,
         * defined in the above line in a file in the services directory
         * rather than with the local pid, defined in the below line.
         */
        verifyValueOfConfigurationProperty("first.global.service.pid", "global.service.property1", "global.service.value1")
        verifyNotExistingConfigurationProperty("last.local.default.pid", "global.service.property1")

        /*
         * Assert that a property=value pair is associated with the last defined local pid,
         * rather than with the global pid, defined in the first line of a file in the services directory.
         */
        verifyValueOfConfigurationProperty("last.local.service.pid", "global.service.property2", "global.service.value2")
        verifyNotExistingConfigurationProperty("first.global.service.pid", "global.service.property2")
    }

    @Test
    public void 'if there is no property=value pair for global PID, a configuration with the given PID will not exist'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_pid_no_pair_conf"
        def servicesDirectory = "global_pid_no_pair_services"
        def defaultConfigFilePath = "$configDirectory/global.pid.no.pair.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /* 
         * Assert that pid with no property=value pair
         * is not processed in a file in the root directory.
         */
        verifyNoPropertiesForConfiguration("no.pair.default.pid")

        /*
         * Assert that pid with no property=value pair
         * is not processed in a file in the services directory.
         */
        verifyNoPropertiesForConfiguration("no.pair.service.pid")
    }

    @Test
    public void 'when global PID is empty string, it remains an empty string'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_pid_empty_conf"
        def servicesDirectory = "global_pid_empty_services"
        def defaultConfigFilePath = "$configDirectory/global.pid.empty.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        // Assert that an empty-string pid from a file in the root directory is processed.
        verifyValueOfConfigurationProperty("", "global.default.property", "global.default.value")

        // Assert that an empty-string pid from a file in the services directory is processed.
        verifyValueOfConfigurationProperty("", "global.service.property", "global.service.value")
    }

    @Test
    public void 'property=value pairs for a local PID in different files, are still associated with that PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_different_files_no_conflict_conf"
        def servicesDirectory = "local_pid_different_files_no_conflict_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that the configuration is updated with all the property=value
         * pairs for the same local pid from all the processed files.
         */
        verifyValueOfConfigurationProperty("local.pid", "first.property", "first.value")
        verifyValueOfConfigurationProperty("local.pid", "second.property", "second.value")
        verifyValueOfConfigurationProperty("local.pid", "third.property", "third.value")
        verifyValueOfConfigurationProperty("local.pid", "fourth.property", "fourth.value")
    }

    @Test
    public void 'properties for the same local PID in different files must be overridden in the order they were last modified'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/local_pid_different_files_conflict_conf"
        def servicesDirectory = "local_pid_different_files_conflict_services"
        def defaultConfigFilePath = "$configDirectory/local.pid.default.file.cfg"
        def lastModifiedFileName = "last.modified.service.file.cfg"

        /* 
         * Every file from servicesDirectory contains this property, but with different value.
         * The value for this property in the last processed file must override the previous
         * values for the same property in the configuration.
         */
        def conflictProperty = "property"

        File fileToModify = new File("$configDirectory/$servicesDirectory/$lastModifiedFileName")

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        modifyFile(fileToModify)

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        String value = getLastModifiedValueForPoperty("$configDirectory/$servicesDirectory", conflictProperty)

        /*
         * Assert that the property for the same local pid in the last modified file
         * has overridden the other properties for that pid from previously modified files.
         */
        verifyValueOfConfigurationProperty("local.conflict.pid", conflictProperty, value)
    }

    @Test
    public void 'when property=value pairs for a global PID are in different files, they are still associated with that PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_pid_different_files_no_conflict_conf"
        def servicesDirectory = "global_pid_different_files_no_conflict_services"
        def defaultConfigFilePath = "$configDirectory/global.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that the configuration is updated with all the property=value
         * pairs for the same global pid from all the processed files.
         */
        verifyValueOfConfigurationProperty("different.files.global.pid", "first.property", "first.value")
        verifyValueOfConfigurationProperty("different.files.global.pid", "second.property", "second.value")
        verifyValueOfConfigurationProperty("different.files.global.pid", "third.property", "third.value")
        verifyValueOfConfigurationProperty("different.files.global.pid", "fourth.property", "fourth.value")
    }

    @Test
    public void 'properties for the same global PID in different files must be overridden in the order they were last modified'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_pid_different_files_conflict_conf"
        def servicesDirectory = "global_pid_different_files_conflict_services"
        def defaultConfigFilePath = "$configDirectory/global.pid.default.file.cfg"
        def lastModifiedFileName = "global.pid.last.modified.service.file.cfg"

        /* 
         * Every file from servicesDirectory contains this property, but with different value.
         * The value for this property in the last processed file will override the previous
         * values for the same property in the configuration.
         */
        def conflictProperty = "property"

        File fileToModify = new File("$configDirectory/$servicesDirectory/$lastModifiedFileName")

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        modifyFile(fileToModify)

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        String value = getLastModifiedValueForPoperty("$configDirectory/$servicesDirectory", conflictProperty)

        /*
         * Assert that the property for the same global pid in the last modified file 
         * has overridden the other properties for that pid from previously modified files.
         */
        verifyValueOfConfigurationProperty("different.files.global.pid", conflictProperty, value)
    }

    @Test
    public void 'the same local and global PIDs in different files are considered one PID'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_and_local_pid_different_files_conf"
        def servicesDirectory = "global_and_local_pid_no_conflict_services"
        def defaultConfigFilePath = "$configDirectory/global.and.local.pid.default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        // Assert that the configuration is updated with all the properties for a pid from all the processed files.
        verifyValueOfConfigurationProperty("no.conflict.global.and.local.pid", "global.property", "global.value")
        verifyValueOfConfigurationProperty("no.conflict.global.and.local.pid", "local.property", "local.value")
    }

    @Test
    public void 'local PID is overridden by global PID in different file, if the file with the global one is modified last'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_and_local_pid_different_files_conf"
        def servicesDirectory = "global_and_local_pid_overridden_local_services"
        def defaultConfigFilePath = "$configDirectory/global.and.local.pid.default.file.cfg"
        def lastModifiedFileName = "a.overriding.global.pid.service.file.cfg"

        /*
         * Both files(with local and global pid) contain this property, but with different value.
         * The value for this property in the last processed file must override the previous 
         * values for the same property in the configuration.
         */
        def conflictProperty = "global.and.local.property"

        File fileToModify = new File("$configDirectory/$servicesDirectory/$lastModifiedFileName")

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        modifyFile(fileToModify)

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        String value = getLastModifiedValueForPoperty("$configDirectory/$servicesDirectory", conflictProperty)

        /*
         * Assert that the global pid from the last modified file
         * has overridden the local pid from previously processed file.
         */
        verifyValueOfConfigurationProperty("overridden.local.pid", conflictProperty, value)
    }

    @Test
    public void 'global PID is overridden by local PID in different file, if the file with the local one is modified last'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/global_and_local_pid_different_files_conf"
        def servicesDirectory = "global_and_local_pid_overridden_global_services"
        def defaultConfigFilePath = "$configDirectory/global.and.local.pid.default.file.cfg"
        def lastModifiedFileName = "a.overriding.local.pid.service.file.cfg"

        /*
         * Both files(with local and global pid) contain this property, but with different value.
         * The value for this property in the last processed file must override the previous 
         * values for the same property in the configuration.
         */
        def conflictProperty = "global.and.local.property"

        File fileToModify = new File("$configDirectory/$servicesDirectory/$lastModifiedFileName")

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        modifyFile(fileToModify)

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        String value = getLastModifiedValueForPoperty("$configDirectory/$servicesDirectory", conflictProperty)

        /*
         * Assert that the local pid from the last modified file
         * has overridden the global pid from previously processed file.
         */
        verifyValueOfConfigurationProperty("overridden.global.pid", conflictProperty, value)
    }

    @Test
    public void 'properties for PID in files in services directory must override default properties'(){

        def configDirectory = "$CONFIGURATION_WATCH_DIRECTORY/default_vs_services_files_conf"
        def servicesDirectory = "default_vs_services_files_services"
        def defaultConfigFilePath = "$configDirectory/default.file.cfg"

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath)

        /*
         * Assert that a file from the services directory is processed
         * after a file from the root directory, therefore the configuration
         * is updated with properties from the file in the services directory.
         */
        verifyValueOfConfigurationProperty("default.and.service.global.pid", "property1", "value1.2")
        verifyValueOfConfigurationProperty("default.and.service.local.pid", "property2", "value2.2")
    }

    private void initialize(String configDirectory, String serviceDirectory, String defaultConfigFile){

        setSystemProperties(configDirectory, serviceDirectory, defaultConfigFile)

        // Start the stopped component, so that the file in the root directory will be read.
        context.enableComponent(CONFIG_DISPATCHER_COMPONENT_ID)
    }

    private void setSystemProperties(String configDirectory, String serviceDirectory, String defaultConfigFile){

        setSystemProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, configDirectory)
        setSystemProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT, serviceDirectory)
        setSystemProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT, defaultConfigFile)
    }

    private void verifyValueOfConfigurationProperty(String pid, String property, String value){

        /* We have to wait for all the files to be processed and the configuration to be updated.
         * Sending events, related to modification of file, is a OS specific action.
         * So when we check if a configuration is updated, we use separate waitForAssert-s
         * in order to be sure that the events are processed before the assertion.
         */
        waitForAssert {
            configuration = configAdmin.getConfiguration(pid)
            assertThat "The configuration for the given pid cannot be found", configuration, is(notNullValue())
        }
        waitForAssert {
            assertThat "There are no properties for the configuration", configuration.getProperties(), is(notNullValue())
        }
        waitForAssert {
            assertThat "There is no such '$property' in the configuration properties", configuration.getProperties().get(property), is(notNullValue())
        }
        waitForAssert {
            assertThat "The value of the property '$property' for pid '$pid' is not as expected",
                    configuration.getProperties().get(property), is(equalTo(value))
        }
    }

    private void verifyNotExistingConfigurationProperty(String pid, String property) {

        /* 
         * If a property is not present in the configuration's properties,
         * configuration.getProperties().get(property) should return null.
         * 
         * Sending events, related to modification of file, is a OS specific action.
         * So when we check if a configuration is updated, we use separate waitForAssert-s
         * in order to be sure that the events are processed before the assertion.
         */
        waitForAssert {
            configuration = configAdmin.getConfiguration(pid)
            assertThat "The configuration for the given pid cannot be found", configuration, is(notNullValue())
        }
        waitForAssert {
            assertThat "There are no properties for the configuration", configuration.getProperties(), is(notNullValue())
        }
        waitForAssert {
            assertThat "There should not be such '$property' in the configuration properties", configuration.getProperties().get(property), is(nullValue())
        }
    }

    private void verifyNoPropertiesForConfiguration(String pid){

        // We have to wait for all the files to be processed and the configuration to be updated.
        waitForAssert {
            configuration = configAdmin.getConfiguration(pid)
            assertThat "Configuration properties should not be present",
                    configuration.getProperties(), is(nullValue())
        }
    }

    private void modifyFile(File file){

        RandomAccessFile raf
        raf = new RandomAccessFile(file, "rw")
        def initialLength = raf.length()

        try{
            file << "modification line"
        } catch(IOException | FileNotFoundException e){
            fail("An exception $e was thrown, while modifying the file $file.getPath()")
        } finally {
            // Delete modified contents by returning the initial length of the file.
            raf.setLength(initialLength)
            if(raf != null){
                raf.close()
            }
        }
    }

    private String getLastModifiedValueForPoperty(String path, String property){

        // This method will return null, if there are no files in the directory.
        String value
        FileInputStream fileInputStream
        File file
        try{
            file = getLastModifiedFileFromDir(path)
            if(file == null){
                return null
            }
            fileInputStream = new FileInputStream(file)
            List<String> lines = IOUtils.readLines(fileInputStream)
            for(String line : lines){
                if(line.contains("$property=")){
                    value = StringUtils.substringAfter(line, "$property=")
                }
            }
        } catch(IOException e){
            fail("An exception $e was thrown, while reading from file $file.getPath()")
        } finally{
            if(fileInputStream != null){
                fileInputStream.close()
            }
        }
        return value
    }

    /*
     * Mainly used the code, provided in
     * http://stackoverflow.com/questions/2064694/how-do-i-find-the-last-modified-file-in-a-directory-in-java
     */
    private File getLastModifiedFileFromDir(String dirPath){
        File dir = new File(dirPath)
        File[] files = dir.listFiles()
        if (files == null || files.length == 0) {
            return null
        }

        File lastModifiedFile = files[0]
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i]
            }
        }
        return lastModifiedFile
    }

    private static void setSystemProperty(String systemProperty, String initialValue){

        if(initialValue != null){
            System.setProperty(systemProperty, initialValue)
        } else {
            /* A system property cannot be set to null, it has to be cleared */
            System.clearProperty(systemProperty)
        }
    }
}
