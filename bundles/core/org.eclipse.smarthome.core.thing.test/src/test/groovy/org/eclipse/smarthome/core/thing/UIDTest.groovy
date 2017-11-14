/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.junit.Test

class UIDTest {

    @Test(expected=IllegalArgumentException)
    void 'UID cannot be constructed with invalid charaters'() {
        new ThingUID("binding:type:id_with_invalidchar#")
    }

    @Test
    void 'valid UIDs'() {
        new ThingUID("binding:type:id-1")
        new ThingUID("binding:type:id_1")
        new ThingUID("binding:type:ID")
        new ThingUID("00:type:ID")
    }
    
    @Test
    void 'channel UID with group'() {
        def channelUID = new ChannelUID("binding", "thing-type", "thing", "group", "id")
        assertThat channelUID.toString(), is(equalTo("binding:thing-type:thing:group#id"))
        assertThat channelUID.isInGroup(), is(true)
        assertThat channelUID.getId(), is("group#id")
        assertThat channelUID.getIdWithoutGroup(), is("id")
    }
}
