/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService

import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.*
import org.eclipse.smarthome.core.library.types.*
import org.eclipse.smarthome.core.thing.*
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder

import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.*

import org.osgi.service.http.HttpService

import org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants
import org.eclipse.smarthome.binding.fsinternetradio.internal.FSInternetRadioDiscoveryParticipant
import org.eclipse.smarthome.binding.fsinternetradio.internal.FSInternetRadioHandlerFactory;
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadioConnection
import org.eclipse.smarthome.binding.fsinternetradio.handler.FSInternetRadioHandler

import org.apache.commons.lang.StringUtils

/**
 * OSGi tests for the {@link FSInternetRadioHandler}
 *
 * @author Mihaela Memova
 *
 */
public class FSInternetRadioHandlerOSGiTest extends OSGiTest{

	def DEFAULT_TEST_THING_NAME = "testRadioThing"
	def DEFAULT_TEST_ITEM_NAME = "testItem"

	def DEFAULT_THING_TYPE_UID = FSInternetRadioBindingConstants.THING_TYPE_RADIO
	def DEFAULT_THING_UID = new ThingUID(DEFAULT_THING_TYPE_UID, DEFAULT_TEST_THING_NAME)

	/**
	 * In order to test a specific channel, it is neccessary to create a Thing with two channels - CHANNEL_POWER
	 * and the tested channel. So before each test, the power channel is created and added
	 * to an ArrayList of channels. Then in the tests an additional channel is created and added to the ArrayList
	 * when it's needed.
	 */
	Channel powerChannel

	/**
	 * A HashMap which saves all the 'channel-acceppted_item_type' pairs.
	 * It is set before all the tests.
	 */
	static HashMap <String, String> acceptedItemTypes

	/**
	 * ArrayList of channels which is used to initialize a radioThing in the test cases.
	 */
	ArrayList <Channel> channels = new ArrayList <Channel>()

	ManagedThingProvider managedThingProvider
	ManagedItemChannelLinkProvider itemChannelLinkProvider
	ThingRegistry thingRegistry
	ItemRegistry itemRegistry

	/** An instance of the mock HttpServlet which is used for the tests */
	RadioServiceMock servlet;
	def MOCK_SERVLET_PATH = FrontierSiliconRadioConnection.path

	FSInternetRadioHandler radioHandler;

	// default configuration properties
	def DEFAULT_CONFIG_PROPERTY_IP = "127.0.0.1"
	def DEFAULT_CONFIG_PROPERTY_PIN = "1234"
	def DEFAULT_CONFIG_PROPERTY_PORT = "9090"
	/** The default refresh interval is 60 seconds. For the purposes of the tests it is set to 1 second */
	def DEFAULT_CONFIG_PROPERTY_REFRESH = "1"
	def DEFAULT_COMPLETE_CONFIGURATION = createDefaultConfiguration()

	/** 
	 * Enabling channel item provider is done asynchronously.
	 * In order to be sure that we get the actual item state, we need to put the current
	 * Thread to sleep for a while after a new {@link ItemChannelLink} is added
	 */
	def WAIT_ITEM_CHANNEL_LINK_TO_BE_ADDED = 300

	@BeforeClass
	public static void setUpClass () {
		setTheChannelsMap()
	}

	@Before
	public void setUp ()  {
		setUpServices()
		registerRadioTestServlet()
		createThePowerChannel()
	}

	@After
	void tearDown() {
		servlet.isInvalidValueExpected = false
		servlet.isInvalidResponseExpected = false
		servlet.isOKAnswerExpected = true

		channels.clear()
		unregisterRadioTestServlet()
		clearTheThingProvider()
		clearTheItemRegistry()
	}

	@Test
	void 'verify OFFLINE Thing-status and NULL Item-state when the IP is NULL'() {

		Configuration config = createConfiguration(null, DEFAULT_CONFIG_PROPERTY_PIN, DEFAULT_CONFIG_PROPERTY_PORT, DEFAULT_CONFIG_PROPERTY_REFRESH)

		Thing radioThingWithNullIP = initializeRadioThing(config)
		testRadioThingWithConfiguration(radioThingWithNullIP)
	}

	@Test
	void 'verify OFFLINE Thing-status and NULL Item-state when the PIN is empty String'() {

		Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, "", DEFAULT_CONFIG_PROPERTY_PORT, DEFAULT_CONFIG_PROPERTY_REFRESH)

		Thing radioThingWithEmptyPIN = initializeRadioThing(config)
		testRadioThingWithConfiguration(radioThingWithEmptyPIN)
	}

	@Test
	void 'verify OFFLINE Thing-status and NULL Item-state when the PORT is zero'() {

		Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN, "0", DEFAULT_CONFIG_PROPERTY_REFRESH)

		Thing radioThingWithZeroPort = initializeRadioThing(config)
		testRadioThingWithConfiguration(radioThingWithZeroPort)
	}

	@Test
	void 'verify OFFLINE Thing-status and NULL Item-state when the PIN is wrong'() {

		def wrongPin = "5678"
		Configuration config = createConfiguration(wrongPin, DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PORT, DEFAULT_CONFIG_PROPERTY_REFRESH)

		Thing radioThingWithWrongPin = initializeRadioThing(config)

		waitForAssert {
			assertThat ("The ThingStatus was not updated correctly when a wrong PIN is used", radioThingWithWrongPin.getStatus(), is(equalTo(ThingStatus.OFFLINE)))
			assertThat ("The ThingStatusInfo was not updated correctly when a wrong PIN is used", radioThingWithWrongPin.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)))
		}

		ChannelUID powerChannelUID = powerChannel.getUID()
		Item testItem = initializeItem(powerChannelUID, "${DEFAULT_TEST_ITEM_NAME}",
				acceptedItemTypes.get(FSInternetRadioBindingConstants.CHANNEL_POWER))

		waitForAssert {
			assertThat ("The item's state was not updated correctly when a wrong PIN is used", testItem.getState(), is (UnDefType.NULL))
		}
	}

	@Test
	void 'verify OFFLINE Thing-status and NULL Item-state when the HTTP response cannot be parsed correctly'() {

		// create a thing with two channels - the power channel and any of the others
		String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE
		String acceptedItemType = acceptedItemTypes.get(modeChannelID)
		createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		// turn-on the radio
		turnTheRadioOn(radioThing)

		ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID()
		Item modeTestItem = initializeItem(modeChannelUID, "mode", acceptedItemType)

		/*
		 *  Setting the isInvalidResponseExpected variable to true
		 *  in order to get the incorrect XML response from the servlet
		 */
		servlet.isInvalidResponseExpected = true

		// try to handle a command
		radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"))

		waitForAssert {
			assertThat ("The ThingStatus was not updated correctly when the HTTP response cannot be parsed",radioThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)))
			assertThat ("The ThingStatusInfo was not updated correctly when the HTTP response cannot be parsed", radioThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)))
		}
		waitForAssert {
			assertThat ("The item's state was not updated correctly when the HTTP response cannot be parsed", modeTestItem.getState(), is (UnDefType.NULL))
		}
	}

	@Test
	void 'verify not registered RadioHandler when the thingTypeUID is not the DEFAULT_THING_TYPE_UID'() {

		ThingTypeUID anotherThingTypeUID = new ThingTypeUID("anotherBindingID","notRadio")
		ThingUID anotherThingUID = new ThingUID(anotherThingTypeUID, DEFAULT_TEST_THING_NAME)

		Thing radioThing = ThingBuilder.create(anotherThingTypeUID, anotherThingUID).withConfiguration(DEFAULT_COMPLETE_CONFIGURATION).withChannels(channels).build();
		managedThingProvider.add(radioThing)

		waitForAssert{
			radioHandler = getService(ThingHandler, FSInternetRadioHandler)
			assertThat "A FSInternetradioHandler was created for a Thing which is not a radio", radioHandler, is(null)
		}
	}

	@Test
	void 'verify the HTTP status is handled correctly when it is not OK_200'() {
		
		// create a thing with two channels - the power channel and any of the others
		String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE
		String acceptedItemType = acceptedItemTypes.get(modeChannelID)
		createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		// turn-on the radio
		turnTheRadioOn(radioThing)

		/*
		 * Setting the needed boolean variable to false, so we can be sure 
		 *  that the XML response won't have a OK_200 status
		 */
		servlet.isOKAnswerExpected = false

		ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID()
		Item modeTestItem = initializeItem(modeChannelUID, "mode", acceptedItemType)

		servlet.isInvalidResponseExpected = true

		// try to handle a command
		radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"))

		waitForAssert {
			assertThat ("The item's state was not updated correctly when the HTTP status is different from OK_200", modeTestItem.getState(), is (UnDefType.NULL))
		}
	}

	@Test
	void 'verify ONLINE status of a Thing with complete configuration' () {

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)
	}

	@Test
	void 'verify the power channel is updated' () {

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		ChannelUID powerChannelUID = powerChannel.getUID()
		Item powerTestItem = initializeItem(powerChannelUID, "${DEFAULT_TEST_ITEM_NAME}",
				acceptedItemTypes.get(FSInternetRadioBindingConstants.CHANNEL_POWER))

		radioHandler.handleCommand(powerChannelUID, OnOffType.ON)
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to turn-on the radio", powerTestItem.getState(), is (OnOffType.ON))
		}

		radioHandler.handleCommand(powerChannelUID, OnOffType.OFF)
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to turn-off the radio", powerTestItem.getState(), is (OnOffType.OFF))
		}

		/*
		 *  Setting the needed boolean variable to true, so we can be sure 
		 *  that an invalid value will be returned in the XML response
		 */
		servlet.isInvalidValueExpected = true

		radioHandler.handleCommand(powerChannelUID, OnOffType.ON)
		waitForAssert {
			assertThat ("The item's state was not updated correctly when an invalid power value is returned in the HTTP response", powerTestItem.getState(), is (OnOffType.OFF))
		}
	}

	@Test
	void 'verify the mute channel is updated' () {

		String muteChannelID = FSInternetRadioBindingConstants.CHANNEL_MUTE
		String acceptedItemType = acceptedItemTypes.get(muteChannelID)
		createChannel(DEFAULT_THING_UID, muteChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID muteChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_MUTE).getUID()
		Item muteTestItem = initializeItem(muteChannelUID, "${DEFAULT_TEST_ITEM_NAME}", acceptedItemType)

		radioHandler.handleCommand(muteChannelUID, OnOffType.ON)
		waitForAssert {
			assertThat ("The item's state was not updated correctly in attempt to mute the radio", muteTestItem.getState(), is (OnOffType.ON))
		}

		radioHandler.handleCommand(muteChannelUID, OnOffType.OFF)
		waitForAssert {
			assertThat ("The item's state was not updated correctly in attempt to unmute the radio", muteTestItem.getState(), is (OnOffType.OFF))
		}

		/*
		 *  Setting the needed boolean variable to true, so we can be sure 
		 *  that an invalid value will be returned in the XML response
		 */
		servlet.isInvalidValueExpected = true

		radioHandler.handleCommand(muteChannelUID, OnOffType.ON)
		waitForAssert {
			assertThat ("The item's state was not updated correctly when an invalid mute value is returned in the HTTP response", muteTestItem.getState(), is (OnOffType.OFF))
		}
	}

	@Test
	void 'verify the mode channel is updated' () {

		String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE
		String acceptedItemType = acceptedItemTypes.get(modeChannelID)
		createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID()
		Item modeTestItem = initializeItem(modeChannelUID, "${DEFAULT_TEST_ITEM_NAME}", acceptedItemType)

		radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"))
		waitForAssert {
			assertThat ("The item's state was not updated correctly in attempt to set the radio mode", modeTestItem.getState(), is (DecimalType.valueOf("1")))
		}

		/*
		 *  Setting the needed boolean variable to true, so we can be sure 
		 *  that an invalid value will be returned in the XML response
		 */	
		servlet.isInvalidValueExpected = true

		radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("3"))
		waitForAssert {
			assertThat("The item's state was not updated correctly when an invalid mode value is returned in the HTTP response", modeTestItem.getState(), is (DecimalType.valueOf("0")))
		}
	}

	@Test
	void 'verify the volume is updated through the CHANNEL_VOLUME_ABSOLUTE' () {

		String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE
		String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID)
		createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID()
		Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, "${DEFAULT_TEST_ITEM_NAME}", absoluteAcceptedItemType)

		testChannelWithINCREASEAndDECREASECommands(absoluteVolumeChannelUID, volumeTestItem)

		testChannelWithUPAndDOWNCommands(absoluteVolumeChannelUID, volumeTestItem)

		// Trying to set a value that is greater than the maximum volume
		radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("36"))

		waitForAssert {
			assertThat("The item's state was not updated correctly when a value greater than the maximum volume is passed", volumeTestItem.getState(), is (DecimalType.valueOf("32")))
		}

		// Trying to increase the volume more than its maximum value using the INCREASE command
		radioHandler.handleCommand(absoluteVolumeChannelUID, IncreaseDecreaseType.INCREASE)

		waitForAssert {
			assertThat("The item's state was not updated correctly in an attemt to increase the volume above the maximum", volumeTestItem.getState(), is (DecimalType.valueOf("32")))
		}

		// Trying to increase the volume more than its maximum value using the UP command
		radioHandler.handleCommand(absoluteVolumeChannelUID, UpDownType.UP)


		waitForAssert {
			assertThat("The item's state was not updated correctly in an attemt to increase the volume above the maximum", volumeTestItem.getState(), is (DecimalType.valueOf("32")))
		}

		// Trying to set a value that is lower than the minimum volume value
		radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("-10"))
		waitForAssert {
			assertThat("The item's state was not updated correctly when a value lower than the minimum volume is passed", volumeTestItem.getState(), is (DecimalType.valueOf("0")))
		}

		// Trying to decrease the volume below the minimum value using the DOWN command
		radioHandler.handleCommand(absoluteVolumeChannelUID, UpDownType.DOWN)
		waitForAssert {
			assertThat("The item's state was not updated correctly in an attemt to decrease the volume below the minimum", volumeTestItem.getState(), is (DecimalType.valueOf("0")))
		}

		/*
		 *  Setting the needed boolean variable to true, so we can be sure 
		 *  that an invalid value will be returned in the XML response
		 */
		servlet.isInvalidValueExpected = true

		//trying to set the volume
		radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("15"))
		waitForAssert {
			assertThat("The item's state was not updated correctly when an invalid volume value is returned in the HTTP response", volumeTestItem.getState(), is (DecimalType.valueOf("0")))
		}
	}

	@Test
	void 'verify the volume is updated through the CHANNEL_VOLUME_PERCENT' () {
		/*
		 * The volume is set through the CHANNEL_VOLUME_PERCENT in order to check if
		 * the absolute volume will be updated properly.
		 */

		String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE
		String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID)
		Channel absoluteVolumeChannel = createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType)

		String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT
		String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID)
		createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID()
		Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, "${DEFAULT_TEST_ITEM_NAME}", absoluteAcceptedItemType)

		ChannelUID percentVolumeChannelUID = radioThing.getChannel(percentVolumeChannelID).getUID()

		testChannelWithINCREASEAndDECREASECommands(percentVolumeChannelUID, volumeTestItem)

		testChannelWithUPAndDOWNCommands(percentVolumeChannelUID, volumeTestItem)

		/* 
		 * Giving the handler a valid percent value. According to the FrontierSiliconRadio's
		 * documentation 100 percents correspond to 32 absolute value
		 */
		radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("50"))
		waitForAssert {
			assertThat ("The item's state was not updated correctly when a valid volume percent value is passed", volumeTestItem.getState(), is (DecimalType.valueOf("16")))
		}

		/*
		 *  Setting the needed boolean variable to true, so we can be sure 
		 *  that an invalid value will be returned in the XML response
		 */
		servlet.isInvalidValueExpected = true

		radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("15"))

		waitForAssert {
			assertThat("The item's state was not updated correctly when an invalid volume value is returned in the HTTP response", volumeTestItem.getState(), is (PercentType.valueOf("0")))
		}
	}

	private void testChannelWithINCREASEAndDECREASECommands(ChannelUID channelUID, Item item) {

		// First we have to make sure that the item state is 0
		radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"))
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to set a value using the DecimalType command", item.getState(), is (DecimalType.valueOf("0")))
		}

		radioHandler.handleCommand(channelUID, IncreaseDecreaseType.INCREASE)

		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to use the INCREASE command", item.getState(), is (DecimalType.valueOf("1")))
		}

		radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE)
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to use the DECREASE command", item.getState(), is (DecimalType.valueOf("0")))
		}

		// Trying to decrease one more time
		radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE)
		waitForAssert {
			assertThat("The item's state was not updated correctly in an attemt to decrease the value below zero using the DECREASE command", item.getState(), is (DecimalType.valueOf("0")))
		}
	}

	private void testChannelWithUPAndDOWNCommands(ChannelUID channelUID, Item item){

		// First we have to make sure that the item state is 0
		radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"))
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to set a value using the DecimalType command", item.getState(), is (DecimalType.valueOf("0")))
		}

		radioHandler.handleCommand(channelUID, UpDownType.UP)
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to use the UP command", item.getState(), is (DecimalType.valueOf("1")))
		}

		radioHandler.handleCommand(channelUID, UpDownType.DOWN)
		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to use the DOWN command", item.getState(), is (DecimalType.valueOf("0")))
		}

		// Trying to decrease one more time
		radioHandler.handleCommand(channelUID, UpDownType.DOWN)
		waitForAssert {
			assertThat("The item's state was not updated correctly in an attemt to decrease the value below zero using the DOWN command", item.getState(), is (DecimalType.valueOf("0")))
		}
	}

	@Test
	void 'verify the preset channel is updated' () {

		String presetChannelID = FSInternetRadioBindingConstants.CHANNEL_PRESET
		String acceptedItemType = acceptedItemTypes.get(presetChannelID)
		createChannel(DEFAULT_THING_UID, presetChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)
		turnTheRadioOn(radioThing)

		ChannelUID presetChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_PRESET).getUID()
		Item presetTestItem = initializeItem(presetChannelUID, "${DEFAULT_TEST_ITEM_NAME}", acceptedItemType)

		radioHandler.handleCommand(presetChannelUID, DecimalType.valueOf("100"))
		waitForAssert {
			assertThat ("The radio station was not set correctly", servlet.getRadioStation(), is ("100"))
		}
	}

	@Test
	void 'verify the playInfoName channel is updated' () {

		String playInfoNameChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME
		String acceptedItemType = acceptedItemTypes.get(playInfoNameChannelID)
		createChannel(DEFAULT_THING_UID, playInfoNameChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID playInfoNameChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME).getUID()
		Item playInfoNameTestItem = initializeItem(playInfoNameChannelUID, "${DEFAULT_TEST_ITEM_NAME}", acceptedItemType)

		waitForAssert {
			assertThat ("The item's state was not updated correctly in attempt to get the name of the current radio station or track", playInfoNameTestItem.getState(), is("random_station"))
		}
	}

	@Test
	void 'verify the playInfoText channel is updated' () {

		String playInfoTextChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT
		String acceptedItemType = acceptedItemTypes.get(playInfoTextChannelID)
		createChannel(DEFAULT_THING_UID, playInfoTextChannelID, acceptedItemType)

		Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION)
		testRadioThingWithConfiguration(radioThing)

		turnTheRadioOn(radioThing)

		ChannelUID playInfoTextChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT).getUID()
		Item playInfoTextTestItem = initializeItem(playInfoTextChannelUID, "${DEFAULT_TEST_ITEM_NAME}", acceptedItemType)

		waitForAssert {
			assertThat("The item's state was not updated correctly in attempt to get the additional information about the station", playInfoTextTestItem.getState(), is("additional_info"))
		}
	}

	private Configuration createDefaultConfiguration() {
		return createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN, DEFAULT_CONFIG_PROPERTY_PORT, DEFAULT_CONFIG_PROPERTY_REFRESH)
	}

	private Configuration createConfiguration(String ip, String pin, String port, String refresh) {
		Configuration config = new Configuration()
		config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP, ip)
		config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN, pin)
		config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT, new BigDecimal(port))
		config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_REFRESH, new BigDecimal(refresh))
		return config
	}

	private static void setTheChannelsMap () {

		acceptedItemTypes = new HashMap <String, String> ()
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_POWER, "Switch")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MODE, "Number")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MUTE, "Switch")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME, "String")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT, "String")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PRESET, "Number")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE, "Number")
		acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT, "Dimmer")
	}

	private void setUpServices() {
		// ManagedItemChannelLinkProvider needs a StorageService to persist the elements
		VolatileStorageService volatileStorageService = new VolatileStorageService()
		registerService(volatileStorageService)

		managedThingProvider = getService(ThingProvider, ManagedThingProvider)

		thingRegistry = getService(ThingRegistry)
		assertThat ("The ThingRegistry service cannot be found", thingRegistry, is(notNullValue()))

		itemRegistry = getService(ItemRegistry)
		assertThat ("The ItemRegistry service cannot be found", itemRegistry, is(notNullValue()))

		itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
		assertThat ("The ManagedItemChannelLinkProvider service cannot be found", itemChannelLinkProvider, is(notNullValue()))
	}

	private void registerRadioTestServlet (){
		HttpService httpService
		waitForAssert {
			httpService = getService(HttpService)
			assertThat ("The HttpService cannot be found", httpService,is(notNullValue()))
		}
		servlet = new RadioServiceMock()
		httpService.registerServlet(MOCK_SERVLET_PATH, servlet, null, null)
	}

	private void createThePowerChannel() {
		String powerChannelID = FSInternetRadioBindingConstants.CHANNEL_POWER
		String acceptedItemType = acceptedItemTypes.get(powerChannelID)
		powerChannel = createChannel(DEFAULT_THING_UID, powerChannelID, acceptedItemType)
	}

	private void unregisterRadioTestServlet(){
		HttpService httpService = getService(HttpService)
		assertThat ("No HttpService can be found", httpService,is(notNullValue()))
		httpService.unregister(MOCK_SERVLET_PATH)
		servlet = null
	}

	private void clearTheThingProvider() {
		for(int j = 0; j < managedThingProvider.getAll().size(); j++) {
			Thing thingToRemove = managedThingProvider.getAll().getAt(j)
			managedThingProvider.remove(thingToRemove.getUID())
		}
		assertThat ("Something went wrong with the removal of the registered Things", managedThingProvider.getAll().isEmpty(), is(true))
	}

	private void clearTheItemRegistry () {
		for(int i = 0; i < itemRegistry.getItems().size(); i++) {
			Item item = itemRegistry.getItems().getAt(i)
			String name = item.getName()
			itemRegistry.remove(name)
		}
		assertThat ("Something went wrong with the removal of the registered Items", itemRegistry.getItems().isEmpty(), is(true))
	}

	private Item initializeItem (ChannelUID channelUID, String itemName, String acceptedItemType) {

		Item item = null

		switch(acceptedItemType) {
			case "Number":
				item = new NumberItem(itemName)
				break

			case "String":
				item = new StringItem(itemName)
				break

			case "Switch":
				item =  new SwitchItem(itemName)
				break;

			case "Dimmer":
				item = new DimmerItem(itemName)
				break;
		}

		itemRegistry.add(item)
		itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID))
		sleep(WAIT_ITEM_CHANNEL_LINK_TO_BE_ADDED)
		return item
	}

	private Channel createChannel(ThingUID thingUID, String channelID, String acceptedItemType) {

		ChannelUID channelUID = new ChannelUID(thingUID,channelID)
		ChannelTypeUID channelTypeUID = new ChannelTypeUID(FSInternetRadioBindingConstants.BINDING_ID,channelUID.getIdWithoutGroup())
		Channel radioChannel = new Channel(channelUID, acceptedItemType)
		channels.add(radioChannel)
		return radioChannel
	}

	private void testRadioThingWithCompleteConfiguration(Thing radioThingWithCompleteConfiguration) {

		waitForAssert {
			assertThat ("The radioThing with complete configuration was not created successfully", radioThingWithCompleteConfiguration.getStatus(), is(equalTo(ThingStatus.ONLINE)))
		}
	}

	private void testRadioThingWithIncompleteConfiguration(Thing radioThingWithIncompleteConfig) {

		waitForAssert {
			assertThat ("The ThingStatus was not updated correctly when a Thing with incomplete configuration was created", radioThingWithIncompleteConfig.getStatus(), is(equalTo(ThingStatus.OFFLINE)))
			assertThat ("The ThingStatusInfo was not updated correctly when a Thing with incomplete configuration was created", radioThingWithIncompleteConfig.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)))
		}

		ChannelUID powerChannelUID = powerChannel.getUID()
		Item testItem = initializeItem(powerChannelUID, "${DEFAULT_TEST_ITEM_NAME}",
				acceptedItemTypes.get(FSInternetRadioBindingConstants.CHANNEL_POWER))

		waitForAssert {
			assertThat ("The item's state was not updated correctly when a Thing with incomplete configuration was created", testItem.getState(), is (UnDefType.NULL))
		}
	}

	private void testRadioThingWithConfiguration(Thing thing) {
		Configuration config = thing.getConfiguration()
		if(isConfigurationComplete(config)) {
			testRadioThingWithCompleteConfiguration(thing)
		} else {
			testRadioThingWithIncompleteConfiguration(thing)
		}
	}

	private boolean isConfigurationComplete(Configuration config){
		String ip = (String) config.get((String)FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP)
		BigDecimal port = (BigDecimal) config.get((String)FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT.toString())
		String pin = (String) config.get((String)FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN.toString())

		if(ip == null || port == 0 || StringUtils.isEmpty(pin)) {
			return false
		}
		return true
	}

	private Thing initializeRadioThing (Configuration config) {

		Thing radioThing = ThingBuilder.create(DEFAULT_THING_TYPE_UID, DEFAULT_THING_UID).withConfiguration(config).withChannels(channels).build();
		managedThingProvider.add(radioThing)

		waitForAssert{
			radioHandler = getThingHandler(FSInternetRadioHandler.class)
			assertThat "RadioHandler service is not found", radioHandler, is(notNullValue())
		}
		return radioThing
	}


	private void turnTheRadioOn (Thing radioThing) {

		radioHandler.handleCommand(radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_POWER).getUID(),OnOffType.ON)

		waitForAssert {
			assertThat ("The radio was not turned on successfully", radioHandler.radio.getPower(), is (true))
		}
	}

	protected <T extends ThingHandler> T getThingHandler(Class<T> clazz){
		FSInternetRadioHandlerFactory factory
		waitForAssert{
			factory = getService(ThingHandlerFactory, FSInternetRadioHandlerFactory)
			assertThat factory, is(notNullValue())
		}
		def handlers = getThingHandlers(factory)

		for(ThingHandler handler : handlers) {
			if(clazz.isInstance(handler)) {
				return handler
			}
		}
		return null
	}

	private Set<ThingHandler> getThingHandlers(ThingHandlerFactory factory) {
		def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
		assertThat thingManager, not(null)
		thingManager.thingHandlersByFactory.get(factory)
	}
}

