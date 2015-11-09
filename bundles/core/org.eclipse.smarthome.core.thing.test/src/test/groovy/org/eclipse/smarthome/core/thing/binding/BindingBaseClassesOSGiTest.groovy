

/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.status.ConfigStatusCallback
import org.eclipse.smarthome.config.core.status.ConfigStatusInfo
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage
import org.eclipse.smarthome.config.core.status.ConfigStatusProvider
import org.eclipse.smarthome.config.core.status.ConfigStatusService
import org.eclipse.smarthome.config.core.validation.ConfigValidationException
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFilter
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

import com.google.common.collect.ImmutableSet

/**
 * Tests for {@link ManagedThingProvider}.
 * @author Oliver Libutzki - Initital contribution
 * @author Dennis Nobel - Added test for bridgeInitialized and bridgeDisposed callbacks
 * @auther Thomas Höfer - Added config status tests
 */
class BindingBaseClassesOSGiTest extends OSGiTest {


    ManagedThingProvider managedThingProvider
    ThingHandlerFactory thingHandlerFactory

    final static String BINDING_ID = "testBinding"
    final static String THING_TYPE_ID = "testThingType"
    final static ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_ID)
    final static String THING1_ID = "testThing1"
    final static String THING2_ID = "testThing2"

    @Before
    void setup() {
        registerVolatileStorageService()
        managedThingProvider = getService ManagedThingProvider
        assertThat managedThingProvider, is(notNullValue())
    }

    @After
    void teardown() {
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
    }

    class SimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new SimpleThingHandler(thing)
        }
    }

    class SimpleThingHandler extends BaseThingHandler {

        SimpleThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            // check getBridge works
            assertThat getBridge().getUID().toString(), is("bindingId:type1:bridgeId")
        }
    }


    @Test
    void 'assert BaseThingHandlerFactory registers handler and BaseThingHandlers getBridge works'() {

        def componentContext = [getBundleContext: { bundleContext }] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def bridge = BridgeBuilder.create(new ThingUID("bindingId:type1:bridgeId")).build()
        def thing = ThingBuilder.create(new ThingUID("bindingId:type2:thingId")).withBridge(bridge.getUID()).build()

        managedThingProvider.add(bridge)
        managedThingProvider.add(thing)

        def handler = thing.getHandler()
        assertThat handler, is(not(null))

        // check that the handler is registered as OSGi service
        def handlerOsgiService = getService(ThingHandler, {
            it.getProperty(ThingHandler.SERVICE_PROPERTY_THING_ID).toString() == "bindingId:type2:thingId"
        })
        assertThat handlerOsgiService, is(handler)

        // check that base thing handler does not implement config status provider
        def configStatusProviderOsgiService = getService(ConfigStatusProvider)
        assertThat configStatusProviderOsgiService, is(null)

        // the assertion is in handle command
        handler.handleCommand(null, null)

        unregisterService(ThingHandlerFactory.class.name)
        thingHandlerFactory.deactivate(componentContext)
    }

    @Test
    void 'assert BaseThingHandlerFactory registers config status provider'() {

        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new ConfigStatusProviderThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()

        managedThingProvider.add(thing)

        def handler = thing.getHandler()
        assertThat handler, is(not(null))

        // check that the config status provider is registered as OSGi service
        def configStatusProviderOsgiService = getService(ConfigStatusProvider)
        assertThat configStatusProviderOsgiService, is(handler)

        unregisterService(ThingHandlerFactory.class.name)
        thingHandlerFactory.deactivate(componentContext)
    }

    @Test
    void 'assert config status is propagated'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new ConfigStatusProviderThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        ThingUID thingUID = new ThingUID("bindingId:type:thingId")

        def thing = ThingBuilder.create(thingUID).build()

        managedThingProvider.add(thing)

        getService(ConfigStatusService)

        EventSubscriber eventSubscriber = new EventSubscriber() {
                    Event event;
                    @Override
                    public Set<String> getSubscribedEventTypes() {
                        return ImmutableSet.copyOf("ConfigStatusInfoEvent")
                    }

                    @Override
                    public EventFilter getEventFilter() {
                        return new EventFilter() {
                                    boolean apply(Event event) {
                                        event.getTopic().equals(ThingConfigStatusSource.TOPIC.replace("{thingUID}", thingUID.getAsString()))
                                    };
                                };
                    }

                    @Override
                    public void receive(Event event) {
                        this.event = event;
                    }
                }
        registerService(eventSubscriber, EventSubscriber.class.getName())

        ThingRegistry thingRegistry = getService(ThingRegistry)

        thingRegistry.updateConfiguration(thingUID, ["param":"invalid"])

        waitForAssert({
            Event event = eventSubscriber.event
            assertThat event, is(notNullValue())
            assertThat event.getPayload(), containsString("\"parameterName\":\"param\",\"type\":\"ERROR\",\"message\":\"param invalid\"}")
            eventSubscriber.event = null
        }, 2500)

        thingRegistry.updateConfiguration(thingUID, ["param":"ok"])

        waitForAssert({
            Event event = eventSubscriber.event
            assertThat event, is(notNullValue())
            assertThat event.getPayload(), containsString("\"parameterName\":\"param\",\"type\":\"INFORMATION\",\"message\":\"param ok\"}")
        }, 2500)
    }

    class AnotherSimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new AnotherSimpleThingHandler(thing)
        }
    }

    def bridgeInitCalled = false;
    def bridgeDisposedCalled = false;

    class AnotherSimpleThingHandler extends BaseThingHandler {

        public AnotherSimpleThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        protected void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
            updateStatus(ThingStatus.ONLINE)
            bridgeInitCalled = true
        }

        @Override
        protected void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
            updateStatus(ThingStatus.OFFLINE)
            bridgeDisposedCalled = true
        }
    }


    @Test
    void 'assert bridgeInitialized is called by BaseThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new AnotherSimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def bridge = BridgeBuilder.create(new ThingUID("bindingId:type1:bridgeId")).build()
        def thing = ThingBuilder.create(new ThingUID("bindingId:type2:thingId")).withBridge(bridge.getUID()).build()

        // add thing first
        managedThingProvider.add(thing)
        managedThingProvider.add(bridge)

        assertThat bridgeInitCalled, is(true)
        assertThat bridgeDisposedCalled, is(false)
        assertThat thing.status, is(ThingStatus.ONLINE)

        // remove bridge
        managedThingProvider.remove(bridge.UID)

        assertThat bridgeDisposedCalled, is(true)
        assertThat thing.status, is(ThingStatus.OFFLINE)

        managedThingProvider.remove(thing.UID)
        bridgeInitCalled = false
        bridgeDisposedCalled = false

        // add bridge first
        managedThingProvider.add(bridge)
        managedThingProvider.add(thing)

        assertThat bridgeInitCalled, is(true)
    }

    class ConfigStatusProviderThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new ConfigStatusProviderThingHandler(thing)
        }
    }

    class ConfigStatusProviderThingHandler extends BaseThingHandler implements ConfigStatusProvider {

        private static final String PARAM = "param"
        private static final ConfigStatusMessage ERROR = ConfigStatusMessage.Builder.error(PARAM, "param invalid").build()
        private static final ConfigStatusMessage INFO = ConfigStatusMessage.Builder.information(PARAM, "param ok").build()
        private ConfigStatusCallback configStatusCallback;

        ConfigStatusProviderThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            // not implemented
        }

        @Override
        protected void updateConfiguration(Configuration configuration) {
            super.updateConfiguration(configuration);
            if(ConfigStatusCallback != null) {
                configStatusCallback.configUpdated(new ThingConfigStatusSource(getThing().getUID().asString))
            }
        }

        @Override
        public ConfigStatusInfo getConfigStatus(Locale locale) {
            if("invalid".equals(getThing().getConfiguration().get(PARAM))) {
                return new ConfigStatusInfo([ERROR])
            }
            return new ConfigStatusInfo([INFO])
        }

        @Override
        public boolean supportsEntity(String entityId) {
            return entityId.equals(getThing().getUID().asString)
        }

        @Override
        public void setConfigStatusCallback(ConfigStatusCallback configStatusCallback) {
            this.configStatusCallback = configStatusCallback
        }
    }

    class YetAnotherThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new YetAnotherThingHandler(thing)
        }
    }

    class YetAnotherThingHandler extends BaseThingHandler {

        YetAnotherThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void initialize() {
            super.initialize()
            ThingBuilder thingBuilder = editThing()
            thingBuilder.withChannels([
                new Channel(new ChannelUID("bindingId:type:thingId:1"), "String")
            ])
            updateThing(thingBuilder.build())
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {

        }

        public updateConfig() {
            Configuration configuration = editConfiguration()
            configuration.put("key", "value")
            updateConfiguration(configuration)
        }

        public updateProperties() {
            def properties = editProperties()
            properties.put(Thing.PROPERTY_MODEL_ID, "1234")
            updateProperties(properties)
        }

        public updateProperty() {
            updateProperty(Thing.PROPERTY_VENDOR, "vendor")
        }
    }

    @Test
    void 'assert thing can be updated from ThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()
            assertThat thing.channels.size(), is(0)
            managedThingProvider.add(thing)
            assertThat thingUpdated, is(true)
            assertThat updatedThing.channels.size(), is(1)

            updatedThing.getHandler().updateConfig()
            assertThat updatedThing.getConfiguration().get("key"), is("value")
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'assert properties can be updated from ThingHandler'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()

            managedThingProvider.add(thing)

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_MODEL_ID), is(null)
            assertThat updatedThing.getProperties().get(Thing.PROPERTY_VENDOR), is(null)

            updatedThing.getHandler().updateProperties()

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_MODEL_ID), is("1234")

            updatedThing.getHandler().updateProperty()

            assertThat updatedThing.getProperties().get(Thing.PROPERTY_VENDOR), is("vendor")
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'assert configuration will be updated by default implementation'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null
        ThingRegistry thingRegistry = getService(ThingRegistry)

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thingUID = new ThingUID("bindingId:type:thingId")
            def thing = ThingBuilder.create(thingUID).build()

            managedThingProvider.add(thing)

            thingRegistry.updateConfiguration(thingUID, [parameter: 'value'] as Map)

            assertThat updatedThing.getConfiguration().get('parameter'), is('value')
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test(expected=ConfigValidationException)
    void 'assert configuration parameters are validated'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        registerThingTypeAndConfigDescription()

        ThingRegistry thingRegistry = getService(ThingRegistry)

        def thingUID = new ThingUID("bindingId:type:thingId")
        def thing = ThingBuilder.create(thingUID).build()

        managedThingProvider.add(thing)

        thingRegistry.updateConfiguration(thingUID, [parameter: null] as Map)
    }

    private void registerThingTypeAndConfigDescription() {
        def URI configDescriptionUri = new URI("test:test");
        def thingType = new ThingType(new ThingTypeUID(BINDING_ID, THING_TYPE_ID), null, "label", null, null, null, null, configDescriptionUri)
        def configDescription = new ConfigDescription(configDescriptionUri,
                [
                    ConfigDescriptionParameterBuilder.create("parameter", ConfigDescriptionParameter.Type.TEXT).withRequired(true).build()] as List);

        registerService([
            getThingType: {thingTypeUID,locale -> thingType }
        ] as ThingTypeProvider)

        registerService([
            getThingType:{thingTypeUID -> thingType}
        ] as ThingTypeRegistry)

        registerService([
            getConfigDescription: {uri, locale -> configDescription}
        ] as ConfigDescriptionProvider)
    }
}
