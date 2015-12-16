/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.discovery

import static org.junit.Assert.*

import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.List
import java.util.Map

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.config.discovery.inbox.InboxListener
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemThingLink
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.thing.type.TypeResolver
import org.eclipse.smarthome.io.rest.core.discovery.InboxResource
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
            approve :{
                thingUID, label -> return testThing
            }
        ] as Inbox
        resource.setInbox(inbox)
        ThingSetupManager thingSetupManager = new ThingSetupManager(){
            public void enableChannels(Thing thing, ThingTypeUID thingTypeUID) {
            }
            public void createGroupItems(String label, List<String> groupNames, Thing thing, ThingTypeUID typeUID) {
            }
        }
        resource.setThingSetupManager(thingSetupManager)
        Response reponse = resource.approve(testThing.getUID().toString(), testThingLabel, false)
        assertTrue reponse.getStatusInfo() == Status.OK
    }
    
    @Test
    void 'assert that approve dont approves Things which are not in the Inbox' () {
        inbox = [
            approve :{
                thingUID, label -> throw new IllegalArgumentException()
            }
        ] as Inbox
        resource.setInbox(inbox)
        Response reponse = resource.approve(testThing.getUID().toString(), testThingLabel, false)
        assertTrue reponse.getStatusInfo() == Status.NOT_FOUND
    }
}
