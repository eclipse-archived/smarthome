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
