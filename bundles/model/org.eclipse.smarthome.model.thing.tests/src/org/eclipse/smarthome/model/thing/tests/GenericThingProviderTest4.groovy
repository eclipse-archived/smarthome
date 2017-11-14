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

import org.eclipse.smarthome.core.service.ReadyMarker
import org.eclipse.smarthome.core.service.ReadyService
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.thing.test.hue.TestHueThingHandlerFactoryX
import org.eclipse.smarthome.model.thing.test.hue.TestHueThingTypeProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
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
@RunWith(Parameterized.class)
class GenericThingProviderTest4 extends OSGiTest{
    private TestHueThingTypeProvider thingTypeProvider
    private ReadyService readyService
    private Bundle bundle
    private ThingHandlerFactory hueThingHandlerFactory
    private boolean finished
    private int bridgeInitializeCounter
    private int thingInitializeCounter
    final boolean slowInit

    private final static String TESTMODEL_NAME = "testModelX.things"

    ModelRepository modelRepository;
    ThingRegistry thingRegistry;

    @Parameters(name = "{index}: slowInit={0}")
    public static Collection<Object[]> data() {
        return [
            [
                false
            ] as Object[],
            [
                true
            ] as Object[]
        ]
    }

    public GenericThingProviderTest4(boolean slowInit) {
        this.slowInit = slowInit
    }

    class TestBridgeHandler extends BaseBridgeHandler {
        public TestBridgeHandler(Bridge bridge) {
            super(bridge)
        }
        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }
        @Override
        public void initialize() {
            bridgeInitializeCounter++
            if (slowInit) {
                Thread.sleep(1000)
            }
            super.initialize()
        }
        @Override
        public void dispose() {
            super.dispose();
        }
    }

    @Before
    public void setUp() {
        readyService = getService ReadyService
        assertThat readyService, is(notNullValue())
        thingRegistry = getService ThingRegistry
        assertThat thingRegistry, is(notNullValue())
        modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())
        modelRepository.removeModel(TESTMODEL_NAME)

        def componentContextMock = [
            getBundleContext: { getBundleContext() }
        ] as ComponentContext
        hueThingHandlerFactory = new TestHueThingHandlerFactoryX(componentContextMock) {
                    @Override
                    protected ThingHandler createHandler(final Thing thing) {
                        if (thing instanceof Bridge) {
                            return new TestBridgeHandler((Bridge) thing)
                        } else {
                            return new BaseThingHandler(thing) {
                                        void handleCommand(ChannelUID arg0, Command arg1) {};
                                        @Override
                                        void initialize() {
                                            updateState(ThingStatus.ONLINE);
                                        }
                                    }
                        }
                    }
                }

        finished = false;
        bundle = FrameworkUtil.getBundle(TestHueThingHandlerFactoryX)

        removeReadyMarker()
    }

    private void removeReadyMarker() {
        waitForAssert {
            // wait for the XML processing to be finished, then remove the ready marker again
            ReadyMarker marker = new ReadyMarker("esh.xmlThingTypes", bundle.getSymbolicName())
            assertThat readyService.isReady(marker), is(true)
            readyService.unmarkReady(marker);
        }
    }

    @After
    public void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME);
        if (thingTypeProvider != null) {
            unregisterService(thingTypeProvider)
        }
    }

    private void prepareThingWithShutDownBundle() {
        updateModel()
        registerThingTypeProvider()
        finishLoading()
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThatAllIsGood()
        unload()
        unregisterService(ThingHandlerFactory.class.name)
        waitForAssert {
            assertThat thingRegistry.getAll().size(), is(1)
            assertThat thingRegistry.getAll().find{
                it.getUID().getAsString().equals("Xhue:Xbridge:myBridge")
            }.getStatus(), is(equalTo(ThingStatus.UNINITIALIZED))
            assertThat thingRegistry.getAll().find{
                it.getUID().getAsString().equals("Xhue:Xbridge:myBridge")
            }.getHandler(), is(nullValue())
        }
        removeReadyMarker()
    }

    private def updateModel() {
        String model =
                '''
            Xhue:Xbridge:myBridge [ XipAddress = "1.2.3.4", XuserName = "123" ]
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
    }

    private def registerThingTypeProvider() {
        thingTypeProvider = new TestHueThingTypeProvider()
        registerService(thingTypeProvider)
    }

    private def finishLoading() {
        finished = true;
        assertThat bridgeInitializeCounter, is(0)
        readyService.markReady(new ReadyMarker("esh.xmlThingTypes", bundle.getSymbolicName()))
    }

    private def unload() {
        unregisterService(thingTypeProvider)
        finished = false;
    }

    private void assertThatAllIsGood() {
        waitForAssert {
            assertThat thingRegistry.getAll().size(), is(1)
            Thing bridge = thingRegistry.get(new ThingUID("Xhue:Xbridge:myBridge"))
            assertThat bridge, is(notNullValue())
            assertThat bridge, is(instanceOf(Bridge))
            assertThat bridge.getStatus(), is(equalTo(ThingStatus.ONLINE))
        }
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with update_factory_loaded'() {
        assertThat thingRegistry.getAll().size(), is(0)
        updateModel()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThat thingRegistry.getAll().size(), is(0)
        registerThingTypeProvider()
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
        registerThingTypeProvider()
        finishLoading()
        assertThatAllIsGood()
    }

    @Test
    void 'assert that things are created only once the bundle finished loading with loaded_factory_update'() {
        assertThat thingRegistry.getAll().size(), is(0)
        registerThingTypeProvider()
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
        registerThingTypeProvider()
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
        registerThingTypeProvider()
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
        registerThingTypeProvider()
        finishLoading()
        assertThat thingRegistry.getAll().size(), is(0)
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        assertThatAllIsGood()
    }

    @Test
    void 'assert that thing handlers are managed correctly on update with factory_loaded'() {
        prepareThingWithShutDownBundle()

        bridgeInitializeCounter = 0
        registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
        registerThingTypeProvider()
        assertThat thingRegistry.get(new ThingUID("Xhue:Xbridge:myBridge")).getHandler(), is(nullValue())
        finishLoading()
        waitForAssert {
            assertThat bridgeInitializeCounter >= 1, is(true)
        }
        assertThatAllIsGood()
    }
}
