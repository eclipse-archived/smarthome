/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.writeback.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

/**
 * @author David Graeff - Initial contribution
 */
public class ConfigWritebackOSGiTest extends JavaOSGiTest implements ConfigurationListener {
    private ConfigWriteback configWriteback;
    private ServiceRegistration<?> registration;
    private List<ConfigurationEvent> events;

    @Before
    public void setUp() throws Exception {
        assertThat("ConfigurationAdmin service cannot be found", getService(ConfigurationAdmin.class),
                is(notNullValue()));

        configWriteback = getService(ConfigWriteback.class);
        assertThat("ConfigWriteback service cannot be found", configWriteback, is(notNullValue()));

        events = new ArrayList<>();
        registration = bundleContext.registerService(new String[] { ConfigurationListener.class.getName() }, this,
                null);
    }

    @After
    public void tearDown() throws Exception {

        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        events.clear();
    }

    @Test
    public void writeConfigBack() throws IOException {
        TestComponent t = new TestComponent();
        File compFile = new File(ConfigWriteback.SERVICES_FOLDER, TestComponent.class.getCanonicalName() + ".cfg");

        if (compFile.exists()) {
            compFile.delete();
        }

        // Create config file with test=oldvalue
        FileUtils.write(compFile, "pid:" + TestComponent.class.getCanonicalName() + "\ntest=oldvalue\n");
        assertTrue(compFile.exists());

        // Register test component and check property value
        ServiceRegistration<TestComponent> regService = bundleContext.registerService(TestComponent.class, t, null);
        Configuration configuration = configWriteback.configAdmin
                .getConfiguration(TestComponent.class.getCanonicalName());

        // File value should be available due to ConfigDispatcher
        verifyValueOfConfigurationProperty(configuration, "test", "oldvalue");

        // Create property update
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(ConfigWriteback.PROP_CONFIG_WRITEBACK, ConfigWriteback.WRITEBACK_ALLOW);
        props.put("test", "value");

        events.clear();
        configuration.update(props);

        // Wait for update to arrive at listeners
        waitForAssert(() -> {
            assertTrue(events.size() >= 1);
        }, 1000, 100);

        // Test existence of property
        verifyValueOfConfigurationProperty(configuration, "test", "value");

        List<String> readLines = FileUtils.readLines(compFile);
        assertThat(readLines.size(), is(3));// header(2)+props(1)
        assertThat(readLines.get(0), is("pid:" + TestComponent.class.getCanonicalName()));
        assertThat(readLines.get(2), is("test=value"));

        // Test if file is created if not existent
        compFile.delete();
        assertFalse(compFile.exists());
        events.clear();
        configuration.update(props);

        // Wait for update to arrive at listeners
        waitForAssert(() -> {
            assertTrue(events.size() >= 1);
        }, 1000, 100);

        assertTrue(compFile.exists());

        // Check if configuration removal leads to a file removal
        events.clear();
        configuration.delete();

        // Wait for update to arrive at listeners
        waitForAssert(() -> {
            assertTrue(events.size() >= 1);
            assertFalse(compFile.exists());
        }, 1000, 100);

        regService.unregister();
    }

    private void verifyValueOfConfigurationProperty(Configuration configuration, String property, String value) {
        waitForAssert(() -> {
            assertThat("The configuration for the given pid cannot be found", configuration, is(notNullValue()));
            assertThat("There are no properties for the configuration", configuration.getProperties(),
                    is(notNullValue()));
            assertThat("There is no such " + property + " in the configuration properties",
                    configuration.getProperties().get(property), is(notNullValue()));
            assertThat("The value of the property " + property + " is not as expected",
                    configuration.getProperties().get(property), is(equalTo(value)));
        }, 1000, 100);
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        events.add(event);
    }
}
