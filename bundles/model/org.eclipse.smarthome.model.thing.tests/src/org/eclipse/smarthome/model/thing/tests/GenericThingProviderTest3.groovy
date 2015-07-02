/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.thing.test.hue.DumbThingHandlerFactory;
import org.eclipse.smarthome.test.OSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;

/**
 * Test asynchronous loading behavior of DSL based thing descriptions.
 * 
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
class GenericThingProviderTest3 extends OSGiTest{
    private def dumbThingHandlerFactory

    private final static String TESTMODEL_NAME = "testModel3.things"

    ModelRepository modelRepository;
    ThingRegistry thingRegistry;

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
            dumb:DUMB:boo {
                Switch : notification [ duration = "5" ]
            }
			'''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        registerService(dumbThingHandlerFactory, ThingHandlerFactory.class.name)

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

        // ensure the property is not set,
        // i.e. the thing was created generically
        assertThat thingRegistry.getAll().first().getProperties().containsKey("funky"), is(false)

        // now become smart again...
        dumbThingHandlerFactory.setDumb(false)

        def waited = 0;
        while (waited < 600 && !thingRegistry.getAll().first().getProperties().containsKey("funky")) {
            Thread.sleep(100)
            waited++
        }

        // ensure the property now has been set,
        // i.e. the thing was created by our handler factory and was refreshed
        assertThat thingRegistry.getAll().first().getProperties().containsKey("funky"), is(true)
    }

}
