/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.junit.Test

class ThingUIDTest {

    @Test
    void 'read properties for 3 segments'() {
        def thingType = new ThingTypeUID("fake", "type")
        def t = new ThingUID(thingType, "gaga");

        assertThat t.getThingTypeId(), is("type")
        assertThat t.getId(), is("gaga")
        assertThat t.getAsString(), is("fake:type:gaga")
    }

    @Test
    void 'read properties for 2 segments'() {
        def t = new ThingUID("fake", "gaga");

        assertThat t.getThingTypeId(), is(null)
        assertThat t.getId(), is("gaga")
        assertThat t.getAsString(), is("fake::gaga")
    }
}
