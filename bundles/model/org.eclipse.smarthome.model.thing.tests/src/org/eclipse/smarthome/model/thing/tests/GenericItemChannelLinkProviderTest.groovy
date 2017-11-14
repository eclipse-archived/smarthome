/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.tests

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.thing.internal.GenericItemChannelLinkProvider;
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test;

class GenericItemChannelLinkProviderTest extends OSGiTest {
	
	private final static String THINGS_TESTMODEL_NAME = "test.things"
	private final static String ITEMS_TESTMODEL_NAME = "test.items"
	
	ModelRepository modelRepository
	ThingRegistry thingRegistry
	ItemRegistry itemRegistry
	ItemChannelLinkRegistry itemChannelLinkRegistry
    ItemChannelLinkProvider itemChannelLinkProvider
	
	@Before
	void setUp() {
		thingRegistry = getService ThingRegistry
		assertThat thingRegistry, is(notNullValue())
		modelRepository = getService ModelRepository
		assertThat modelRepository, is(notNullValue())
		itemRegistry = getService ItemRegistry
		assertThat itemRegistry, is(notNullValue())
		itemChannelLinkRegistry = getService ItemChannelLinkRegistry
		assertThat itemChannelLinkRegistry, is(notNullValue())
        itemChannelLinkProvider = getService ItemChannelLinkProvider
        assertThat itemChannelLinkProvider, is(notNullValue())
		modelRepository.removeModel(THINGS_TESTMODEL_NAME)
		modelRepository.removeModel(ITEMS_TESTMODEL_NAME)
	}
	
	@After
	void tearDown() {
		modelRepository.removeModel(THINGS_TESTMODEL_NAME)
		modelRepository.removeModel(ITEMS_TESTMODEL_NAME)
	}

	@Test
	void 'test that an ItemChannelLink was created for a thing and an item'() {
		def things = thingRegistry.all
		assertThat things.size(), is(0)

		String thingsModel =
			'''
			Bridge hue:bridge:huebridge [ ipAddress = "192.168.3.84", userName = "19fc3fa6fc870a4280a55f21315631f" ] {
				LCT001 bulb3 [ lightId = "3"	]
				LCT001 bulb4 [ lightId = "3" ]
			}
			'''
		modelRepository.addOrRefreshModel(THINGS_TESTMODEL_NAME, new ByteArrayInputStream(thingsModel.bytes))
		def actualThings = thingRegistry.all

		assertThat actualThings.size(), is(3)
		
		def items = itemRegistry.items
		assertThat items.size(), is(0)
		
		def itemChannelLinks = itemChannelLinkRegistry.all
		assertThat itemChannelLinks.size(), is(0)

		String itemsModel =
			'''
			Color	Light3Color					"Light3 Color"		{ channel="hue:LCT001:huebridge:bulb3:color" }
			'''
		modelRepository.addOrRefreshModel(ITEMS_TESTMODEL_NAME, new ByteArrayInputStream(itemsModel.bytes))
		def actualItems = itemRegistry.items
		
		assertThat actualItems.size(), is(1)
			
		def actualItemChannelLinks = itemChannelLinkRegistry.all
		assertThat actualItemChannelLinks.size(), is(1)
		assertThat actualItemChannelLinks.first().toString(), is(equalTo("Light3Color -> hue:LCT001:huebridge:bulb3:color"))
	}
	
    @Test
    void 'test that multiple links are created'() {
        def things = thingRegistry.all
        assertThat things.size(), is(0)

        String thingsModel =
            '''
            Bridge hue:bridge:huebridge [ ipAddress = "192.168.3.84", userName = "19fc3fa6fc870a4280a55f21315631f" ] {
                LCT001 bulb3 [ lightId = "3" ]
                LCT001 bulb4 [ lightId = "3" ]
            }
            '''
        modelRepository.addOrRefreshModel(THINGS_TESTMODEL_NAME, new ByteArrayInputStream(thingsModel.bytes))
        def actualThings = thingRegistry.all

        assertThat actualThings.size(), is(3)
        
        def items = itemRegistry.items
        assertThat items.size(), is(0)
        
        def itemChannelLinks = itemChannelLinkRegistry.all
        assertThat itemChannelLinks.size(), is(0)

        String itemsModel =
            '''
            Color   Light3Color                 "Light3 Color"      { channel="hue:LCT001:huebridge:bulb3:color, hue:LCT001:huebridge:bulb4:color" }
            '''
        modelRepository.addOrRefreshModel(ITEMS_TESTMODEL_NAME, new ByteArrayInputStream(itemsModel.bytes))
        def actualItems = itemRegistry.items
        
        assertThat actualItems.size(), is(1)
            
        def actualItemChannelLinks = itemChannelLinkRegistry.all
        assertThat actualItemChannelLinks.size(), is(2)
        assertThat actualItemChannelLinks.first().toString(),   is(equalTo("Light3Color -> hue:LCT001:huebridge:bulb3:color"))
        assertThat actualItemChannelLinks.last().toString(),    is(equalTo("Light3Color -> hue:LCT001:huebridge:bulb4:color"))
    }
}
