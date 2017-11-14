/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.internal.discovery

import static org.junit.Assert.*

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.*

import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.io.rest.core.internal.discovery.InboxResource
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

class InboxResourceOSGITest extends OSGiTest {

    ThingTypeRegistry thingTypeRegistry = new ThingTypeRegistry()
    ConfigDescriptionRegistry configDescRegistry
    Inbox inbox
    InboxResource resource
    ThingRegistry thingRegistry

    final ThingUID testUID = new ThingUID("binding:type:id")
    final Thing testThing = ThingBuilder.create(testUID).build()
    final String testThingLabel = "dummy_thing"

    @Before
    void setup() throws Exception {
        inbox = getService Inbox
        configDescRegistry = getService ConfigDescriptionRegistry
        registerService new InboxResource(), InboxResource.class.getName()
        resource = getService InboxResource
        resource.setInbox(inbox)
        assertFalse thingTypeRegistry == null
        assertFalse configDescRegistry == null
        assertFalse resource == null
    }

    @Test
    void 'assert that approve approves Things which are in the Inbox' () {
        inbox = [
            approve :{ thingUID, label -> return testThing }
        ] as Inbox
        resource.setInbox(inbox)
        Response reponse = resource.approve(null, testThing.getUID().toString(), testThingLabel)
        assertTrue reponse.getStatusInfo() == Status.OK
    }

    @Test
    void 'assert that approve dont approves Things which are not in the Inbox' () {
        inbox = [
            approve :{ thingUID, label ->
                throw new IllegalArgumentException()
            }
        ] as Inbox
        resource.setInbox(inbox)
        Response reponse = resource.approve(null, testThing.getUID().toString(), testThingLabel)
        assertTrue reponse.getStatusInfo() == Status.NOT_FOUND
    }
}
