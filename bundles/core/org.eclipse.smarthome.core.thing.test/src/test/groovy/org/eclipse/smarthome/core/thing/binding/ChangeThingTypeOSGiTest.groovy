/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.internal.ThingManager
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelGroupType
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Simon Kaufmann - initial contribution
 */
class ChangeThingTypeOSGiTest extends OSGiTest {


    def ManagedThingProvider managedThingProvider
    def ThingHandlerFactory thingHandlerFactory
    def boolean selfChanging = false

    final static String BINDING_ID = "testBinding"
    final static String THING_TYPE_GENERIC_ID = "generic"
    final static String THING_TYPE_SPECIFIC_ID = "specific"
    final static ThingTypeUID THING_TYPE_GENERIC_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_GENERIC_ID)
    final static ThingTypeUID THING_TYPE_SPECIFIC_UID = new ThingTypeUID(BINDING_ID, THING_TYPE_SPECIFIC_ID)
    final static String THING_ID = "testThing"
    final static ChannelUID CHANNEL_GENERIC_UID = new ChannelUID(BINDING_ID + "::" + THING_ID + ":" + "channel" + THING_TYPE_GENERIC_ID)
    final static ChannelUID CHANNEL_SPECIFIC_UID = new ChannelUID(BINDING_ID + "::" + THING_ID + ":" + "channel" + THING_TYPE_SPECIFIC_ID)
    final static String ITEM_GENERIC = "item" + THING_TYPE_GENERIC_ID;
    final static String ITEM_SPECIFIC = "item" + THING_TYPE_SPECIFIC_ID;
    final static ItemChannelLink ITEM_CHANNEL_LINK_GENERIC = new ItemChannelLink(ITEM_GENERIC,  CHANNEL_GENERIC_UID)
    final static ItemChannelLink ITEM_CHANNEL_LINK_SPECIFIC = new ItemChannelLink(ITEM_SPECIFIC,  CHANNEL_SPECIFIC_UID)

    def Map<ThingTypeUID, ThingType> thingTypes = new HashMap<>()
    def Map<URI, ConfigDescription> configDescriptions = new HashMap<>()
    def Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>()
    def Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypes = new HashMap<>()
    def ComponentContext componentContext
    def ConfigDescriptionRegistry configDescriptionRegistry
    def ManagedItemChannelLinkProvider managedItemChannelLinkProvider
    def ManagedItemProvider managedItemProvider
    def ThingRegistry thingRegistry

    def ThingType thingTypeGeneric
    def ThingType thingTypeSpecific

    def specificInits = 0;
    def genericInits = 0;
    def unregisterHandlerDelay = 0;


    @Before
    void setup() {
        registerVolatileStorageService()
        managedThingProvider = getService(ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        configDescriptionRegistry = getService(ConfigDescriptionRegistry)
        assertThat configDescriptionRegistry, is(notNullValue())

        managedItemChannelLinkProvider = getService ManagedItemChannelLinkProvider
        assertThat managedItemChannelLinkProvider, is(notNullValue())

        managedItemProvider = getService ManagedItemProvider
        assertThat managedItemProvider, is(notNullValue())

        componentContext = [getBundleContext: { bundleContext }] as ComponentContext
        thingHandlerFactory = new SampleThingHandlerFactory()
        thingHandlerFactory.activate(componentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

        thingTypeGeneric = registerThingTypeAndConfigDescription(THING_TYPE_GENERIC_UID)
        thingTypeSpecific = registerThingTypeAndConfigDescription(THING_TYPE_SPECIFIC_UID)

        registerService([
            getThingType: { aThingTypeUID,locale ->
                return thingTypes.get(aThingTypeUID)
            }
        ] as ThingTypeProvider)

        registerService([
            getThingType:{ aThingTypeUID ->
                return thingTypes.get(aThingTypeUID)
            }
        ] as ThingTypeRegistry)

        registerService([
            getConfigDescription: { uri, locale ->
                return configDescriptions.get(uri)
            }
        ] as ConfigDescriptionProvider)

        registerService([
            getChannelTypes: { channelTypes.values() },
            getChannelType: { ChannelTypeUID uid, Locale locale ->
                channelTypes.get(uid)
            },
            getChannelGroupTypes: { channelGroupTypes.values() },
            getChannelGroupType: { ChannelGroupTypeUID uid, Locale locale ->
                channelGroupTypes.get(uid)
            },
        ] as ChannelTypeProvider)

        managedItemProvider.add(new StringItem(ITEM_GENERIC))
        managedItemProvider.add(new StringItem(ITEM_SPECIFIC))

        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK_GENERIC
        managedItemChannelLinkProvider.add ITEM_CHANNEL_LINK_SPECIFIC
    }

    @After
    void teardown() {
        unregisterService(ThingHandlerFactory.class.name)
        unregisterService(ThingTypeProvider)
        unregisterService(ThingTypeRegistry)
        unregisterService(ConfigDescriptionProvider)
        unregisterService(ChannelTypeProvider)
        thingHandlerFactory.deactivate(componentContext)

        clearProviders()
        unregisterService(StorageService)
    }

    private void clearProviders() {
        getService(ManagedThingProvider).getAll().each {
            getService(ManagedThingProvider).remove(it.getUID())
        }

        getService(ManagedItemProvider).getAll().each {
            getService(ManagedItemProvider).remove(it.getName())
        }

        getService(ManagedItemChannelLinkProvider).getAll().each {
            getService(ManagedItemChannelLinkProvider).remove(it.getUID().toString())
        }
    }

    private Logger logger = LoggerFactory.getLogger(ChangeThingTypeOSGiTest);
    class SampleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            logger.debug("creating handler for thing '{}' of type '{}'", thing.getUID().asString, thing.getThingTypeUID().asString)
            if (thing.getThingTypeUID().equals(THING_TYPE_GENERIC_UID)) {
                return new GenericThingHandler(thing)
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_SPECIFIC_UID)) {
                return new SpecificThingHandler(thing)
            }
        }

        @Override
        public void unregisterHandler(Thing thing) {
            Thread.sleep(unregisterHandlerDelay);
            super.unregisterHandler(thing);
        }
    }

    class GenericThingHandler extends BaseThingHandler {

        GenericThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void initialize() {
            println "[ChangeThingTypeOSGiTest] GenericThingHandler.initialize"
            super.initialize()
            genericInits++;
            if (selfChanging) {
                changeThingType(THING_TYPE_SPECIFIC_UID, new Configuration(['providedspecific':'there']))
            }
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            println "[ChangeThingTypeOSGiTest] Generic Handle Command"
        }
    }

    class SpecificThingHandler extends BaseThingHandler {

        SpecificThingHandler(Thing thing) {
            super(thing)
        }


        @Override
        public void initialize() {
            println "[ChangeThingTypeOSGiTest] SpecificThingHandler.initialize"
            specificInits++;
            super.initialize();
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            println "[ChangeThingTypeOSGiTest] Specific Handle Command"
        }
    }

    @Test
    void 'assert changing the ThingType works'() {
        println "[ChangeThingTypeOSGiTest] ======== assert changing the ThingType works"
        def thing = ThingFactory.createThing(thingTypeGeneric, new ThingUID("testBinding", "testThing"), new Configuration(), null, configDescriptionRegistry)
        thing.setProperty("universal", "survives")
        managedThingProvider.add(thing)

        // Pre-flight checks - see below
        assertThat thing.getHandler(), isA(GenericThingHandler)
        assertThat thing.getConfiguration().get("parametergeneric"), is("defaultgeneric")
        assertThat thing.getConfiguration().get("providedspecific"), is(nullValue())
        assertThat thing.getChannels().size(), is(1)
        assertThat thing.getChannels().get(0).getUID(), is (CHANNEL_GENERIC_UID)
        assertThat thing.getProperties().get("universal"), is("survives")

        def handlerFactory = getService(ThingHandlerFactory.class, SampleThingHandlerFactory.class)
        assertThat handlerFactory, not(null)
        def handlers = getThingHandlers(handlerFactory)
        assertThat handlers.contains(thing.getHandler()), is(true)

        thing.getHandler().handleCommand(null, null)
        waitForAssert({
            assertThat thing.getStatus(), is(ThingStatus.ONLINE)
        }, 4000, 100)

        // Now do the actual migration
        thing.getHandler().changeThingType(THING_TYPE_SPECIFIC_UID, new Configuration(['providedspecific':'there']))

        assertThingWasChanged(thing)
    }

    @Test
    void 'assert changing thing type within initialize works'() {
        println "[ChangeThingTypeOSGiTest] ======== assert changing thing type within initialize works"
        selfChanging = true
        println "[ChangeThingTypeOSGiTest] Create thing"
        def thing = ThingFactory.createThing(thingTypeGeneric, new ThingUID("testBinding", "testThing"), new Configuration(), null, configDescriptionRegistry)
        thing.setProperty("universal", "survives")
        println "[ChangeThingTypeOSGiTest] Add thing to managed thing provider"
        managedThingProvider.add(thing)

        println "[ChangeThingTypeOSGiTest] Wait for thing changed"
        assertThingWasChanged(thing)
    }

    @Test
    void 'assert changing thing type within initialize works even if service deregistration is slow'() {
        println "[ChangeThingTypeOSGiTest] ======== assert changing thing type within initialize works even if service deregistration is slow"
        selfChanging = true
        unregisterHandlerDelay = 6000
        println "[ChangeThingTypeOSGiTest] Create thing"
        def thing = ThingFactory.createThing(thingTypeGeneric, new ThingUID("testBinding", "testThing"), new Configuration(), null, configDescriptionRegistry)
        thing.setProperty("universal", "survives")
        println "[ChangeThingTypeOSGiTest] Add thing to managed thing provider"
        managedThingProvider.add(thing)

        println "[ChangeThingTypeOSGiTest] Wait for thing changed"
        assertThingWasChanged(thing)
    }

    @Test
    void 'assert loading specialized thing type works directly'() {
        println "[ChangeThingTypeOSGiTest] ======== assert loading specialized thing type works directly"
        clearProviders()

        def storage = getService(StorageService)
        def persistedThing = ThingFactory.createThing(thingTypeSpecific, new ThingUID("testBinding", "persistedThing"), new Configuration(['providedspecific':'there']), null, null)
        persistedThing.setProperty("universal", "survives")
        storage.getStorage(Thing.class.getName()).put("testBinding::persistedThing", persistedThing)
        selfChanging = true

        unregisterService(storage)
        managedThingProvider = getService(ManagedThingProvider)
        assertThat managedThingProvider, is(nullValue())

        registerService(storage)
        managedThingProvider = getService(ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        def res = managedThingProvider.all
        assertThat res.size(), is(1)

        def thing = res.iterator().next()
        assertThat thing.getUID().asString, is("testBinding::persistedThing")

        // Ensure that the ThingHandler has been registered as an OSGi service correctly
        waitForAssert({
            assertThat thing.getHandler(), isA(SpecificThingHandler)
        }, 4000, 100)
        def handlerFactory = getService(ThingHandlerFactory.class, SampleThingHandlerFactory.class)
        assertThat handlerFactory, not(null)
        def handlers = getThingHandlers(handlerFactory)
        assertThat handlers.contains(thing.getHandler()), is(true)

        // Ensure it's initialized
        waitForAssert {
            assertThat(specificInits, is(1))
            assertThat(genericInits, is(0))
        }

        // Ensure the Thing is ONLINE again
        assertThat thing.getStatus(), is(ThingStatus.ONLINE)
    }


    private void assertThingWasChanged(Thing thing) {
        // Ensure that the ThingHandler has been registered as an OSGi service correctly
        waitForAssert({
            assertThat thing.getHandler(), isA(SpecificThingHandler)
        }, 30000, 100)

        def handlerFactory = getService(ThingHandlerFactory.class, SampleThingHandlerFactory.class)
        assertThat handlerFactory, not(null)
        def handlers = getThingHandlers(handlerFactory)
        assertThat handlers.contains(thing.getHandler()), is(true)

        // Ensure it's initialized
        waitForAssert({
            assertThat(specificInits, is(1))
            assertThat(genericInits, is(1))
        }, 4000)


        thing.getHandler().handleCommand(null, null)

        //Ensure that the provided configuration has been applied and default values have been added
        assertThat thing.getConfiguration().get("parameterspecific"), is("defaultspecific")
        assertThat thing.getConfiguration().get("parametergeneric"), is(nullValue())
        assertThat thing.getConfiguration().get("providedspecific"), is("there")

        // Ensure that the new set of channels is there
        assertThat thing.getChannels().size(), is(1)
        assertThat thing.getChannels().get(0).getUID().getId(), containsString("specific")

        // Ensure that the properties are still there
        assertThat thing.getProperties().get("universal"), is("survives")

        // Ensure the new thing type got written correctly into the storage
        assertThat managedThingProvider.get(new ThingUID("testBinding", "testThing")).getThingTypeUID(), is(THING_TYPE_SPECIFIC_UID)

        // Ensure the Thing is ONLINE again
        assertThat thing.getStatus(), is(ThingStatus.ONLINE)

        // Ensure the new thing type has been persisted into the database
        def storage = getService(StorageService).getStorage(Thing.class.getName())
        def Thing persistedThing = storage.get("testBinding::testThing")
        assertThat persistedThing.getThingTypeUID().getAsString(), is("testBinding:specific")
    }

    private ThingType registerThingTypeAndConfigDescription(ThingTypeUID thingTypeUID) {
        def URI configDescriptionUri = new URI("test:" + thingTypeUID.getId());
        def thingType = new ThingType(thingTypeUID, null, "label", null, getChannelDefinitions(thingTypeUID), null, null, configDescriptionUri)
        def configDescription = new ConfigDescription(configDescriptionUri, [
            ConfigDescriptionParameterBuilder.create("parameter"+thingTypeUID.getId(), ConfigDescriptionParameter.Type.TEXT).withRequired(false).withDefault("default"+thingTypeUID.getId()).build(),
            ConfigDescriptionParameterBuilder.create("provided"+thingTypeUID.getId(), ConfigDescriptionParameter.Type.TEXT).withRequired(false).build()
        ] as List);

        thingTypes.put(thingTypeUID, thingType)
        configDescriptions.put(configDescriptionUri, configDescription)

        return thingType
    }

    private List getChannelDefinitions(ThingTypeUID thingTypeUID){
        List channelDefinitions = new ArrayList<ChannelDefinition>()
        def channelTypeUID = new ChannelTypeUID("test:"+thingTypeUID.getId())
        def channelType = new ChannelType(channelTypeUID, false, "itemType", "channelLabel", "description", "category", new HashSet<String>(), null, new URI("scheme", "channelType:"+thingTypeUID.getId(), null))

        channelTypes.put(channelTypeUID, channelType)

        def cd = new ChannelDefinition("channel"+thingTypeUID.getId(), channelTypeUID)
        channelDefinitions.add(cd)
        return channelDefinitions;
    }

    private Set<ThingHandler> getThingHandlers(ThingHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, ThingManager.class)
        assertThat thingManager, not(null)
        thingManager.thingHandlersByFactory.get(factory)
    }
}
