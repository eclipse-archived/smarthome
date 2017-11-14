/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.tests;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.thing.test.hue.DumbThingHandlerFactory
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 * Test asynchronous loading behavior of DSL based thing descriptions.
 *
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
class GenericThingProviderTest3 extends OSGiTest{
    private def dumbThingHandlerFactory

    private final static String TESTMODEL_NAME = "testModel3.things"

    ModelRepository modelRepository
    ThingRegistry thingRegistry

    @Before
    public void setUp() {
        thingRegistry = getService ThingRegistry
        assertThat thingRegistry, is(notNullValue())

        modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())

        modelRepository.removeModel(TESTMODEL_NAME)

        def componentContextMock = [
            getBundleContext: {getBundleContext()}
        ] as ComponentContext

        // create a "dumb" thing handler that acts as if the XML config was not yet loaded
        dumbThingHandlerFactory = new DumbThingHandlerFactory(componentContextMock, true)

        def things = thingRegistry.getAll()
        assertThat things.size(), is(0)

        String model =
                '''
            dumb:DUMB:boo "Test Label" @ "Test Location" [
                testConf="foo"
            ]
            {
                Switch : manual [ duration = "5" ]
            }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        registerService(dumbThingHandlerFactory, ThingHandlerFactory.class.name)

        def configDescription = new ConfigDescription(new URI("test:test"), [
            ConfigDescriptionParameterBuilder.create("testAdditional", ConfigDescriptionParameter.Type.TEXT).withRequired(false).withDefault("hello world").build(),
            ConfigDescriptionParameterBuilder.create("testConf", ConfigDescriptionParameter.Type.TEXT).withRequired(false).withDefault("bar").build()
        ] as List);

        registerService([
            getConfigDescription: {uri, locale -> configDescription}
        ] as ConfigDescriptionProvider)


    }

    @After
    public void tearDown() {
        unregisterService(dumbThingHandlerFactory)
        modelRepository.removeModel(TESTMODEL_NAME);
    }

    @Test
    public void 'assert that things are updated once the xml files have been processed'() {
        assertThat thingRegistry.getAll().size(), is(1)
        assertThat thingRegistry.getAll().first().UID.toString(), is("dumb:DUMB:boo")
        assertThat thingRegistry.getAll().first().getChannels().size(), is(1)
        assertThat thingRegistry.getAll().first().getChannel("manual"), is(notNullValue())
        assertThat thingRegistry.getAll().first().getLabel(), is("Test Label")
        assertThat thingRegistry.getAll().first().getLocation(), is("Test Location")
        assertThat thingRegistry.getAll().first().getConfiguration().getProperties().get("testConf"), is("foo")


        // now become smart again...
        dumbThingHandlerFactory.setDumb(false)
        ChannelType channelType1 = new ChannelType(new ChannelTypeUID(DumbThingHandlerFactory.BINDING_ID, "channel1"), false, "String", "Channel 1", null, null, null, null, null)
        def channelTypeProvider = [
            getChannelType: {ChannelTypeUID channelTypeUID, Locale locale ->
                if (channelType1.getUID().id.equals("channel1")) {
                    return channelType1
                }
                return null
            }
        ] as ChannelTypeProvider
        registerService(channelTypeProvider)

        // ensure thing type was considered and manual and predefined values are there.
        waitForAssert({
            assertThat thingRegistry.getAll().first().getLabel(), is("Test Label")
            assertThat thingRegistry.getAll().first().getLocation(), is("Test Location")
            assertThat thingRegistry.getAll().first().getChannels().size(), is(2)
            assertThat thingRegistry.getAll().first().getChannel("manual"), is(notNullValue())
            assertThat thingRegistry.getAll().first().getChannel("channel1"), is(notNullValue())

            // there is a default, so make sure the manually configured one (from the DSL) wins
            assertThat thingRegistry.getAll().first().getConfiguration().getProperties().get("testConf"), is("foo")

            // it's not manually configured, but the thing type defines a default, so ensure it's in
            assertThat thingRegistry.getAll().first().getConfiguration().getProperties().get("testAdditional"), is("hello world")
        })

    }
}
