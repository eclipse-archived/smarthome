/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


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
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.core.status.ConfigStatusCallback
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage
import org.eclipse.smarthome.config.core.status.ConfigStatusProvider
import org.eclipse.smarthome.config.core.status.ConfigStatusService
import org.eclipse.smarthome.config.core.validation.ConfigValidationException
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventFilter
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.i18n.I18nProvider
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
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
    ThingRegistry thingRegistry

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
        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())
    }

    @After
    void teardown() {
        managedThingProvider.getAll().each {
            managedThingProvider.remove(it.getUID())
        }
    }

    class SimpleThingHandlerFactory extends BaseThingHandlerFactory {
        def handlers = new HashSet<ThingHandler>()

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            def handler = (thing instanceof Bridge) ? new SimpleBridgeHandler(thing) : new SimpleThingHandler(thing)
            handlers.add(handler)
            handler
        }

        public Set<ThingHandler> getHandlers() {
            handlers
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

    class SimpleBridgeHandler extends BaseBridgeHandler {

        SimpleBridgeHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        public void updateBridgetatus(ThingStatus status) {
            updateStatus(status)
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

        def handler = null
        waitForAssert {
            handler = thing.getHandler()
            assertThat handler, is(not(null))
        }

        def retrievedHandler = getThingHandler(thingHandlerFactory, SimpleThingHandler)
        assertThat retrievedHandler, is(handler)

        // check that base thing handler does not implement config status provider
        waitForAssert {
            def configStatusProviderOsgiService = getService(ConfigStatusProvider)
            assertThat configStatusProviderOsgiService, is(null)
        }

        // the assertion is in handle command
        handler.handleCommand(null, null)

        unregisterService(ThingHandlerFactory.class.name)
        thingHandlerFactory.deactivate(componentContext)
    }

    @Test
    void 'assert BaseThingHandlerFactory registers config status provider'() {

        def componentContext = [getBundleContext: { bundleContext }] as ComponentContext
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
        def componentContext = [getBundleContext: { bundleContext }] as ComponentContext
        def thingHandlerFactory = new ConfigStatusProviderThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        ThingUID thingUID = new ThingUID("bindingId:type:thingId")

        def thing = ThingBuilder.create(thingUID).build()

        managedThingProvider.add(thing)

        ConfigStatusService service = getService(ConfigStatusService)
        service.setI18nProvider([
            getText: { bundle, key, defaultText, locale, args ->
                key.endsWith("param.invalid") ? "param invalid" : "param ok"
            }
        ]  as I18nProvider)

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

        thing.getHandler().handleConfigurationUpdate(["param":"invalid"])

        waitForAssert({
            Event event = eventSubscriber.event
            assertThat event, is(notNullValue())
            assertThat event.getPayload(), containsString("\"parameterName\":\"param\",\"type\":\"ERROR\",\"message\":\"param invalid\"}")
            eventSubscriber.event = null
        }, 2500)

        thing.getHandler().handleConfigurationUpdate(["param":"ok"])

        waitForAssert({
            Event event = eventSubscriber.event
            assertThat event, is(notNullValue())
            assertThat event.getPayload(), containsString("\"parameterName\":\"param\",\"type\":\"INFORMATION\",\"message\":\"param ok\"}")
        }, 2500)
    }

    @Test
    void 'assert BaseThingHandler notifies ThingManager about configuration updates'() {
        // register ThingTypeProvider & ConfigurationDescription with 'required' parameter
        registerThingTypeProvider()
        registerConfigDescriptionProvider(true)

        // register thing handler factory
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate([getBundleContext: {bundleContext}] as ComponentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUID = new ThingUID(THING_TYPE_UID, "thingId")
        def thing = ThingBuilder.create(thingUID).build()

        // add thing with empty configuration
        managedThingProvider.add(thing)

        // ThingHandler.initialize() has not been called; thing with status UNINITIALIZED.HANDLER_CONFIGURATION_PENDING
        def statusInfo = ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED,
                ThingStatusDetail.HANDLER_CONFIGURATION_PENDING).build()
        assertThat thing.getStatusInfo(), is(statusInfo)

        thingRegistry.updateConfiguration(thingUID, [parameter: "value"] as Map)

        // ThingHandler.initialize() has been called; thing with status ONLINE.NONE
        statusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build()
        waitForAssert({
            assertThat thing.getStatusInfo(), is(statusInfo)
        }, 4000)
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

    class ConfigStatusProviderThingHandler extends ConfigStatusThingHandler {

        private static final String PARAM = "param"
        private static final ConfigStatusMessage ERROR = ConfigStatusMessage.Builder.error(PARAM).withMessageKeySuffix("param.invalid").build()
        private static final ConfigStatusMessage INFO = ConfigStatusMessage.Builder.information(PARAM).withMessageKeySuffix("param.ok").build()
        private ConfigStatusCallback configStatusCallback;

        ConfigStatusProviderThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            // not implemented
        }

        @Override
        public Collection<ConfigStatusMessage> getConfigStatus() {
            if("invalid".equals(getThing().getConfiguration().get(PARAM))) {
                return [ERROR]
            }
            return [INFO]
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
        registerThingTypeProvider()
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null

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

            waitForAssert({
                assertThat thingUpdated, is(true)
                assertThat updatedThing.channels.size(), is(1)
            }, 4000)

            updatedThing.getHandler().updateConfig()
            assertThat updatedThing.getConfiguration().get("key"), is("value")
        } finally {
            thingRegistry.removeRegistryChangeListener(registryChangeListener)
        }
    }

    @Test
    void 'assert properties can be updated from ThingHandler'() {
        registerThingTypeProvider()
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new YetAnotherThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def thingUpdated = false
        Thing updatedThing = null

        def registryChangeListener = [
            added: {thing -> },
            updated: {old, updated ->
                thingUpdated = true; updatedThing = updated}
        ] as RegistryChangeListener

        try {
            thingRegistry.addRegistryChangeListener(registryChangeListener)
            def thing = ThingBuilder.create(new ThingUID("bindingId:type:thingId")).build()

            managedThingProvider.add(thing)

            waitForAssert({-> assertThat thingUpdated, is(true) }, 10000, 100)

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

            waitForAssert({-> assertThat thingUpdated, is(true) }, 10000, 100)

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

        registerThingTypeProvider()
        registerConfigDescriptionProvider(true)

        def thingUID = new ThingUID("bindingId:type:thingId")
        def thing = ThingBuilder.create(thingUID).build()

        managedThingProvider.add(thing)

        thingRegistry.updateConfiguration(thingUID, [parameter: null] as Map)
    }

    @Test
    void 'assert configuration is rolled-back on error'() {
        def componentContext = [getBundleContext: {bundleContext}] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        registerThingTypeAndConfigDescription()

        ThingRegistry thingRegistry = getService(ThingRegistry)

        def thingUID = new ThingUID("bindingId:type:thingId")
        def thing = ThingBuilder.create(thingUID).build()

        managedThingProvider.add(thing)

        // set the config to an initial value
        thingRegistry.updateConfiguration(thingUID, [parameter: "before"] as Map)
        assertThat thing.getConfiguration().get("parameter"), is("before")

        // let it fail next time...
        thing.getHandler().callback = [thingUpdated: {updatedThing -> throw new IllegalStateException()}] as ThingHandlerCallback
        try {
            thingRegistry.updateConfiguration(thingUID, [parameter: "after"] as Map)
            fail("There should have been an exception!")
        } catch (IllegalStateException e) {
            // all good, we want that
        }

        // now check if the thing's configuration has been rolled back
        assertThat thing.getConfiguration().get("parameter"), is("before")
    }

    @Test
    void 'assert BaseThingHandler handles bridge status updates correctly'() {
        def componentContext = [getBundleContext: { bundleContext }] as ComponentContext
        def thingHandlerFactory = new SimpleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        def bridge = BridgeBuilder.create(new ThingUID("bindingId:type1:bridgeId")).build()
        def thingA = ThingBuilder.create(new ThingUID("bindingId:type2:thingIdA")).withBridge(bridge.getUID()).build()
        def thingB = ThingBuilder.create(new ThingUID("bindingId:type2:thingIdB")).withBridge(bridge.getUID()).build()

        assertThat bridge.getStatus(), is(ThingStatus.UNINITIALIZED)
        assertThat thingA.getStatus(), is(ThingStatus.UNINITIALIZED)
        assertThat thingB.getStatus(), is(ThingStatus.UNINITIALIZED)

        managedThingProvider.add(bridge)
        managedThingProvider.add(thingA)
        managedThingProvider.add(thingB)

        waitForAssert({assertThat bridge.getStatus(), is(ThingStatus.ONLINE)})
        waitForAssert({assertThat thingA.getStatus(), is(ThingStatus.ONLINE)})
        waitForAssert({assertThat thingB.getStatus(), is(ThingStatus.ONLINE)})

        // set bridge status to OFFLINE
        def bridgeHandler = getThingHandler(thingHandlerFactory, SimpleBridgeHandler)
        assertThat bridgeHandler, not(null)
        bridgeHandler.updateBridgetatus(ThingStatus.OFFLINE)

        // child things are OFFLINE with detail BRIDGE_OFFLINE
        waitForAssert({assertThat bridge.getStatus(), is(ThingStatus.OFFLINE)})
        def thingStatusInfo = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE).build()
        waitForAssert({assertThat thingA.getStatusInfo(), is(thingStatusInfo)})
        waitForAssert({assertThat thingB.getStatusInfo(), is(thingStatusInfo)})

        // set bridge status to ONLINE
        bridgeHandler.updateBridgetatus(ThingStatus.ONLINE)

        // child things are ONLINE with detail NONE
        waitForAssert({assertThat bridge.getStatus(), is(ThingStatus.ONLINE)})
        thingStatusInfo = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()
        waitForAssert({assertThat thingA.getStatusInfo(), is(thingStatusInfo)})
        waitForAssert({assertThat thingB.getStatusInfo(), is(thingStatusInfo)})

        unregisterService(ThingHandlerFactory.class.name)
        thingHandlerFactory.deactivate(componentContext)
    }

    protected <T extends ThingHandler> T getThingHandler(SimpleThingHandlerFactory factory, Class<T> clazz){
        for(ThingHandler handler : factory.getHandlers()) {
            if(clazz.isInstance(handler)) {
                return handler
            }
        }
        return null
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


    private void registerThingTypeProvider() {
        def URI configDescriptionUri = new URI("test:test");
        def thingType = new ThingType(new ThingTypeUID(BINDING_ID, THING_TYPE_ID), null, "label", null, null, null, null, configDescriptionUri)

        registerService([
            getThingType: {thingTypeUID,locale -> thingType }
        ] as ThingTypeProvider)

        registerService([
            getThingType:{thingTypeUID -> thingType}
        ] as ThingTypeRegistry)
    }

    private void registerConfigDescriptionProvider(boolean withRequiredParameter = false) {
        def URI configDescriptionUri = new URI("test:test");
        def configDescription = new ConfigDescription(configDescriptionUri, [
            ConfigDescriptionParameterBuilder.create("parameter", ConfigDescriptionParameter.Type.TEXT).withRequired(withRequiredParameter).build()] as List);

        registerService([
            getConfigDescription: {uri, locale -> configDescription}
        ] as ConfigDescriptionProvider)
    }

}
