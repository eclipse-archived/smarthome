/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ntp.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import java.text.ParseException
import java.text.SimpleDateFormat

import org.apache.commons.lang.StringUtils
import org.eclipse.smarthome.binding.ntp.NtpBindingConstants
import org.eclipse.smarthome.binding.ntp.handler.NtpHandler
import org.eclipse.smarthome.binding.ntp.internal.NtpHandlerFactory
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber
import org.eclipse.smarthome.core.library.items.DateTimeItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.types.DateTimeType
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

/**
 * OSGi tests for the {@link NtpHandler}
 *
 * @author Petar Valchev
 *
 */
class NtpOSGiTest extends OSGiTest {
    private static TimeZone systemTimeZone
    private static Locale locale

    private EventSubscriberMock eventSubscriberMock

    private NtpHandler ntpHandler
    private Thing ntpThing
    private GenericItem testItem

    private ManagedThingProvider managedThingProvider
    private ThingRegistry thingRegistry
    private ItemRegistry itemRegistry

    private def static final DEFAULT_TIME_ZONE_ID = "Europe/Helsinki"
    private def final TEST_TIME_ZONE_ID = "America/Los_Angeles"

    private def final TEST_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z"

    private def final TEST_ITEM_NAME = "testItem"
    private def final TEST_THING_ID = "testThingId"

    // No bundle in ESH is exporting a package from which we can use item types as constants, so we will use String.
    private def final ACCEPTED_ITEM_TYPE_STRING = "String"
    private def final ACCEPTED_ITEM_TYPE_DATE_TIME = "DateTime"

    enum UpdateEventType{
        HANDLE_COMMAND("handleCommand"), CHANNEL_LINKED("channelLinked");

        private String updateEventType

        public UpdateEventType(String updateEventType){
            this.updateEventType = updateEventType
        }

        public String getUpdateEventType(){
            return updateEventType
        }
    }

    @BeforeClass
    public static void setUpClass(){
        /* Store the initial system time zone and locale value,
         so that we can restore them at the test end.*/
        systemTimeZone = TimeZone.getDefault()
        locale = Locale.getDefault()

        /* Set new default time zone and locale,
         which will be used during the tests execution.*/
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID))
        Locale.setDefault(Locale.US)
    }

    @Before
    public void setUp(){
        VolatileStorageService volatileStorageService = new VolatileStorageService()
        registerService(volatileStorageService)

        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat "Could not get ManagedThingProvider",
                managedThingProvider,
                is(notNullValue())

        thingRegistry = getService(ThingRegistry)
        assertThat "Could not get ThingRegistry",
                thingRegistry,
                is(notNullValue())

        itemRegistry = getService(ItemRegistry)
        assertThat "Could not get ItemRegistry",
                itemRegistry,
                is(notNullValue())
    }

    @After
    public void tearDown(){
        if(ntpThing != null){
            Thing removedThing = thingRegistry.forceRemove(ntpThing.getUID())
            assertThat("The ntp thing was not deleted",
                    removedThing,
                    is(notNullValue()))
        }

        if(testItem != null) {
            itemRegistry.remove(TEST_ITEM_NAME)
        }
    }

    @AfterClass
    public static void tearDownClass(){
        // Set the default time zone and locale to their initial value.
        TimeZone.setDefault(systemTimeZone)
        Locale.setDefault(locale)
    }

    @Test
    public void 'the string channel is updated with the right time zone'(){
        def expectedTimeZonePDT = "PDT"
        def expectedTimeZonePST = "PST"

        Configuration configuration = new Configuration()
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID)

        Configuration channelConfig  = new Configuration()
        /* Set the format of the date, so it is updated in the item registry
         in a format from which we can easily get the time zone.*/
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, TEST_DATE_TIME_FORMAT)

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig)

        String timeZoneFromItemRegistry = getStringChannelTimeZoneFromItemRegistry()

        assertThat "The string channel was not updated with the right timezone",
                timeZoneFromItemRegistry,
                is(anyOf(equalTo(expectedTimeZonePDT), equalTo(expectedTimeZonePST)))
    }

    @Ignore("the dateTime channel is updated with a time from the system timezone")
    @Test
    public void 'the dateTime channel is updated with the right time zone'(){
        def expectedTimeZone = "-0700"

        Configuration configuration = new Configuration()
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID)

        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null)

        String testItemState = getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME).toString()
        /* There is no way to format the date in the dateTime channel
         in advance(there is no property for formatting in the dateTime channel),
         so we will rely on the format, returned by the toString() method of the DateTimeType.*/
        //FIXME: Adapt the tests if property for formatting in the dateTime channel is added.
        assertFormat(testItemState, DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS)
        /* Because of the format from the toString() method,
         the time zone will be the last five symbols of
         the string from the item registry(e.g. "+0300" or "-0700").*/
        String timeZoneFromItemRegistry = testItemState.substring(testItemState.length() - expectedTimeZone.length())

        assertThat "The dateTime channel was not updated with the right timezone",
                timeZoneFromItemRegistry,
                is(equalTo(expectedTimeZone))
    }

    @Ignore("the time zone in the calendar is lost after the serialization of the state")
    @Test
    public void 'the calendar of the dateTime channel is updated with the right time zone'(){
        Configuration configuration = new Configuration()
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID)

        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null)

        String timeZoneIdFromItemRegistry = ((DateTimeType)getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME)).getCalendar().getTimeZone().getID()

        assertThat "The dateTime channel calendar was not updated with the right timezone",
                timeZoneIdFromItemRegistry,
                is(equalTo(TEST_TIME_ZONE_ID))
    }

    @Test
    public void 'if no time zone is set in the configuration, the string channel is updated with the default one'(){
        def expectedTimeZoneEEST = "EEST"
        def expectedTimeZoneEET = "EET"

        Configuration configuration = new Configuration()

        Configuration channelConfig  = new Configuration()
        /* Set the format of the date, so it is updated in the item registry
         in a format from which we can easily get the time zone.*/
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, TEST_DATE_TIME_FORMAT)

        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null)

        String timeZoneFromItemRegistry = getStringChannelTimeZoneFromItemRegistry()

        assertThat "The string channel was not updated with the right timezone",
                timeZoneFromItemRegistry,
                is(anyOf(equalTo(expectedTimeZoneEEST), equalTo(expectedTimeZoneEET)))
    }

    @Test
    public void 'if no time zone is set in the configuration, the dateTime channel is updated with the default one'(){
        Calendar systemCalendar = Calendar.getInstance()
        String expectedTimeZone = getDateTimeChannelTimeZone(new DateTimeType(systemCalendar).toString())

        Configuration configuration = new Configuration()

        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null)

        String testItemState = getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME).toString()
        /* There is no way to format the date in the dateTime channel
         in advance(there is no property for formatting in the dateTime channel),
         so we will rely on the format, returned by the toString() method of the DateTimeType.*/
        //FIXME: Adapt the tests if property for formatting in the dateTime channel is added.
        assertFormat(testItemState, DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS)

        String timeZoneFromItemRegistry = getDateTimeChannelTimeZone(testItemState)

        assertThat "The dateTime channel was not updated with the right timezone",
                timeZoneFromItemRegistry,
                is(equalTo(expectedTimeZone))
    }

    @Test
    public void 'if no time zone is set in the configuration, the calendar of the dateTime channel is updated with the default one'(){
        Configuration configuration = new Configuration()

        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null)

        String timeZoneIdFromItemRegistry = ((DateTimeType)getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME)).getCalendar().getTimeZone().getID()

        assertThat "The dateTime channel calendar was not updated with the right timezone",
                timeZoneIdFromItemRegistry,
                is(equalTo(DEFAULT_TIME_ZONE_ID))
    }

    @Test
    public void 'the string channel is updated with the right formatting'(){
        def formatPattern = "EEE, d MMM yyyy HH:mm:ss Z"

        Configuration configuration = new Configuration()

        Configuration channelConfig  = new Configuration()
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, formatPattern)

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig)

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString()

        assertFormat(dateFromItemRegistry, formatPattern)
    }

    @Test
    public void 'if no property for formatting is set in the configuration of the string channel, the default formatting is used'(){
        Configuration configuration = new Configuration()

        // Initialize with configuration with no property for formatting set.
        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null)

        String dateFromItemRegistryString = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString()

        assertFormat(dateFromItemRegistryString, NtpHandler.DATE_PATTERN_WITH_TZ)
    }

    @Test
    public void 'if the property for formatting in the configuration of the string channel is empty string, the default formatting is used'(){
        Configuration configuration = new Configuration()

        Configuration channelConfig  = new Configuration()
        // Empty string
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, "")

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig)

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString()

        assertFormat(dateFromItemRegistry, NtpHandler.DATE_PATTERN_WITH_TZ)
    }

    @Test
    public void 'if the property for formatting in the configuration of the string channel is null, the default formatting is used'(){
        Configuration configuration = new Configuration()

        Configuration channelConfig  = new Configuration()
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, null)

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig)

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString()

        assertFormat(dateFromItemRegistry, NtpHandler.DATE_PATTERN_WITH_TZ)
    }

    @Test
    public void 'the status of a thing with dateTime channel is updated with communication error, when unknown host is set in the configuraion'(){
        assertCommunicationError(ACCEPTED_ITEM_TYPE_DATE_TIME)
    }

    @Test
    public void 'the status of a thing with string channel is updated with communication error, when unknown host is set in the configuraion'(){
        assertCommunicationError(ACCEPTED_ITEM_TYPE_STRING)
    }

    @Test
    public void 'the string channel is updated on handleCommand'(){
        assertEventIsReceived(UpdateEventType.HANDLE_COMMAND, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING)
    }

    @Test
    public void 'the dateTime channel is updated on handleCommand'(){
        assertEventIsReceived(UpdateEventType.HANDLE_COMMAND, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME)
    }

    @Test
    public void 'time is refreshed when string channel is linked'(){
        assertEventIsReceived(UpdateEventType.CHANNEL_LINKED, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING)
    }

    @Test
    public void 'time is refreshed when dateTime channel is linked'(){
        assertEventIsReceived(UpdateEventType.CHANNEL_LINKED, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME)
    }

    private void initialize(Configuration configuration, String channelID, String acceptedItemType, Configuration channelConfiguration){
        ThingUID ntpUid = new ThingUID(NtpBindingConstants.THING_TYPE_NTP, TEST_THING_ID)

        ChannelUID channelUID = new ChannelUID(ntpUid, channelID)
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(NtpBindingConstants.BINDING_ID, channelUID.getIdWithoutGroup())
        Channel channel
        if(channelConfiguration != null){
            channel = new Channel(channelUID, acceptedItemType, channelConfiguration)
        } else {
            channel = new Channel(channelUID, acceptedItemType)
        }

        ntpThing = ThingBuilder.create(NtpBindingConstants.THING_TYPE_NTP, ntpUid)
                .withConfiguration(configuration)
                .withChannel(channel)
                .build()

        managedThingProvider.add(ntpThing)

        NtpHandlerFactory factory
        waitForAssert({
            factory = getNtpHandlerFactory()
            assertThat factory, not(null)
        })

        // Wait for the NTP thing to be added to the ManagedThingProvider.
        waitForAssert({
            ntpHandler = getNtpHandler(factory)
            assertThat "Could not get NtpHandler",
                    ntpHandler,
                    is(notNullValue())
        })

        if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)){
            testItem = new StringItem(TEST_ITEM_NAME)
        } else if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)){
            testItem = new DateTimeItem(TEST_ITEM_NAME)
        }

        itemRegistry.add(testItem)

        def ManagedItemChannelLinkProvider itemChannelLinkProvider

        // Wait for the item , linked to the NTP thing to be added to the ManagedThingProvider.
        waitForAssert({
            itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
            assertThat "Could not get ManagedItemChannelLinkProvider",
                    itemChannelLinkProvider,
                    is(notNullValue())
        })
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_ITEM_NAME, channelUID))
    }

    private NtpHandlerFactory getNtpHandlerFactory() {
        NtpHandlerFactory factory
        waitForAssert({
            factory = getService(ThingHandlerFactory, NtpHandlerFactory)
            assertThat factory, is(notNullValue())
        }, 10000)
        factory
    }

    private NtpHandler getNtpHandler(NtpHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
        assertThat thingManager, not(null)
        def handlers = thingManager.thingHandlersByFactory.get(factory)

        for(ThingHandler handler : handlers) {
            if(handler instanceof NtpHandler) {
                return handler
            }
        }
        null
    }

    private State getItemState(String acceptedItemType){
        Item testItem
        waitForAssert({
            testItem = itemRegistry.getItem(TEST_ITEM_NAME)
            assertThat "The item was null",
                    testItem,
                    is(notNullValue())
        })

        State testItemState
        waitForAssert({
            testItemState = testItem.getState()
            if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)){
                assertThat "The item was not of type StringType",
                        testItemState,
                        is(instanceOf(StringType))
            } else if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)){
                assertThat "The item was not of type DateTimeType",
                        testItemState,
                        is(instanceOf(DateTimeType))
            }
        }, 30000, 100)

        return testItemState
    }

    private String getDateTimeChannelTimeZone(String date){
        /* Because of the format from the toString() method,
         the time zone will be the last five symbols of
         the string from the item registry(e.g. "+0300" or "-0700").*/
        return date.substring(date.length() - 5)
    }

    private String getStringChannelTimeZoneFromItemRegistry(){
        String itemState = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString()
        /* This method is used only in tests for the string channel,
         where we have set the format for the date in advance.
         Because of that format, we know that the time zone will be the
         last word of the string from the item registry.*/
        // FIXME: This can happen a lot easier with Java 8 date time API, so tests can be adapted, if there is an upgrade to Java 8
        String timeZoneFromItemRegistry = StringUtils.substringAfterLast(itemState, " ")
        return timeZoneFromItemRegistry
    }

    private void assertFormat(String initialDate, String formatPattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatPattern)

        Date date
        try{
            date = dateFormat.parse(initialDate)
        } catch (ParseException e){
            fail("An exception $e was thrown, while trying to parse the date $initialDate")
        }

        String formattedDate = dateFormat.format(date)

        assertThat "The default formatting was not used",
                formattedDate,
                is(equalTo(initialDate))
    }

    private void assertCommunicationError(String acceptedItemType){
        Configuration configuration = new Configuration()
        configuration.put(NtpBindingConstants.PROPERTY_NTP_SERVER, "wrong.hostname")

        if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)){
            initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null)
        } else if(acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)){
            initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null)
        }

        waitForAssert({
            assertThat "The thing status was not communication error",
                    ntpThing.getStatusInfo().getStatusDetail(),
                    is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR))
        })
    }

    private void assertEventIsReceived(UpdateEventType updateEventType, String channelID, String acceptedItemType){
        Configuration configuration = new Configuration()

        initialize(configuration, channelID, acceptedItemType, null)

        eventSubscriberMock = new EventSubscriberMock()
        registerService(eventSubscriberMock, EventSubscriber.class.getName())

        if(updateEventType.equals(UpdateEventType.HANDLE_COMMAND)){
            ntpHandler.handleCommand(null, null)
        } else if(updateEventType.equals(UpdateEventType.CHANNEL_LINKED)){
            ntpHandler.channelLinked(null)
        }
        waitForAssert({
            assertThat "The $channelID channel was not updated on ${updateEventType.getUpdateEventType()} method",
                    eventSubscriberMock.isEventReceived,
                    is(true)
        })
    }

    private class EventSubscriberMock extends AbstractItemEventSubscriber{
        public boolean isEventReceived = false

        @Override
        public void receive(Event event) {
            isEventReceived = true
        }
    }
}
