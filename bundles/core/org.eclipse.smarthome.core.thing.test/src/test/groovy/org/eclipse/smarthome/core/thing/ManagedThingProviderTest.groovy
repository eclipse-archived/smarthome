/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.junit.Before
import org.junit.Test


/**
 * Testing thing properties. 
 * 
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 */
class ManagedThingProviderTest {

    def properties = ["key1":"value1", "key2":"value2"]
    def ManagedThingProvider mgdThingProvider

    @Before
    void setup() {
		mgdThingProvider = new ManagedThingProvider()
    }
    
	@Test
	void 'test create channel with NULL arguments'() {
		Channel newChannel = mgdThingProvider.createChannel(null, null, null)
		
		assertThat newChannel.getUID(), is(null)
		assertThat newChannel.getChannelTypeUID(), is(null)
		assertThat newChannel.getAcceptedItemType(), is(null)
		assertThat newChannel.getConfiguration(), is(not(null))
		assertThat newChannel.getLabel(), is(null)
		assertThat newChannel.getDescription(), is(null)
		assertThat newChannel.getProperties(), is(not(null))
		assertThat newChannel.getDefaultTags().size(), is(0)
	}
	
	@Test
	void 'test create channel'() {
		def channelUID = new ChannelUID("binding", "thing-type", "thing", "group", "id")
		
		Channel newChannel = mgdThingProvider.createChannel(channelUID, 'my-item-type', null)
		
		assertThat newChannel.getUID().toString(), is(equalTo("binding:thing-type:thing:group#id"))
		assertThat newChannel.getChannelTypeUID(), is(null)
		assertThat newChannel.getAcceptedItemType(), is('my-item-type')
		assertThat newChannel.getConfiguration(), is(not(null))
		assertThat newChannel.getLabel(), is(null)
		assertThat newChannel.getDescription(), is(null)
		assertThat newChannel.getProperties(), is(not(null))
		assertThat newChannel.getDefaultTags().size(), is(0)
	}
	
}
