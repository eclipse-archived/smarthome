/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.tests;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.BundleProcessor
import org.eclipse.smarthome.config.core.BundleProcessor.BundleProcessorListener
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.thing.test.hue.TestHueThingHandlerFactoryX
import org.eclipse.smarthome.model.thing.test.hue.TestHueThingTypeProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkUtil
import org.osgi.service.component.ComponentContext


/**
 * Test class for the GenericThingProvider.
 *
 * It focuses on the XML processing (i.e. ThingType loading) and verifies
 * that the loading of Things gets delayed until the XML processing is completed.
 *
 * @author Simon Kaufmann - initial contribution and API.
 */
class GenericThingProviderTest4 extends OSGiTest{
    private TestHueThingTypeProvider thingTypeProvider
    private BundleProcessor bundleProcessor
    private Bundle bundle
    private ThingHandlerFactory hueThingHandlerFactory
    private BundleProcessorListener listener
    private boolean finished

    private final static String TESTMODEL_NAME = "testModelX.things"

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
        hueThingHandlerFactory = new TestHueThingHandlerFactoryX(componentContextMock)

        finished = false;
        bundle = FrameworkUtil.getBundle(TestHueThingHandlerFactoryX)
        bundleProcessor = [
            "hasFinishedLoading": { return finished },
            "registerListener": {l -> listener = l},
            "unregisterListener": {l -> listener = null}
        ] as BundleProcessor
        registerService(bundleProcessor)
    }

    @After
    public void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME);
        if (thingTypeProvider != null) {
            unregisterService(thingTypeProvider)
        }
    }

    private def updateModel() {
        String model =
                '''
            Xhue:Xbridge:myBridge [ XipAddress = "1.2.3.4", XuserName = "123" ]
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
    }

    private def finishLoading() {
        thingTypeProvider = new TestHueThingTypeProvider()
        registerService(thingTypeProvider)

        finished = true;
        listener.bundleFinished(bundleProcessor, bundle)
    }

    private void assertThatAllIsGood() {
        assertThat thingRegistry.getAll().size(), is(1)

        Thing bridge = thingRegistry.get(new ThingUID("Xhue:Xbridge:myBridge"))
        assertThat bridge, is(notNullValue())
        assertThat bridge, is(instanceOf(Bridge))
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with update_factory_loaded'() {
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with factory_update_loaded'() {
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with loaded_factory_update'() {
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with loaded_update_factory'() {
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with factory_loaded_update'() {
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with update_loaded_factory'() {
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThat thingRegistry.getAll().size(), is(0)
        finishLoading()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThatAllIsGood()
    }
}
