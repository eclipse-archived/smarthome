/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Test for Configuration class.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ConfigurationTest {
    public static class ConfigSubClass {
        String user;
        String password;
    };

    public static class ConfigClass {
        private int intField;
        private boolean booleanField;
        private String stringField = "default";
        private static final String CONSTANT = "SOME_CONSTANT";
        ConfigSubClass credentials;
        List<ConfigSubClass> credentialList;
    };

    @Test
    public void getConfigAs() {
        Map<String, Object> data = new HashMap<>();
        data.put("intField", 1);
        data.put("booleanField", false);
        data.put("stringField", "test");
        data.put("notExisitingProperty", true);
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("user", "testuser");
        credentials.put("password", "testpwd");
        data.put("credentials", credentials);
        data.put("credentialList", Stream.of(credentials, credentials).collect(Collectors.toList()));

        Configuration configuration = new Configuration(data);

        ConfigClass configClass = configuration.as(ConfigClass.class);

        assertThat(configClass.intField, is(equalTo(1)));
        assertThat(configClass.booleanField, is(false));
        assertThat(configClass.stringField, is("test"));
        assertThat(configClass.credentials.user, is("testuser"));
        assertThat(configClass.credentials.password, is("testpwd"));
        assertThat(configClass.credentialList.get(0).password, is("testpwd"));
        assertThat(configClass.credentialList.get(1).password, is("testpwd"));
    }

    @Test
    public void allowsNullValues() {
        Map<String, Object> data = new HashMap<>();
        data.put("stringField", null);
        data.put("anotherField", null);
        Configuration configuration = new Configuration(data);

        // ensure conversions are null-tolerant and don't throw exceptions
        Map<String, Object> props = configuration.getProperties();
        Set<String> keys = configuration.keySet();
        Collection<Object> values = configuration.values();

        // ensure copies, not views
        configuration.put("stringField", "someValue");
        configuration.put("additionalField", "");
        assertThat(props.get("stringField"), is(nullValue()));
        assertThat(values.size(), is(2));
        assertThat(keys.size(), is(2));
        Iterator<Object> iterator = values.iterator();
        assertThat(iterator.next(), is(nullValue()));
        assertThat(iterator.next(), is(nullValue()));
    }

    @Test
    public void removeProperties() {
        Map<String, Object> data = new HashMap<>();
        data.put("intField", 1);
        data.put("booleanField", false);
        data.put("stringField", "test");
        data.put("notExisitingProperty", true);

        Map<String, Object> newData = new HashMap<>();
        data.put("booleanField", false);
        data.put("stringField", "test");
        data.put("notExisitingProperty", true);

        Configuration configuration = new Configuration(data);

        assertThat(configuration.get("intField"), is(equalTo(BigDecimal.ONE)));

        configuration.setProperties(newData);

        assertThat(configuration.get("intField"), is(equalTo(null)));
    }

    @Test
    public void toStringHandlesNull() {
        Map<String, Object> data = new HashMap<>();
        data.put("stringField", null);
        Configuration configuration = new Configuration(data);
        String res = configuration.toString();
        assertThat(res.contains("type=?"), is(true));
    }

    @Test
    public void normalizationSetProperties() {
        Map<String, Object> data = new HashMap<>();
        data.put("intField", 1);
        Configuration configuration = new Configuration(data);
        configuration.setProperties(data);
        assertThat(configuration.get("intField"), is(equalTo(BigDecimal.ONE)));
    }

    @Test
    public void normalizationInPut() {
        Configuration configuration = new Configuration();
        configuration.put("intField", 1);
        assertThat(configuration.get("intField"), is(equalTo(BigDecimal.ONE)));
    }
}
