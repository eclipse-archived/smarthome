/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.test.cases

import static org.mockito.Mockito.*

import org.eclipse.smarthome.binding.astro.AstroBindingConstants
import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler
import org.eclipse.smarthome.binding.astro.handler.SunHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.DateTimeItem
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.mockito.Mockito

/**
 * Contains some test data used across different tests
 *
 * @author Svilen Valkanov - Initial implementation
 */
class AstroBindingTestsData {
    public static final String TEST_SUN_THING_ID = "testSunThingId"
    public static final String TEST_MOON_THING_ID = "testMoonThingId"
    public static final String TEST_ITEM_NAME = "testItem"
    public static final String DEFAULT_IMEM_TYPE = "DateTime"

    public static final String DEFAULT_TEST_CHANNEL_ID = "rise#start"
    public static final String GEOLOCATION_PROPERTY = "geolocation"
    public static final String GEOLOCATION_VALUE = "51.2,25.4"
    public static final String INTERVAL_PROPERTY = "interval"
    public static final BigDecimal INTERVAL_DEFAULT_VALUE = new BigDecimal(300)
}
