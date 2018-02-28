/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ThingUIDTest {

    @Test
    public void testThreeSegments() {
        ThingTypeUID thingType = new ThingTypeUID("fake", "type");
        ThingUID t = new ThingUID(thingType, "gaga");

        assertThat(t.getThingTypeId(), is("type"));
        assertThat(t.getId(), is("gaga"));
        assertThat(t.getAsString(), is("fake:type:gaga"));
    }

    @Test
    public void testTwoSegments() {
        ThingUID t = new ThingUID("fake", "gaga");

        assertThat(t.getThingTypeId(), is(nullValue()));
        assertThat(t.getId(), is("gaga"));
        assertThat(t.getAsString(), is("fake::gaga"));
    }
}
