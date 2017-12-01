/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author David Graeff - Initial contribution
 *
 */
public class NestedConfigConverterTest {
    Map<String, Object> nested;
    Map<String, Object> flat;

    @Before
    public void setup() {
        nested = new HashMap<>();
        nested.put("simple", "value");
        nested.put("simpleInt", 12);

        Map<String, Object> instances = new HashMap<>();
        nested.put("broker", instances);

        Map<String, Object> br1 = new HashMap<>();
        Map<String, Object> br2 = new HashMap<>();
        instances.put("br1", br1);
        instances.put("br2", br2);

        br1.put("host", "value");
        br1.put("port", 12);
        br2.put("host", "value2");
        br2.put("port", 24);

        flat = new HashMap<>();
        flat.put("simple", "value");
        flat.put("simpleInt", 12);
        flat.put("broker.br1#host", "value");
        flat.put("broker.br2#host", "value2");
        flat.put("broker.br1#port", 12);
        flat.put("broker.br2#port", 24);
    }

    @Test
    public void nestedToFlat() throws URISyntaxException {
        Map<String, Object> subject = NestedConfigConverter.mapToFlat(nested);
        assertThat(subject, is(flat));
    }

    @Test
    public void flatToNested() throws URISyntaxException {
        assert flat != null;
        NestedConfigConverter.mapToNested(flat);
        assertThat(flat, is(nested));
    }

}
