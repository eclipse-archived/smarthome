/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants;
import org.eclipse.smarthome.binding.fsinternetradio.handler.FSInternetRadioHandler;
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;
import org.eclipse.smarthome.binding.fsinternetradio.internal.radio.FrontierSiliconRadioConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * OSGi tests for the {@link FSInternetRadioHandler}.
 *
 * @author Mihaela Memova - Initial contribution
 * @author Markus Rathgeb - Migrated from Groovy to pure Java test, made more robust
 *
 */
public class FSInternetRadioHandlerOSGiTest extends JavaOSGiTest {
    private static final String DEFAULT_TEST_THING_NAME = "testRadioThing";
    private static final String DEFAULT_TEST_ITEM_NAME = "testItem";

    private static final ThingTypeUID DEFAULT_THING_TYPE_UID = FSInternetRadioBindingConstants.THING_TYPE_RADIO;
    private static final ThingUID DEFAULT_THING_UID = new ThingUID(DEFAULT_THING_TYPE_UID, DEFAULT_TEST_THING_NAME);

    /**
     * In order to test a specific channel, it is necessary to create a Thing with two channels - CHANNEL_POWER
     * and the tested channel. So before each test, the power channel is created and added
     * to an ArrayList of channels. Then in the tests an additional channel is created and added to the ArrayList
     * when it's needed.
     */
    private Channel powerChannel;

    /**
     * A HashMap which saves all the 'channel-acceppted_item_type' pairs.
     * It is set before all the tests.
     */
    private static HashMap<String, String> acceptedItemTypes;

    /**
     * ArrayList of channels which is used to initialize a radioThing in the test cases.
     */
    private ArrayList<Channel> channels = new ArrayList<Channel>();

    private ManagedThingProvider managedThingProvider;
    private ManagedItemChannelLinkProvider itemChannelLinkProvider;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;

    /** An instance of the mock HttpServlet which is used for the tests */
    private RadioServiceMock servlet;
    private static final String MOCK_SERVLET_PATH = FrontierSiliconRadioConstants.CONNECTION_PATH;

    private FSInternetRadioHandler radioHandler;

    // default configuration properties
    private static final String DEFAULT_CONFIG_PROPERTY_IP = "127.0.0.1";
    private static final String DEFAULT_CONFIG_PROPERTY_PIN = "1234";
    private static final String DEFAULT_CONFIG_PROPERTY_PORT = "9090";
    /** The default refresh interval is 60 seconds. For the purposes of the tests it is set to 1 second */
    private static final String DEFAULT_CONFIG_PROPERTY_REFRESH = "1";
    private static final Configuration DEFAULT_COMPLETE_CONFIGURATION = createDefaultConfiguration();

    /**
     * Enabling channel item provider is done asynchronously.
     * In order to be sure that we get the actual item state, we need to put the current
     * Thread to sleep for a while after a new {@link ItemChannelLink} is added
     */
    private static final int WAIT_ITEM_CHANNEL_LINK_TO_BE_ADDED = 300;

    @BeforeClass
    public static void setUpClass() {
        setTheChannelsMap();
    }

    @Before
    public void setUp() throws ServletException, NamespaceException {
        setUpServices();
        registerRadioTestServlet();
        createThePowerChannel();
    }

    @After
    public void tearDown() {
        servlet.setInvalidValueExpected(false);
        servlet.setInvalidResponseExpected(false);
        servlet.setOKAnswerExpected(true);

        channels.clear();
        unregisterRadioTestServlet();
        clearTheThingProvider();
        clearTheItemRegistry();
    }

    /**
     * Verify OFFLINE Thing status when the IP is NULL.
     */
    @Test
    public void offlineIfNullIp() {
        Configuration config = createConfiguration(null, DEFAULT_CONFIG_PROPERTY_PIN, DEFAULT_CONFIG_PROPERTY_PORT,
                DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithNullIP = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithNullIP);
    }

    /**
     * Verify OFFLINE Thing status when the PIN is empty String.
     */
    @Test
    public void offlineIfEmptyPIN() {
        Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, "", DEFAULT_CONFIG_PROPERTY_PORT,
                DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithEmptyPIN = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithEmptyPIN);
    }

    /**
     * Verify OFFLINE Thing status when the PORT is zero.
     */
    @Test
    public void offlineIfZeroPort() {
        Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN, "0",
                DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithZeroPort = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithZeroPort);
    }

    /**
     * Verify OFFLINE Thing status when the PIN is wrong.
     */
    @Test
    public void offlineIfWrongPIN() {
        final String wrongPin = "5678";
        Configuration config = createConfiguration(wrongPin, DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PORT,
                DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithWrongPin = initializeRadioThing(config);
        waitForAssert(() -> {
            assertThat("The ThingStatus was not updated correctly when a wrong PIN is used",
                    radioThingWithWrongPin.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat("The ThingStatusInfo was not updated correctly when a wrong PIN is used",
                    radioThingWithWrongPin.getStatusInfo().getStatusDetail(),
                    is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        });
    }

    /**
     * Verify OFFLINE Thing status when the HTTP response cannot be parsed correctly.
     */
    @Test
    public void offlineIfParseError() {
        // create a thing with two channels - the power channel and any of the others
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        // turn-on the radio
        turnTheRadioOn(radioThing);

        ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID();

        /*
         * Setting the isInvalidResponseExpected variable to true
         * in order to get the incorrect XML response from the servlet
         */
        servlet.setInvalidResponseExpected(true);

        // try to handle a command
        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));

        waitForAssert(() -> {
            assertThat("The ThingStatus was not updated correctly when the HTTP response cannot be parsed",
                    radioThing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
            assertThat("The ThingStatusInfo was not updated correctly when the HTTP response cannot be parsed",
                    radioThing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.COMMUNICATION_ERROR)));
        });
    }

    /**
     * Verify not registered RadioHandler when the thingTypeUID is not the DEFAULT_THING_TYPE_UID.
     */
    @Test
    public void notRegisteredHandlerOnThingTypeMismatch() {
        ThingTypeUID anotherThingTypeUID = new ThingTypeUID("anotherBindingID", "notRadio");
        ThingUID anotherThingUID = new ThingUID(anotherThingTypeUID, DEFAULT_TEST_THING_NAME);

        Thing radioThing = ThingBuilder.create(anotherThingTypeUID, anotherThingUID)
                .withConfiguration(DEFAULT_COMPLETE_CONFIGURATION).withChannels(channels).build();
        managedThingProvider.add(radioThing);

        waitForAssert(() -> {
            radioHandler = getService(ThingHandler.class, FSInternetRadioHandler.class);
            assertThat("A FSInternetradioHandler was created for a Thing which is not a radio", radioHandler,
                    nullValue());
        });
    }

    /**
     * Verify the HTTP status is handled correctly when it is not OK_200.
     */
    @Test
    public void httpStatusNokHandling() {
        // create a thing with two channels - the power channel and any of the others
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        // turn-on the radio
        turnTheRadioOn(radioThing);

        /*
         * Setting the needed boolean variable to false, so we can be sure
         * that the XML response won't have a OK_200 status
         */
        servlet.setOKAnswerExpected(false);

        ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID();
        Item modeTestItem = initializeItem(modeChannelUID, "mode", acceptedItemType);

        servlet.setInvalidResponseExpected(true);

        // try to handle a command
        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));

        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly when the HTTP status is different from OK_200",
                    modeTestItem.getState(), is(UnDefType.NULL));
        });
    }

    /**
     * Verify ONLINE status of a Thing with complete configuration.
     */
    @Test
    public void verifyOnlineStatus() {
        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);
    }

    /**
     * Verify the power channel is updated.
     */
    @Test
    public void powerChannelUpdated() {
        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        ChannelUID powerChannelUID = powerChannel.getUID();
        Item powerTestItem = initializeItem(powerChannelUID, DEFAULT_TEST_ITEM_NAME,
                acceptedItemTypes.get(FSInternetRadioBindingConstants.CHANNEL_POWER));

        radioHandler.handleCommand(powerChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly in attempt to turn-on the radio",
                    powerTestItem.getState(), is(OnOffType.ON));
        });

        radioHandler.handleCommand(powerChannelUID, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly in attempt to turn-off the radio",
                    powerTestItem.getState(), is(OnOffType.OFF));
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        servlet.setInvalidValueExpected(true);

        radioHandler.handleCommand(powerChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when an invalid power value is returned in the HTTP response",
                    powerTestItem.getState(), is(OnOffType.OFF));
        });
    }

    /**
     * Verify the mute channel is updated.
     */
    @Test
    public void muteChhannelUpdated() {
        String muteChannelID = FSInternetRadioBindingConstants.CHANNEL_MUTE;
        String acceptedItemType = acceptedItemTypes.get(muteChannelID);
        createChannel(DEFAULT_THING_UID, muteChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID muteChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_MUTE).getUID();
        Item muteTestItem = initializeItem(muteChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(muteChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly in attempt to mute the radio",
                    muteTestItem.getState(), is(OnOffType.ON));
        });

        radioHandler.handleCommand(muteChannelUID, OnOffType.OFF);
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly in attempt to unmute the radio",
                    muteTestItem.getState(), is(OnOffType.OFF));
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        servlet.setInvalidValueExpected(true);

        radioHandler.handleCommand(muteChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when an invalid mute value is returned in the HTTP response",
                    muteTestItem.getState(), is(OnOffType.OFF));
        });
    }

    /**
     * Verify the mode channel is updated.
     */
    @Test
    public void modeChannelUdpated() {
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID modeChannelUID = radioThing.getChannel(modeChannelID).getUID();
        Item modeTestItem = initializeItem(modeChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly in attempt to set the radio mode",
                    modeTestItem.getState(), is(DecimalType.valueOf("1")));
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        servlet.setInvalidValueExpected(true);

        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("3"));
        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when an invalid mode value is returned in the HTTP response",
                    modeTestItem.getState(), is(DecimalType.valueOf("0")));
        });
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_ABSOLUTE using INCREASE and DECREASE commands.
     */
    @Test
    public void volumechannelUpdatedAbsIncDec() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        testChannelWithINCREASEAndDECREASECommands(absoluteVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_ABSOLUTE using UP and DOWN commands.
     */
    @Test
    public void volumeChannelUpdatedAbsUpDown() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        testChannelWithUPAndDOWNCommands(absoluteVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the invalid values when updating CHANNEL_VOLUME_ABSOLUTE are handled correctly.
     */
    @Test
    public void invalidAbsVolumeValues() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        // Trying to set a value that is greater than the maximum volume
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("36"));

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when a value greater than the maximum volume is passed",
                    volumeTestItem.getState(), is(DecimalType.valueOf("32")));
        });

        // Trying to increase the volume more than its maximum value using the INCREASE command
        radioHandler.handleCommand(absoluteVolumeChannelUID, IncreaseDecreaseType.INCREASE);

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly in an attemt to increase the volume above the maximum",
                    volumeTestItem.getState(), is(DecimalType.valueOf("32")));
        });

        // Trying to increase the volume more than its maximum value using the UP command
        radioHandler.handleCommand(absoluteVolumeChannelUID, UpDownType.UP);

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly in an attemt to increase the volume above the maximum",
                    volumeTestItem.getState(), is(DecimalType.valueOf("32")));
        });

        // Trying to set a value that is lower than the minimum volume value
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("-10"));
        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when a value lower than the minimum volume is passed",
                    volumeTestItem.getState(), is(DecimalType.valueOf("0")));
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        servlet.setInvalidValueExpected(true);

        // trying to set the volume
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("15"));
        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when an invalid volume value is returned in the HTTP response",
                    volumeTestItem.getState(), is(DecimalType.valueOf("0")));
        });
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_PERCENT using INCREASE and DECREASE commands.
     */
    @Test
    public void volumeChannelUpdatedPercIncDec() {

        /*
         * The volume is set through the CHANNEL_VOLUME_PERCENT in order to check if
         * the absolute volume will be updated properly.
         */
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        Channel absoluteVolumeChannel = createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID,
                absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = radioThing.getChannel(percentVolumeChannelID).getUID();

        testChannelWithINCREASEAndDECREASECommands(percentVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_PERCENT using UP and DOWN commands.
     */
    @Test
    public void volumeChannelUpdatedPercUpDown() {

        /*
         * The volume is set through the CHANNEL_VOLUME_PERCENT in order to check if
         * the absolute volume will be updated properly.
         */
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        Channel absoluteVolumeChannel = createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID,
                absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = radioThing.getChannel(percentVolumeChannelID).getUID();

        testChannelWithUPAndDOWNCommands(percentVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the valid and invalid values when updating CHANNEL_VOLUME_PERCENT are handled correctly.
     */
    @Test
    public void validInvalidPercVolume() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        Channel absoluteVolumeChannel = createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID,
                absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = radioThing.getChannel(absoluteVolumeChannelID).getUID();
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = radioThing.getChannel(percentVolumeChannelID).getUID();

        /*
         * Giving the handler a valid percent value. According to the FrontierSiliconRadio's
         * documentation 100 percents correspond to 32 absolute value
         */
        radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("50"));
        waitForAssert(() -> {
            assertThat("The item's state was not updated correctly when a valid volume percent value is passed",
                    volumeTestItem.getState(), is(DecimalType.valueOf("16")));
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        servlet.setInvalidValueExpected(true);

        radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("15"));

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly when an invalid volume value is returned in the HTTP response",
                    volumeTestItem.getState(), is(PercentType.valueOf("0")));
        });
    }

    private void testChannelWithINCREASEAndDECREASECommands(ChannelUID channelUID, Item item) {
        synchronized (channelUID) {
            // First we have to make sure that the item state is 0
            radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"));
            waitForAssert(() -> {
                assertThat(
                        "The item's state was not updated correctly in attempt to set a value using the DecimalType command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });

            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.INCREASE);

            waitForAssert(() -> {
                assertThat("The item's state was not updated correctly in attempt to use the INCREASE command",
                        item.getState(), is(DecimalType.valueOf("1")));
            });

            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE);
            waitForAssert(() -> {
                assertThat("The item's state was not updated correctly in attempt to use the DECREASE command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });

            // Trying to decrease one more time
            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE);
            waitForAssert(() -> {
                assertThat(
                        "The item's state was not updated correctly in an attemt to decrease the value below zero using the DECREASE command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });
        }
    }

    private void testChannelWithUPAndDOWNCommands(ChannelUID channelUID, Item item) {
        synchronized (channelUID) {
            // First we have to make sure that the item state is 0
            radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"));
            waitForAssert(() -> {
                assertThat(
                        "The item's state was not updated correctly in attempt to set a value using the DecimalType command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });

            radioHandler.handleCommand(channelUID, UpDownType.UP);
            waitForAssert(() -> {
                assertThat("The item's state was not updated correctly in attempt to use the UP command",
                        item.getState(), is(DecimalType.valueOf("1")));
            });

            radioHandler.handleCommand(channelUID, UpDownType.DOWN);
            waitForAssert(() -> {
                assertThat("The item's state was not updated correctly in attempt to use the DOWN command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });

            // Trying to decrease one more time
            radioHandler.handleCommand(channelUID, UpDownType.DOWN);
            waitForAssert(() -> {
                assertThat(
                        "The item's state was not updated correctly in an attemt to decrease the value below zero using the DOWN command",
                        item.getState(), is(DecimalType.valueOf("0")));
            });
        }
    }

    /**
     * Verify the preset channel is updated.
     */
    @Test
    public void presetChannelUpdated() {
        String presetChannelID = FSInternetRadioBindingConstants.CHANNEL_PRESET;
        String acceptedItemType = acceptedItemTypes.get(presetChannelID);
        createChannel(DEFAULT_THING_UID, presetChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);
        turnTheRadioOn(radioThing);

        ChannelUID presetChannelUID = radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_PRESET).getUID();
        Item presetTestItem = initializeItem(presetChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(presetChannelUID, DecimalType.valueOf("100"));
        waitForAssert(() -> {
            assertThat("The radio station was not set correctly", servlet.getRadioStation(), is("100"));
        });
    }

    /**
     * Verify the playInfoName channel is updated.
     */
    @Test
    public void playInfoNameChannelUpdated() {
        String playInfoNameChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME;
        String acceptedItemType = acceptedItemTypes.get(playInfoNameChannelID);
        createChannel(DEFAULT_THING_UID, playInfoNameChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID playInfoNameChannelUID = radioThing
                .getChannel(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME).getUID();
        Item playInfoNameTestItem = initializeItem(playInfoNameChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly in attempt to get the name of the current radio station or track",
                    playInfoNameTestItem.getState(), is("random_station"));
        });
    }

    /**
     * Verify the playInfoText channel is updated.
     */
    @Test
    public void playInfoTextChannelUpdated() {
        String playInfoTextChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT;
        String acceptedItemType = acceptedItemTypes.get(playInfoTextChannelID);
        createChannel(DEFAULT_THING_UID, playInfoTextChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID playInfoTextChannelUID = radioThing
                .getChannel(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT).getUID();
        Item playInfoTextTestItem = initializeItem(playInfoTextChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        waitForAssert(() -> {
            assertThat(
                    "The item's state was not updated correctly in attempt to get the additional information about the station",
                    playInfoTextTestItem.getState(), is("additional_info"));
        });
    }

    private static Configuration createDefaultConfiguration() {
        return createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN,
                DEFAULT_CONFIG_PROPERTY_PORT, DEFAULT_CONFIG_PROPERTY_REFRESH);
    }

    private static Configuration createConfiguration(String ip, String pin, String port, String refresh) {
        Configuration config = new Configuration();
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP, ip);
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN, pin);
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT, new BigDecimal(port));
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_REFRESH, new BigDecimal(refresh));
        return config;
    }

    private static void setTheChannelsMap() {
        acceptedItemTypes = new HashMap<String, String>();
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_POWER, "Switch");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MODE, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MUTE, "Switch");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME, "String");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT, "String");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PRESET, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT, "Dimmer");
    }

    private void setUpServices() {
        // ManagedItemChannelLinkProvider needs a StorageService to persist the elements
        VolatileStorageService volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);

        thingRegistry = getService(ThingRegistry.class);
        assertThat("The ThingRegistry service cannot be found", thingRegistry, is(notNullValue()));

        itemRegistry = getService(ItemRegistry.class);
        assertThat("The ItemRegistry service cannot be found", itemRegistry, is(notNullValue()));

        itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
        assertThat("The ManagedItemChannelLinkProvider service cannot be found", itemChannelLinkProvider,
                is(notNullValue()));
    }

    private void registerRadioTestServlet() throws ServletException, NamespaceException {
        HttpService httpService = waitForAssert(() -> {
            HttpService tmp = getService(HttpService.class);
            assertThat("The HttpService cannot be found", tmp, is(notNullValue()));
            return tmp;
        });
        servlet = new RadioServiceMock();
        httpService.registerServlet(MOCK_SERVLET_PATH, servlet, null, null);
    }

    private void createThePowerChannel() {
        String powerChannelID = FSInternetRadioBindingConstants.CHANNEL_POWER;
        String acceptedItemType = acceptedItemTypes.get(powerChannelID);
        powerChannel = createChannel(DEFAULT_THING_UID, powerChannelID, acceptedItemType);
    }

    private void unregisterRadioTestServlet() {
        HttpService httpService = getService(HttpService.class);
        assertThat("No HttpService can be found", httpService, is(notNullValue()));
        httpService.unregister(MOCK_SERVLET_PATH);
        servlet = null;
    }

    private void clearTheThingProvider() {
        final Collection<Thing> things = managedThingProvider.getAll();
        for (final Thing thing : things) {
            managedThingProvider.remove(thing.getUID());
        }
        assertThat("Something went wrong with the removal of the registered Things",
                managedThingProvider.getAll().isEmpty(), is(true));
    }

    private void clearTheItemRegistry() {
        final Collection<Item> items = itemRegistry.getItems();
        for (final Item item : items) {
            itemRegistry.remove(item.getName());
        }
        assertThat("Something went wrong with the removal of the registered Items", itemRegistry.getItems().isEmpty(),
                is(true));
    }

    private Item initializeItem(ChannelUID channelUID, String itemName, String acceptedItemType) {

        Item item = null;

        switch (acceptedItemType) {
            case "Number":
                item = new NumberItem(itemName);
                break;

            case "String":
                item = new StringItem(itemName);
                break;

            case "Switch":
                item = new SwitchItem(itemName);
                break;

            case "Dimmer":
                item = new DimmerItem(itemName);
                break;
        }

        itemRegistry.add(item);
        itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));
        try {
            Thread.sleep(WAIT_ITEM_CHANNEL_LINK_TO_BE_ADDED);
        } catch (InterruptedException e) {
        }
        return item;
    }

    private Channel createChannel(ThingUID thingUID, String channelID, String acceptedItemType) {
        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(FSInternetRadioBindingConstants.BINDING_ID,
                channelUID.getIdWithoutGroup());
        Channel radioChannel = new Channel(channelUID, acceptedItemType);
        channels.add(radioChannel);
        return radioChannel;
    }

    private void testRadioThingConsideringConfiguration(Thing thing) {
        Configuration config = thing.getConfiguration();
        if (isConfigurationComplete(config)) {
            waitForAssert(() -> {
                assertThat("The radioThing with complete configuration was not created successfully", thing.getStatus(),
                        is(equalTo(ThingStatus.ONLINE)));
            });
        } else {
            waitForAssert(() -> {
                assertThat(
                        "The ThingStatus was not updated correctly when a Thing with incomplete configuration was created",
                        thing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
                assertThat(
                        "The ThingStatusInfo was not updated correctly when a Thing with incomplete configuration was created",
                        thing.getStatusInfo().getStatusDetail(), is(equalTo(ThingStatusDetail.CONFIGURATION_ERROR)));
            });
        }
    }

    private boolean isConfigurationComplete(Configuration config) {
        String ip = (String) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP);
        BigDecimal port = (BigDecimal) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT.toString());
        String pin = (String) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN.toString());

        if (ip == null || port.compareTo(BigDecimal.ZERO) == 0 || StringUtils.isEmpty(pin)) {
            return false;
        }
        return true;
    }

    private Thing initializeRadioThing(Configuration config) {
        Thing radioThing = ThingBuilder.create(DEFAULT_THING_TYPE_UID, DEFAULT_THING_UID).withConfiguration(config)
                .withChannels(channels).build();
        managedThingProvider.add(radioThing);

        radioHandler = waitForAssert(() -> {
            final ThingHandler thingHandler = radioThing.getHandler();
            assertThat("RadioHandler service is not found", thingHandler, is(instanceOf(FSInternetRadioHandler.class)));
            return (FSInternetRadioHandler) thingHandler;
        });
        return radioThing;
    }

    private void turnTheRadioOn(Thing radioThing) {
        radioHandler.handleCommand(radioThing.getChannel(FSInternetRadioBindingConstants.CHANNEL_POWER).getUID(),
                OnOffType.ON);

        FrontierSiliconRadio radio;
        try {
            Field f = radioHandler.getClass().getDeclaredField("radio");
            f.setAccessible(true);
            radio = (FrontierSiliconRadio) f.get(radioHandler);
        } catch (IllegalStateException | NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException ex) {
            throw new IllegalStateException("Cannot access radio per reflection.", ex);
        }

        waitForAssert(() -> {
            try {
                assertThat("The radio was not turned on successfully", radio.getPower(), is(true));
            } catch (IOException ex) {
                throw new AssertionError("I/O error", ex);
            }
        });
    }

}
