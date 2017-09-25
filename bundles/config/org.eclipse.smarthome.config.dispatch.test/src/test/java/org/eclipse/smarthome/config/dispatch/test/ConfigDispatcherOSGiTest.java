package org.eclipse.smarthome.config.dispatch.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.dispatch.internal.ConfigDispatcher;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

public class ConfigDispatcherOSGiTest extends JavaOSGiTest {

    @Rule
    public TemporaryFolder tmpBaseFolder = new TemporaryFolder();

    private ConfigurationAdmin configAdmin;

    // The ComponentContext is static as we expect that it is the same for all tests.
    private static ComponentContext context;

    private static final String CONFIGURATION_BASE_DIR = "configurations";
    private static final String CONFIG_DISPATCHER_COMPONENT_ID = "org.eclipse.smarthome.config.dispatch.internal.ConfigDispatcher";
    private static final String SEP = File.separator;

    private static String defaultConfigDir;
    private static String defaultServiceDir;
    private static String defaultConfigFile;

    private Configuration configuration;
    private static String configBaseDirectory;

    protected void activate(ComponentContext componentContext) {
        /*
         * The files in the root configuration directory are read only on the
         * activate() method of the ConfigDispatcher. So before every test method
         * the component is disabled using the component context, the watched
         * directories are set and then the component is enabled again using the
         * component context. In that way we can be sure that the files in the
         * root directory will be read.
         */
        context = componentContext;
    }

    @BeforeClass
    public static void setUpClass() {
        // Store the default values in order to restore them after all the tests are finished.
        defaultConfigDir = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT);
        defaultServiceDir = System.getProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT);
        defaultConfigFile = System.getProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT);
    }

    @Before
    public void setUp() throws Exception {
        configBaseDirectory = tmpBaseFolder.getRoot().getAbsolutePath();
        FileUtils.copyDirectory(new File(CONFIGURATION_BASE_DIR), new File(configBaseDirectory));
        /*
         * Disable the component of the ConfigDispatcher,so we can
         * set the properties for the directories to be watched,
         * before the ConfigDispatcher is started.
         */
        context.disableComponent(CONFIG_DISPATCHER_COMPONENT_ID);

        /*
         * After we disable the component we have to wait for the
         * AbstractWatchService to stop watching the previously set
         * directories, because we will be setting a new ones.
         */
        Thread.sleep(300);

        configAdmin = getService(ConfigurationAdmin.class);
        assertThat("ConfigurationAdmin service cannot be found", configAdmin, is(notNullValue()));
    }

    @After
    public void tearDown() throws Exception {
        // Clear the configuration with the current pid from the persistent store.
        if (configuration != null) {
            configuration.delete();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Set the system properties to their initial values.
        setSystemProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, defaultConfigDir);
        setSystemProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT, defaultServiceDir);
        setSystemProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT, defaultConfigFile);
    }

    @Test
    public void allConfigurationFilesWithLocalPIDsAreProcessedAndConfigurationIsUpdated() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_conf";
        String servicesDirectory = "local_pid_services";
        String defaultConfigFileName = configDirectory + SEP + "local.pid.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFileName);

        // Assert that a file with local pid from the root configuration directory is processed.
        verifyValueOfConfigurationProperty("local.default.pid", "default.property", "default.value");

        // Assert that a file with local pid from the services directory is processed.
        verifyValueOfConfigurationProperty("local.service.first.pid", "service.property", "service.value");

        // Assert that more than one file with local pid from the services directory is processed.
        verifyValueOfConfigurationProperty("local.service.second.pid", "service.property", "service.value");
    }

    @Test
    public void whenTheConfigurationFileNameContainsDotAndNoPIDIsDefinedTheFileNameBecomesPID() {
        String configDirectory = configBaseDirectory + SEP + "no_pid_conf";
        String servicesDirectory = "no_pid_services";
        // In this test case we need configuration files, which names contain dots.
        String defaultConfigFilePath = configDirectory + SEP + "no.pid.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /* Assert that the configuration is updated with the name of the file in the root directory as a pid. */
        verifyValueOfConfigurationProperty("no.pid.default.file", "default.property", "default.value");

        /* Assert that the configuration is updated with the name of the file in the services directory as a pid. */
        verifyValueOfConfigurationProperty("no.pid.service.file", "service.property", "service.value");
    }

    @Test
    public void IfNoPIDIsDefinedInConfigurationFileWithNo_dotNameTheDefaultNamespacePlusTheFileNameBecomesPID() {
        String configDirectory = configBaseDirectory + SEP + "no_pid_no_dot_conf";
        String servicesDirectory = "no_pid_no_dot_services";
        // In this test case we need configuration files, which names don't contain dots.
        String defaultConfigFilePath = configDirectory + SEP + "default.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is updated with the default
         * namespace the no-dot name of the file in the root directory as pid.
         */
        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".default",
                "no.dot.default.property", "no.dot.default.value");

        /*
         * Assert that the configuration is updated with the default namespace
         * the no-dot name of the file in the services directory as pid.
         */
        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".service",
                "no.dot.service.property", "no.dot.service.value");
    }

    @Test
    public void whenLocalPIDDoesNotContainDotTheDefaultNamespacePlusThatPIDIsTheOverallPID() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_no_dot_conf";
        String servicesDirectory = "local_pid_no_dot_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.no.dot.default.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is updated with the default namespace
         * the no-dot pid from the file in the the root directory.
         */
        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".default", "default.property",
                "default.value");

        /*
         * Assert that the configuration is updated with the default namespace
         * the no-dot pid from the file in the the services directory.
         */
        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".service", "service.property",
                "service.value");
    }

    @Test
    public void whenLocalPIDIsAnEmptyStringTheDefaultNamespacePlusDotIsTheOverallPID() {
        // This test case is a corner case for the above test.
        String configDirectory = configBaseDirectory + SEP + "local_pid_empty_conf";
        String servicesDirectory = "local_pid_empty_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.empty.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".", "default.property",
                "default.value");

        verifyValueOfConfigurationProperty(ConfigDispatcher.SERVICE_PID_NAMESPACE + ".", "service.property",
                "service.value");
    }

    @Test
    public void valueIsStillValidIfItIsLeftEmpty() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_no_value_conf";
        String servicesDirectory = "local_pid_no_value_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.no.value.default.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is updated with
         * an empty value for a file in the root directory.
         */
        verifyValueOfConfigurationProperty("default.pid", "default.property", "");

        /*
         * Assert that the configuration is updated with
         * an empty value for a file in the services directory.
         */
        verifyValueOfConfigurationProperty("service.pid", "service.property", "");
    }

    @Test
    public void ifPropertyIsLeftEmptyAConfigurationWithTheGivenPIDWillNotExist() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_no_property_conf";
        String servicesDirectory = "local_pid_no_property_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.no.property.default.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is not updated with an
         * empty-string property from the file in the root directory.
         */
        verifyNoPropertiesForConfiguration("no.property.default.pid");

        /*
         * Assert that the configuration is not updated with an
         * empty-string property from the file in the services directory.
         */
        verifyNoPropertiesForConfiguration("no.property.service.pid");
    }

    @Test
    public void propertiesForLocalPIDCanBeOverridden() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_conflict_conf";
        String servicesDirectory = "local_pid_conflict_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.conflict.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the second processed property from the
         * file in the root directory overrides the first one.
         */
        verifyValueOfConfigurationProperty("same.default.local.pid", "default.property", "default.value2");

        /*
         * Assert that the second processed property from the file
         * in the services directory overrides the first one.
         */
        verifyValueOfConfigurationProperty("same.service.local.pid", "service.property", "service.value2");
    }

    @Test
    public void commentLinesAreNotProcessed() {
        String configDirectory = configBaseDirectory + SEP + "comments_conf";
        String servicesDirectory = "comments_services";
        String defaultConfigFilePath = configDirectory + SEP + "comments.default.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is not updated with a
         * pid from a comment from a file in the root directory.
         */
        verifyNoPropertiesForConfiguration("comment.default.pid");

        /*
         * Assert that the configuration is not updated with a
         * pid from a comment from a file in the services directory.
         */
        verifyNoPropertiesForConfiguration("comment.service.pid");
    }

    @Test
    public void txtFilesAreNotProcessed() {
        String configDirectory = configBaseDirectory + SEP + "txt_conf";
        String servicesDirectory = "txt_services";
        String defaultConfigFilePath = configDirectory + SEP + "txt.default.txt";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is not updated with
         * pid from a txt file in the root directory.
         */
        verifyNoPropertiesForConfiguration("txt.default.pid");

        /*
         * Assert that the configuration is not updated with
         * pid from a txt file in the services directory.
         */
        verifyNoPropertiesForConfiguration("txt.service.pid");
    }

    @Test
    public void allConfigurationFilesWithGlobalPIDsAreProcessedAndConfigurationIsUpdated() {
        String configDirectory = configBaseDirectory + SEP + "global_pid_conf";
        String servicesDirectory = "global_pid_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.pid.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        // Assert that a file with global pid from the root configuration directory is processed.
        verifyValueOfConfigurationProperty("global.default.pid", "default.property", "default.value");

        // Assert that a file with global pid from the services directory is processed.
        verifyValueOfConfigurationProperty("global.service.first.pid", "service.property", "service.value");

        // Assert that more than one file with global pid from the services directory is processed.
        verifyValueOfConfigurationProperty("global.service.second.pid", "service.property", "service.value");
    }

    @Test
    public void ifThePropertyValuePairIsPrefixedWithLocalPIDInTheSameFileTheGlobalPIDIsIgnored() {
        String configDirectory = configBaseDirectory + SEP + "ignored_global_pid_conf";
        String servicesDirectory = "ignored_global_pid_services";
        String defaultConfigFilePath = configDirectory + SEP + "ignored.global.default.pid.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        // Assert that the configuration is not updated with the global pid in the root directory.
        verifyNoPropertiesForConfiguration("ignored.global.default.pid");

        // Assert that the configuration is updated with the local pid in the root directory.
        verifyValueOfConfigurationProperty("not.ignored.local.default.pid", "local.default.property",
                "local.default.value");

        // Assert that the configuration is not updated with the global pid in the services directory.
        verifyNoPropertiesForConfiguration("ignored.global.service.pid");

        // Assert that the configuration is updated with the local pid in the services directory.
        verifyValueOfConfigurationProperty("not.ignored.local.service.pid", "local.service.property",
                "local.service.value");
    }

    @Test
    public void ifThePropertyIsNotPrefixedWithLocalPIDTheLastPIDBecomesPIDForThatProperty() {
        String configDirectory = configBaseDirectory + SEP + "last_pid_conf";
        String servicesDirectory = "last_pid_services";
        String defaultConfigFilePath = configDirectory + SEP + "first.global.default.pid.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that a property=value pair is associated with the global pid,
         * defined in the above line in a file in the root directory
         * rather than with the local pid, defined in the below line.
         */
        verifyValueOfConfigurationProperty("first.global.default.pid", "global.default.property1",
                "global.default.value1");
        verifyNotExistingConfigurationProperty("last.local.default.pid", "global.default.property1");

        /*
         * Assert that a property=value pair is associated with the last defined local pid,
         * rather than with the global pid, defined in the first line of a file in the root directory.
         */
        verifyValueOfConfigurationProperty("last.local.default.pid", "global.default.property2",
                "global.default.value2");
        verifyNotExistingConfigurationProperty("first.global.default.pid", "global.default.property2");

        /*
         * Assert that a property=value pair is associated with the global pid,
         * defined in the above line in a file in the services directory
         * rather than with the local pid, defined in the below line.
         */
        verifyValueOfConfigurationProperty("first.global.service.pid", "global.service.property1",
                "global.service.value1");
        verifyNotExistingConfigurationProperty("last.local.default.pid", "global.service.property1");

        /*
         * Assert that a property=value pair is associated with the last defined local pid,
         * rather than with the global pid, defined in the first line of a file in the services directory.
         */
        verifyValueOfConfigurationProperty("last.local.service.pid", "global.service.property2",
                "global.service.value2");
        verifyNotExistingConfigurationProperty("first.global.service.pid", "global.service.property2");
    }

    @Test
    public void ifThereIsNoPropertyValuePairForGlobalPIDAConfigurationWithTheGivenPIDWillNotExist() {
        String configDirectory = configBaseDirectory + SEP + "global_pid_no_pair_conf";
        String servicesDirectory = "global_pid_no_pair_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.pid.no.pair.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that pid with no property=value pair
         * is not processed in a file in the root directory.
         */
        verifyNoPropertiesForConfiguration("no.pair.default.pid");

        /*
         * Assert that pid with no property=value pair
         * is not processed in a file in the services directory.
         */
        verifyNoPropertiesForConfiguration("no.pair.service.pid");
    }

    @Test
    public void whenGlobalPIDIsEmptyStringItRemainsAnEmptyString() {
        String configDirectory = configBaseDirectory + SEP + "global_pid_empty_conf";
        String servicesDirectory = "global_pid_empty_services";

        initialize(configDirectory, servicesDirectory, null);

        // Assert that an empty-string pid from a file in the services directory is processed.
        verifyValueOfConfigurationProperty("", "global.service.property", "global.service.value");
    }

    @Test
    public void PropertyValuePairsForALocalPIDInDifferentFilesAreStillAssociatedWithThatPID() {
        String configDirectory = configBaseDirectory + SEP + "local_pid_different_files_no_conflict_conf";
        String servicesDirectory = "local_pid_different_files_no_conflict_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that the configuration is updated with all the property=value
         * pairs for the same local pid from all the processed files.
         */
        verifyValueOfConfigurationProperty("local.pid", "first.property", "first.value");
        verifyValueOfConfigurationProperty("local.pid", "second.property", "second.value");
        verifyValueOfConfigurationProperty("local.pid", "third.property", "third.value");
        verifyValueOfConfigurationProperty("local.pid", "fourth.property", "fourth.value");
    }

    @Test
    public void propertiesForTheSameLocalPIDInDifferentFilesMustBeOverriddenInTheOrderTheyWereLastModified()
            throws Exception {
        String configDirectory = configBaseDirectory + SEP + "local_pid_different_files_conflict_conf";
        String servicesDirectory = "local_pid_different_files_conflict_services";
        String defaultConfigFilePath = configDirectory + SEP + "local.pid.default.file.cfg";
        String lastModifiedFileName = "last.modified.service.file.cfg";

        /*
         * Every file from servicesDirectory contains this property, but with different value.
         * The value for this property in the last processed file must override the previous
         * values for the same property in the configuration.
         */
        String conflictProperty = "property";

        File fileToModify = new File(configDirectory + SEP + servicesDirectory + SEP + lastModifiedFileName);

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        FileUtils.touch(fileToModify);

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        String value = getLastModifiedValueForPoperty(configDirectory + SEP + servicesDirectory, conflictProperty);

        /*
         * Assert that the property for the same local pid in the last modified file
         * has overridden the other properties for that pid from previously modified files.
         */
        verifyValueOfConfigurationProperty("local.conflict.pid", conflictProperty, value);
    }

    @Test
    public void whenPropertyValuePairsForAGlobalPIDAreInDifferentFilesPropertiesWillNotBeMerged() throws IOException {
        String configDirectory = configBaseDirectory + SEP + "global_pid_different_files_no_merge_conf";
        String servicesDirectory = "global_pid_different_files_no_merge_services";

        initialize(configDirectory, servicesDirectory, null);

        File lastModified = new File(configDirectory + SEP + servicesDirectory + SEP + "global.pid.service.c.file.cfg");
        FileUtils.touch(lastModified);

        /*
         * Assert that the configuration is updated only with the property=value
         * pairs which are parsed last:
         */
        verifyNotExistingConfigurationProperty("different.files.global.pid", "first.property");
        verifyNotExistingConfigurationProperty("different.files.global.pid", "second.property");
        verifyValueOfConfigurationProperty("different.files.global.pid", "third.property", "third.value");
    }

    @Test
    public void whenLocalPIDIsDefinedForGlobalPIDFile_AbortParsing() {
        String configDirectory = configBaseDirectory + SEP + "global_pid_with_local_pid_line_error";
        String servicesDirectory = "global_pid_with_local_pid_line_services_error";

        initialize(configDirectory, servicesDirectory, null);

        /*
         * Assert that the configuration is updated only with the property=value
         * pairs which are parsed last:
         */
        verifyNotExistingConfigurationProperty("global.service.pid", "property1");
    }

    @Test
    public void whenExclusivePIDFileIsDeleted_DeleteTheConfiguration() throws IOException {
        String configDirectory = configBaseDirectory + SEP + "exclusive_pid_file_removed_during_runtime";
        String servicesDirectory = "exclusive_pid_file_removed_during_runtime_services";

        initialize(configDirectory, servicesDirectory, null);
        verifyValueOfConfigurationProperty("global.service.pid", "property1", "value1");

        String pid = "service.pid.cfg";
        File serviceConfigFile = new File(configDirectory + SEP + servicesDirectory, pid);

        serviceConfigFile.delete();

        waitForAssert(() -> {
            try {
                configuration = configAdmin.getConfiguration(pid);
            } catch (IOException e) {
                fail("IOException occured while retrieving configuration for pid " + pid);
            }
            assertThat("The configuration with properties for the given pid was found but should have been removed.",
                    configuration.getProperties(), is(nullValue()));
        });
    }

    @Test
    public void whenExclusiveConfigIsTruncated_OverrideReducedConfig() throws IOException {
        String configDirectory = configBaseDirectory + SEP + "exclusive_pid_overrides_configuration_on_update";
        String servicesDirectory = "exclusive_pid_overrides_configuration_on_update_services";

        File serviceConfigFile = new File(configDirectory + SEP + servicesDirectory, "service.pid.cfg");

        initialize(configDirectory, servicesDirectory, null);

        /*
         * Assert that the configuration is updated with all properties:
         */
        verifyValueOfConfigurationProperty("global.service.pid", "property1", "value1");
        verifyValueOfConfigurationProperty("global.service.pid", "property2", "value2");

        truncateLastLine(serviceConfigFile);

        /*
         * Assert that the configuration is updated without the truncated property/value pair:
         */
        verifyValueOfConfigurationProperty("global.service.pid", "property1", "value1");
        verifyNotExistingConfigurationProperty("global.service.pid", "property2");
    }

    @Test
    public void whenExclusiveConfigFileIsDeleted_shouldRemoveConfigFromConfigAdmin() throws IOException {
        String configDirectory = configBaseDirectory + SEP + "exclusive_pid_configuration_removed_after_file_delete";
        String servicesDirectory = "exclusive_pid_configuration_removed_after_file_delete_services";

        File serviceConfigFile = new File(configDirectory + SEP + servicesDirectory, "service.pid.cfg");

        initialize(configDirectory, servicesDirectory, null);

        /*
         * Assert that the configuration is updated with all properties:
         */
        String pid = "global.service.pid";
        verifyValueOfConfigurationProperty(pid, "property1", "value1");
        verifyValueOfConfigurationProperty(pid, "property2", "value2");

        context.disableComponent(CONFIG_DISPATCHER_COMPONENT_ID);

        // remember the file content and delete the file:
        serviceConfigFile.delete();

        context.enableComponent(CONFIG_DISPATCHER_COMPONENT_ID);
        /*
         * Assert that the configuration was deleted from configAdmin
         */
        waitForAssert(() -> {
            try {
                configuration = configAdmin.getConfiguration(pid);
            } catch (IOException e) {
                fail("IOException occured while retrieving configuration for pid " + pid);
            }
            assertThat("The configuration with properties for the given pid was found but should have been removed.",
                    configuration.getProperties(), is(nullValue()));
        });
    }

    @Test
    public void propertiesForTheSameGlobalPIDInDifferentFilesMustBeOverriddenInTheOrderTheyWereLastModified()
            throws Exception {
        String configDirectory = configBaseDirectory + SEP + "global_pid_different_files_conflict_conf";
        String servicesDirectory = "global_pid_different_files_conflict_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.pid.default.file.cfg";
        String lastModifiedFileName = "global.pid.last.modified.service.file.cfg";

        /*
         * Every file from servicesDirectory contains this property, but with different value.
         * The value for this property in the last processed file will override the previous
         * values for the same property in the configuration.
         */
        String conflictProperty = "property";

        File fileToModify = new File(configDirectory + SEP + servicesDirectory + SEP + lastModifiedFileName);

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        FileUtils.touch(fileToModify);

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        String value = getLastModifiedValueForPoperty(configDirectory + SEP + servicesDirectory, conflictProperty);

        /*
         * Assert that the property for the same global pid in the last modified file
         * has overridden the other properties for that pid from previously modified files.
         */
        verifyValueOfConfigurationProperty("different.files.global.pid", conflictProperty, value);
    }

    @Test
    public void theSameLocalAndGlobalPIDsInDifferentFilesAreConsideredOnePID() {
        String configDirectory = configBaseDirectory + SEP + "global_and_local_pid_different_files_conf";
        String servicesDirectory = "global_and_local_pid_no_conflict_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.and.local.pid.default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        // Assert that the configuration is updated with all the properties for a pid from all the processed files.
        verifyValueOfConfigurationProperty("no.conflict.global.and.local.pid", "global.property", "global.value");
        verifyValueOfConfigurationProperty("no.conflict.global.and.local.pid", "local.property", "local.value");
    }

    @Test
    public void localPIDIsOverriddenByGlobalPIDInDifferentFileIfTheFileWithTheGlobalOneIsModifiedLast()
            throws Exception {
        String configDirectory = configBaseDirectory + SEP + "global_and_local_pid_different_files_conf";
        String servicesDirectory = "global_and_local_pid_overridden_local_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.and.local.pid.default.file.cfg";
        String lastModifiedFileName = "a.overriding.global.pid.service.file.cfg";

        /*
         * Both files(with local and global pid) contain this property, but with different value.
         * The value for this property in the last processed file must override the previous
         * values for the same property in the configuration.
         */
        String conflictProperty = "global.and.local.property";

        File fileToModify = new File(configDirectory + SEP + servicesDirectory + SEP + lastModifiedFileName);

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        FileUtils.touch(fileToModify);

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        String value = getLastModifiedValueForPoperty(configDirectory + SEP + servicesDirectory, conflictProperty);

        /*
         * Assert that the global pid from the last modified file
         * has overridden the local pid from previously processed file.
         */
        verifyValueOfConfigurationProperty("overridden.local.pid", conflictProperty, value);
    }

    @Test
    public void globalPIDIsOverriddenByLocalPIDInDifferentFileIfTheFileWithTheLocalOneIsModifiedLast()
            throws Exception {
        String configDirectory = configBaseDirectory + SEP + "global_and_local_pid_different_files_conf";
        String servicesDirectory = "global_and_local_pid_overridden_global_services";
        String defaultConfigFilePath = configDirectory + SEP + "global.and.local.pid.default.file.cfg";
        String lastModifiedFileName = "a.overriding.local.pid.service.file.cfg";

        /*
         * Both files(with local and global pid) contain this property, but with different value.
         * The value for this property in the last processed file must override the previous
         * values for the same property in the configuration.
         */
        String conflictProperty = "global.and.local.property";

        File fileToModify = new File(configDirectory + SEP + servicesDirectory + SEP + lastModifiedFileName);

        // Modify this file, so that we are sure it is the last modified file in servicesDirectory.
        FileUtils.touch(fileToModify);

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        String value = getLastModifiedValueForPoperty(configDirectory + SEP + servicesDirectory, conflictProperty);

        /*
         * Assert that the local pid from the last modified file
         * has overridden the global pid from previously processed file.
         */
        verifyValueOfConfigurationProperty("overridden.global.pid", conflictProperty, value);
    }

    @Test
    public void propertiesForPIDInFilesInServicesDirectoryMustOverrideDefaultProperties() {
        String configDirectory = configBaseDirectory + SEP + "default_vs_services_files_conf";
        String servicesDirectory = "default_vs_services_files_services";
        String defaultConfigFilePath = configDirectory + SEP + "default.file.cfg";

        initialize(configDirectory, servicesDirectory, defaultConfigFilePath);

        /*
         * Assert that a file from the services directory is processed
         * after a file from the root directory, therefore the configuration
         * is updated with properties from the file in the services directory.
         */
        verifyValueOfConfigurationProperty("default.and.service.global.pid", "property1", "value1.2");
        verifyValueOfConfigurationProperty("default.and.service.local.pid", "property2", "value2.2");
    }

    private void initialize(String configDirectory, String serviceDirectory, String defaultConfigFile) {
        setSystemProperties(configDirectory, serviceDirectory, defaultConfigFile);

        // Start the stopped component, so that the file in the root directory will be read.
        context.enableComponent(CONFIG_DISPATCHER_COMPONENT_ID);
    }

    private void setSystemProperties(String configDirectory, String serviceDirectory, String defaultConfigFile) {
        setSystemProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, configDirectory);
        setSystemProperty(ConfigDispatcher.SERVICEDIR_PROG_ARGUMENT, serviceDirectory);
        setSystemProperty(ConfigDispatcher.SERVICECFG_PROG_ARGUMENT, defaultConfigFile);
    }

    private void verifyValueOfConfigurationProperty(String pid, String property, String value) {
        waitForAssert(() -> {
            try {
                configuration = configAdmin.getConfiguration(pid);
            } catch (IOException e) {
                fail("IOException occured while retrieving configuration for pid " + pid);
            }
            assertThat("The configuration for the given pid cannot be found", configuration, is(notNullValue()));
            assertThat("There are no properties for the configuration", configuration.getProperties(),
                    is(notNullValue()));
            assertThat("There is no such " + property + " in the configuration properties",
                    configuration.getProperties().get(property), is(notNullValue()));
            assertThat("The value of the property " + property + " for pid " + pid + " is not as expected",
                    configuration.getProperties().get(property), is(equalTo(value)));
        });
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
        waitForAssert(() -> {
            try {
                configuration = configAdmin.getConfiguration(pid);
            } catch (IOException e) {
                fail("IOException occured while retrieving configuration for pid " + pid);
            }
            assertThat("The configuration for the given pid cannot be found", configuration, is(notNullValue()));
            assertThat("There are no properties for the configuration", configuration.getProperties(),
                    is(notNullValue()));
            assertThat("There should not be such " + property + " in the configuration properties",
                    configuration.getProperties().get(property), is(nullValue()));
        });
    }

    private void verifyNoPropertiesForConfiguration(String pid) {
        // We have to wait for all the files to be processed and the configuration to be updated.
        waitForAssert(() -> {
            try {
                configuration = configAdmin.getConfiguration(pid);
            } catch (IOException e) {
                fail("IOException occured while retrieving configuration for pid " + pid);
            }
            assertThat("Configuration properties should not be present", configuration.getProperties(),
                    is(nullValue()));
        });
    }

    private void truncateLastLine(File file) throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream(file));
        IOUtils.writeLines(lines.subList(0, lines.size() - 1), "\n", new FileOutputStream(file));
    }

    private String getLastModifiedValueForPoperty(String path, String property) {
        // This method will return null, if there are no files in the directory.
        String value = null;

        File file = getLastModifiedFileFromDir(path);
        if (file == null) {
            return null;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            List<String> lines = IOUtils.readLines(fileInputStream);
            for (String line : lines) {
                if (line.contains(property + "=")) {
                    value = StringUtils.substringAfter(line, property + "=");
                }
            }
        } catch (IOException e) {
            fail("An exception " + e + " was thrown, while reading from file " + file.getPath());
        }

        return value;
    }

    /*
     * Mainly used the code, provided in
     * http://stackoverflow.com/questions/2064694/how-do-i-find-the-last-modified-file-in-a-directory-in-java
     */
    private File getLastModifiedFileFromDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    private static void setSystemProperty(String systemProperty, String initialValue) {
        if (initialValue != null) {
            System.setProperty(systemProperty, initialValue);
        } else {
            /* A system property cannot be set to null, it has to be cleared */
            System.clearProperty(systemProperty);
        }
    }
}
