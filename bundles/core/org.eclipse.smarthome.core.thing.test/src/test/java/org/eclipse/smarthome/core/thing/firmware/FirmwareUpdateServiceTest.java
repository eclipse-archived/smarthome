/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing.firmware;

import static org.eclipse.smarthome.core.thing.firmware.Constants.*;
import static org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.common.SafeCaller;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateBackgroundTransferHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.osgi.framework.Bundle;

/**
 * Testing the {@link FirmwareUpdateService}.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Simon Kaufmann - converted to standalone Java tests
 */
public class FirmwareUpdateServiceTest extends JavaOSGiTest {

    public static final ProgressStep[] SEQUENCE = new ProgressStep[] { ProgressStep.REBOOTING, ProgressStep.DOWNLOADING,
            ProgressStep.TRANSFERRING, ProgressStep.UPDATING };

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Thing thing1;
    private Thing thing2;
    private Thing thing3;

    private final FirmwareStatusInfo unknownInfo = createUnknownInfo();
    private final FirmwareStatusInfo upToDateInfo = createUpToDateInfo();
    private final FirmwareStatusInfo updateAvailableInfo = createUpdateAvailableInfo();
    private final FirmwareStatusInfo updateExecutableInfoFw120 = createUpdateExecutableInfo(FW120_EN.getUID());

    private final FirmwareStatusInfo updateExecutableInfoFw112 = createUpdateExecutableInfo(FW112_EN.getUID());
    private final FirmwareStatusInfo updateExecutableInfoFw113 = createUpdateExecutableInfo(FW113_EN.getUID());

    private FirmwareRegistry firmwareRegistry;
    private FirmwareUpdateService firmwareUpdateService;

    @Mock
    private FirmwareUpdateHandler handler1;

    @Mock
    private FirmwareUpdateHandler handler2;

    @Mock
    private FirmwareUpdateHandler handler3;

    @Mock
    private EventPublisher mockPublisher;

    @Mock
    private LocaleProvider mockLocaleProvider;

    @Mock
    private FirmwareProvider mockProvider;

    @Mock
    private TranslationProvider mockTranslationProvider;

    private SafeCaller safeCaller;

    @Before
    public void setup() {
        initMocks(this);
        Map<String, String> props1 = new HashMap<>();
        props1.put(Thing.PROPERTY_FIRMWARE_VERSION, V111);
        props1.put(Thing.PROPERTY_MODEL_ID, MODEL1);
        thing1 = ThingBuilder.create(THING_TYPE_UID1, THING1_ID).withProperties(props1).build();
        Map<String, String> props2 = new HashMap<>();
        props2.put(Thing.PROPERTY_FIRMWARE_VERSION, V112);
        props2.put(Thing.PROPERTY_MODEL_ID, MODEL2);
        thing2 = ThingBuilder.create(THING_TYPE_UID1, THING2_ID).withProperties(props2).build();
        Map<String, String> props3 = new HashMap<>();
        props3.put(Thing.PROPERTY_FIRMWARE_VERSION, VALPHA);
        thing3 = ThingBuilder.create(THING_TYPE_UID2, THING3_ID).withProperties(props3).build();

        firmwareUpdateService = new FirmwareUpdateService();

        safeCaller = getService(SafeCaller.class);
        assertNotNull(safeCaller);

        firmwareUpdateService.setSafeCaller(safeCaller);

        handler1 = addHandler(thing1);
        handler2 = addHandler(thing2);
        handler3 = addHandler(thing3);

        firmwareUpdateService.setEventPublisher(mockPublisher);

        when(mockLocaleProvider.getLocale()).thenReturn(Locale.ENGLISH);
        firmwareUpdateService.setLocaleProvider(mockLocaleProvider);

        firmwareRegistry = new FirmwareRegistry();
        firmwareUpdateService.setFirmwareRegistry(firmwareRegistry);
        firmwareRegistry.setLocaleProvider(mockLocaleProvider);

        when(mockProvider.getFirmware(eq(FW009_EN.getUID()), Matchers.any(Locale.class))).thenReturn(FW009_EN);
        when(mockProvider.getFirmware(eq(FW111_EN.getUID()), Matchers.any(Locale.class))).thenReturn(FW111_EN);
        when(mockProvider.getFirmware(eq(FW112_EN.getUID()), Matchers.any(Locale.class))).thenReturn(FW112_EN);
        when(mockProvider.getFirmware(eq(FWALPHA_EN.getUID()), Matchers.any(Locale.class))).thenReturn(FWALPHA_EN);
        when(mockProvider.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).then(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID2.equals(thingTypeUID)
                    || THING_TYPE_UID3.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Stream.of(FW009_EN, FW111_EN, FW112_EN).collect(Collectors.toSet());
            }
        });
        firmwareRegistry.addFirmwareProvider(mockProvider);

        firmwareUpdateService.setTranslationProvider(mockTranslationProvider);
    }

    @After
    public void tearDown() {
        firmwareUpdateService.deactivate();
    }

    @Test
    public void testGetFirmwareStatusInfo() {
        assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111));
        assertThat(thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112));

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo));
        thing2.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, null);
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(unknownInfo));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(3)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw112);
        assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(1), upToDateInfo);
        assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(2), unknownInfo);
    }

    @Test
    public void testGetFirmwareStatusInfo_noFirmwareProvider() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(unknownInfo));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(1)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING3_UID, eventCaptor.getValue(), unknownInfo);
    }

    @Test
    public void testGetFirmwareStatusInfo_firmwareProviderAddedLate() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(unknownInfo));

        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(eq(FWALPHA_EN.getUID()), any(Locale.class))).thenReturn(FWALPHA_EN);
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID1.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Collections.singleton(FWALPHA_EN);
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING3_UID), is(upToDateInfo));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(2)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING3_UID, eventCaptor.getAllValues().get(0), unknownInfo);
        assertFirmwareStatusInfoEvent(THING3_UID, eventCaptor.getAllValues().get(1), upToDateInfo);
    }

    @Test
    public void testGetFirmwareStatusInfo_eventsOnlySentOnce() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(1)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw112);
    }

    @Test
    public void firmwareStatusPropagatedRegularly() throws Exception {
        assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111));
        assertThat(thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112));

        updateConfig(1, 0, TimeUnit.SECONDS);

        waitForAssert(() -> {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(mockPublisher, times(3)).post(eventCaptor.capture());
            assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw112);
            assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(1), upToDateInfo);
            assertFirmwareStatusInfoEvent(THING3_UID, eventCaptor.getAllValues().get(2), unknownInfo);

        });

        mockPublisher = mock(EventPublisher.class);
        firmwareUpdateService.setEventPublisher(mockPublisher);
        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(eq(FWALPHA_EN.getUID()), any(Locale.class))).thenReturn(null);
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID2.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Collections.singleton(FW113_EN);
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);

        waitForAssert(() -> {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(mockPublisher, times(2)).post(eventCaptor.capture());
            assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw113);
            assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(1), updateExecutableInfoFw113);
        });

        mockPublisher = mock(EventPublisher.class);
        firmwareUpdateService.setEventPublisher(mockPublisher);
        firmwareRegistry.removeFirmwareProvider(firmwareProvider2);

        waitForAssert(() -> {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(mockPublisher, times(2)).post(eventCaptor.capture());
            assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw112);
            assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(1), upToDateInfo);
        });
    }

    @Test
    public void testUpdateFirmware() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112.toString()));
        });

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(2)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(0), updateExecutableInfoFw112);
        assertFirmwareStatusInfoEvent(THING1_UID, eventCaptor.getAllValues().get(1), upToDateInfo);
    }

    @Test
    public void testCancelFirmwareUpdate() {
        firmwareUpdateService.updateFirmware(THING1_UID, FW111_EN.getUID(), Locale.ENGLISH);
        firmwareUpdateService.updateFirmware(THING2_UID, FW111_EN.getUID(), Locale.ENGLISH);
        firmwareUpdateService.updateFirmware(THING3_UID, FWALPHA_EN.getUID(), Locale.ENGLISH);

        firmwareUpdateService.cancelFirmwareUpdate(THING3_UID);

        waitForAssert(() -> {
            verify(handler1, times(0)).cancel();
            verify(handler2, times(0)).cancel();
            verify(handler3, times(1)).cancel();
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void cancelFirmwareUpdate_noFirmwareUpdateHandler() {
        firmwareUpdateService.cancelFirmwareUpdate(new ThingUID("dummy:thing:withoutHandler"));
    }

    @Test(expected = NullPointerException.class)
    public void cancelFirmwareUpdate_thingIdIdNull() {
        firmwareUpdateService.cancelFirmwareUpdate(null);
    }

    @Test(expected = IllegalStateException.class)
    public void cancelFirmwareUpdate_updateNotStarted() {
        firmwareUpdateService.cancelFirmwareUpdate(THING3_UID);
    }

    @Test
    public void testCancelFirmwareUpdate_unexpectedFailure() {
        FirmwareUpdateHandler firmwareUpdateHandler = mock(FirmwareUpdateHandler.class);
        doThrow(new RuntimeException()).when(firmwareUpdateHandler).cancel();
        doReturn(true).when(firmwareUpdateHandler).isUpdateExecutable();
        when(firmwareUpdateHandler.getThing()).thenReturn(ThingBuilder.create(THING_TYPE_UID1, THING4_UID).build());
        firmwareUpdateService.addFirmwareUpdateHandler(firmwareUpdateHandler);

        assertCancellationMessage("unexpected-handler-error-during-cancel", "english", null, 1);

        when(mockLocaleProvider.getLocale()).thenReturn(Locale.FRENCH);
        assertCancellationMessage("unexpected-handler-error-during-cancel", "français", Locale.FRENCH, 2);

        when(mockLocaleProvider.getLocale()).thenReturn(Locale.GERMAN);
        assertCancellationMessage("unexpected-handler-error-during-cancel", "deutsch", Locale.GERMAN, 3);
    }

    @Test
    public void testCancelFirmwareUpdate_takesLong() {
        firmwareUpdateService.timeout = 50;
        FirmwareUpdateHandler firmwareUpdateHandler = mock(FirmwareUpdateHandler.class);
        doAnswer(invocation -> {
            Thread.sleep(200);
            return null;
        }).when(firmwareUpdateHandler).cancel();
        doReturn(true).when(firmwareUpdateHandler).isUpdateExecutable();
        when(firmwareUpdateHandler.getThing()).thenReturn(ThingBuilder.create(THING_TYPE_UID1, THING4_UID).build());
        firmwareUpdateService.addFirmwareUpdateHandler(firmwareUpdateHandler);

        assertCancellationMessage("timeout-error-during-cancel", "english", null, 1);

        when(mockLocaleProvider.getLocale()).thenReturn(Locale.FRENCH);
        assertCancellationMessage("timeout-error-during-cancel", "français", Locale.FRENCH, 2);

        when(mockLocaleProvider.getLocale()).thenReturn(Locale.GERMAN);
        assertCancellationMessage("timeout-error-during-cancel", "deutsch", Locale.GERMAN, 3);
    }

    @Test
    public void testUpdateFirmware_downgrade() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo));

        firmwareUpdateService.updateFirmware(THING2_UID, FW111_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString()));
        });

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(updateExecutableInfoFw112));

        // verify that the corresponding events are sent
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(2)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(0), upToDateInfo);
        assertFirmwareStatusInfoEvent(THING2_UID, eventCaptor.getAllValues().get(1), updateExecutableInfoFw112);
    }

    @Test(expected = NullPointerException.class)
    public void testGetFirmwareStatusInfo_null() {
        firmwareUpdateService.getFirmwareStatusInfo(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetFirmwareStatusInfo_null2() {
        firmwareUpdateService.updateFirmware(null, FW009_EN.getUID(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testGetFirmwareStatusInfo_null3() {
        firmwareUpdateService.updateFirmware(THING1_UID, null, null);
    }

    @Test
    public void testGetFirmwareStatusInfo_notFirmwareUpdateHandler() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(UNKNOWN_THING_UID), is(nullValue()));
    }

    @Test
    public void testUpdateFirmware_unknownFirmware() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(equalTo(String.format("Firmware with UID %s was not found.", UNKNOWN_FIRMWARE_UID)));

        firmwareUpdateService.updateFirmware(THING1_UID, UNKNOWN_FIRMWARE_UID, null);
    }

    @Test
    public void testUpdateFirmware_noFirmwareUpdateHandler() {
        Thing thing4 = ThingBuilder.create(THING_TYPE_UID_WITHOUT_FW, THING5_ID).build();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                equalTo(String.format("There is no firmware update handler for thing with UID %s.", thing4.getUID())));

        firmwareUpdateService.updateFirmware(thing4.getUID(), FW009_EN.getUID(), null);
    }

    @Test
    public void testUpdateFirmware_notExecutable() {
        doReturn(false).when(handler1).isUpdateExecutable();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(
                equalTo(String.format("The firmware update of thing with UID %s is not executable.", THING1_UID)));

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null);
    }

    @Test
    public void testPrerequisiteVersionCheck_illegalVersion() {
        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(any(FirmwareUID.class), any(Locale.class))).thenAnswer(invocation -> {
            FirmwareUID firmwareUID = (FirmwareUID) invocation.getArguments()[0];
            if (FW111_FIX_EN.getUID().equals(firmwareUID)) {
                return FW111_FIX_EN;
            } else if (FW113_EN.getUID().equals(firmwareUID)) {
                return FW113_EN;
            } else {
                return null;
            }

        });
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID2.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Stream.of(FW111_FIX_EN, FW113_EN).collect(Collectors.toSet());
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113));

        try {
            firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null);
            fail("Expeced an IllegalArgumentException, but it was not thrown.");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is(String.format(
                    "Firmware with UID %s requires at least firmware version %s to get installed. But the current firmware version of the thing with UID %s is %s.",
                    FW113_EN.getUID(), FW113_EN.getPrerequisiteVersion(), THING1_UID, V111)));
        }
    }

    @Test
    public void testPrerequisiteVersionCheck() {
        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(any(FirmwareUID.class), any(Locale.class))).thenAnswer(invocation -> {
            FirmwareUID firmwareUID = (FirmwareUID) invocation.getArguments()[0];
            if (FW111_FIX_EN.getUID().equals(firmwareUID)) {
                return FW111_FIX_EN;
            } else if (FW113_EN.getUID().equals(firmwareUID)) {
                return FW113_EN;
            } else {
                return null;
            }

        });
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID2.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Stream.of(FW111_FIX_EN, FW113_EN).collect(Collectors.toSet());
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113));

        firmwareUpdateService.updateFirmware(THING1_UID, FW111_FIX_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111_FIX));
            assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw113));
        });

        firmwareUpdateService.updateFirmware(THING1_UID, FW113_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113));
            assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo));
            assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(updateExecutableInfoFw113));
        });

        firmwareUpdateService.updateFirmware(THING2_UID, FW113_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing2.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V113));
            assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING2_UID), is(upToDateInfo));
        });
    }

    @Test
    public void testFirmwareStatus_updateIsNotExecutable() {
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        doReturn(false).when(handler1).isUpdateExecutable();
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateAvailableInfo));

        doReturn(true).when(handler1).isUpdateExecutable();
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V112));
        });

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(upToDateInfo));
    }

    @Test
    public void testUpdateFirmware_wrongThingType() {
        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(eq(FWALPHA_EN.getUID()), any(Locale.class))).thenReturn(FWALPHA_EN);
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID_WITHOUT_FW.equals(thingTypeUID) || THING_TYPE_UID1.equals(thingTypeUID)) {
                return Collections.emptySet();
            } else {
                return Collections.singleton(FWALPHA_EN);
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(equalTo(String.format("Firmware with UID %s is not suitable for thing with UID %s.",
                FWALPHA_EN.getUID(), THING1_UID)));

        firmwareUpdateService.updateFirmware(THING1_UID, FWALPHA_EN.getUID(), null);
    }

    @Test
    public void testUpdateFirmware_wrongModel() {
        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(eq(FWALPHA_RESTRICTED_TO_MODEL2.getUID()), any(Locale.class)))
                .thenReturn(FWALPHA_RESTRICTED_TO_MODEL2);
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID1.equals(thingTypeUID)) {
                return Collections.singleton(FWALPHA_RESTRICTED_TO_MODEL2);
            } else {
                return Collections.emptySet();
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(equalTo(String.format("Firmware with UID %s is not suitable for thing with UID %s.",
                FWALPHA_RESTRICTED_TO_MODEL2.getUID(), THING1_UID)));

        firmwareUpdateService.updateFirmware(THING1_UID, FWALPHA_RESTRICTED_TO_MODEL2.getUID(), null);
    }

    @Test
    public void testEvents() {
        doAnswer(invocation -> {
            Firmware firmware = (Firmware) invocation.getArguments()[0];
            ProgressCallback progressCallback = (ProgressCallback) invocation.getArguments()[1];

            progressCallback.defineSequence(SEQUENCE);
            progressCallback.next();
            progressCallback.next();
            progressCallback.next();
            progressCallback.next();
            thing1.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion());
            return null;
        }).when(handler1).updateFirmware(any(Firmware.class), any(ProgressCallback.class));

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
        firmwareUpdateService.updateFirmware(THING1_UID, FW112_EN.getUID(), null);

        AtomicReference<List<Event>> events = new AtomicReference<>(new ArrayList<>());
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        waitForAssert(() -> {
            verify(mockPublisher, atLeast(SEQUENCE.length)).post(eventCaptor.capture());
        });
        events.get().addAll(eventCaptor.getAllValues());
        List<Event> list = events.get().stream().filter(event -> event instanceof FirmwareUpdateProgressInfoEvent)
                .collect(Collectors.toList());
        assertTrue(list.size() >= SEQUENCE.length);
        for (int i = 0; i < SEQUENCE.length; i++) {
            FirmwareUpdateProgressInfoEvent event = (FirmwareUpdateProgressInfoEvent) list.get(i);
            assertThat(event.getTopic(), containsString(THING1_UID.getAsString()));
            assertThat(event.getThingUID(), is(THING1_UID));
            assertThat(event.getProgressInfo().getProgressStep(), is(SEQUENCE[i]));
        }
    }

    @Test
    public void testUpdateFirmware_timeOut() {
        firmwareUpdateService.timeout = 50;

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
        waitForAssert(() -> {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(mockPublisher, times(1)).post(eventCaptor.capture());
        });

        doAnswer(invocation -> {
            Thread.sleep(200);
            return null;
        }).when(handler1).updateFirmware(any(Firmware.class), any(ProgressCallback.class));

        assertResultInfoEvent(THING1_UID, FW112_EN, "timeout-error", null, "english", 1);
        assertResultInfoEvent(THING1_UID, FW112_EN, "timeout-error", Locale.ENGLISH, "english", 2);
        assertResultInfoEvent(THING1_UID, FW112_EN, "timeout-error", Locale.GERMAN, "deutsch", 3);
    }

    @Test
    public void testUpdateFirmware_error() {
        doAnswer(invocation -> {
            ProgressCallback progressCallback = (ProgressCallback) invocation.getArguments()[1];
            progressCallback.defineSequence(SEQUENCE);
            progressCallback.next();
            progressCallback.next();
            progressCallback.next();
            progressCallback.next();
            try {
                progressCallback.next();
            } catch (NoSuchElementException e) {
                fail("Unexcepted exception thrown");
            }
            return null;
        }).when(handler1).updateFirmware(any(Firmware.class), any(ProgressCallback.class));

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        assertResultInfoEvent(THING1_UID, FW112_EN, "unexpected-handler-error", null, "english", 1);
        assertResultInfoEvent(THING1_UID, FW112_EN, "unexpected-handler-error", Locale.ENGLISH, "english", 2);
        assertResultInfoEvent(THING1_UID, FW112_EN, "unexpected-handler-error", Locale.GERMAN, "deutsch", 3);

        assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString()));
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
    }

    @Test
    public void testUpdateFirmware_customError() {
        doAnswer(invocation -> {
            ProgressCallback progressCallback = (ProgressCallback) invocation.getArguments()[1];
            progressCallback.failed("test-error");
            return null;
        }).when(handler1).updateFirmware(any(Firmware.class), any(ProgressCallback.class));

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));

        assertResultInfoEvent(THING1_UID, FW112_EN, "test-error", null, "english", 1);
        assertResultInfoEvent(THING1_UID, FW112_EN, "test-error", Locale.ENGLISH, "english", 2);
        assertResultInfoEvent(THING1_UID, FW112_EN, "test-error", Locale.GERMAN, "deutsch", 3);

        assertThat(thing1.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V111.toString()));
        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING1_UID), is(updateExecutableInfoFw112));
    }

    @Test
    public void testIndependentHandlers() {
        int expectedFirmwareUpdateHandlers = 3;

        FirmwareUpdateHandler firmwareUpdateHandler = mock(FirmwareUpdateHandler.class);
        when(firmwareUpdateHandler.getThing())
                .thenReturn(ThingBuilder.create(THING_TYPE_UID3, UNKNOWN_THING_UID).build());
        firmwareUpdateService.addFirmwareUpdateHandler(firmwareUpdateHandler);

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(UNKNOWN_THING_UID), is(unknownInfo));
    }

    @Test
    public void testInvalidValuesAreRejected() throws Exception {
        int originalPeriod = firmwareUpdateService.getFirmwareStatusInfoJobPeriod();
        int originalDelay = firmwareUpdateService.getFirmwareStatusInfoJobDelay();
        TimeUnit originalTimeUnit = firmwareUpdateService.getFirmwareStatusInfoJobTimeUnit();

        updateInvalidConfigAndAssert(0, 0, TimeUnit.SECONDS, originalPeriod, originalDelay, originalTimeUnit);
        updateInvalidConfigAndAssert(1, -1, TimeUnit.SECONDS, originalPeriod, originalDelay, originalTimeUnit);
        updateInvalidConfigAndAssert(1, 0, TimeUnit.NANOSECONDS, originalPeriod, originalDelay, originalTimeUnit);
    }

    private volatile AtomicBoolean updateExecutable = new AtomicBoolean(false);

    @Test
    public void testBackgroundTransfer() throws Exception {

        Map<String, String> props = new HashMap<>();
        props.put(Thing.PROPERTY_FIRMWARE_VERSION, V111);
        Thing thing4 = ThingBuilder.create(THING_TYPE_UID3, THING4_ID).withProperties(props).build();

        FirmwareUpdateBackgroundTransferHandler handler4 = mock(FirmwareUpdateBackgroundTransferHandler.class);
        when(handler4.getThing()).thenReturn(thing4);
        doAnswer(invocation -> {
            return updateExecutable.get();
        }).when(handler4).isUpdateExecutable();
        doAnswer(invocation -> {
            Firmware firmware = (Firmware) invocation.getArguments()[0];
            thing4.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion());
            updateExecutable.set(false);
            return null;
        }).when(handler4).updateFirmware(any(Firmware.class), any(ProgressCallback.class));
        firmwareUpdateService.addFirmwareUpdateHandler(handler4);
        doAnswer(invocation -> {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            updateExecutable.set(true);
            return null;
        }).when(handler4).transferFirmware(any(Firmware.class));

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(unknownInfo));
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockPublisher, times(1)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING4_UID, eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1),
                unknownInfo);

        FirmwareProvider firmwareProvider2 = mock(FirmwareProvider.class);
        when(firmwareProvider2.getFirmware(eq(FW120_EN.getUID()), any(Locale.class))).thenReturn(FW120_EN);
        when(firmwareProvider2.getFirmwares(any(ThingTypeUID.class), any(Locale.class))).thenAnswer(invocation -> {
            ThingTypeUID thingTypeUID = (ThingTypeUID) invocation.getArguments()[0];
            if (THING_TYPE_UID3.equals(thingTypeUID)) {
                return Collections.singleton(FW120_EN);
            } else {
                return Collections.emptySet();
            }
        });
        firmwareRegistry.addFirmwareProvider(firmwareProvider2);

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(updateAvailableInfo));
        verify(mockPublisher, times(2)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING4_UID, eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1),
                updateAvailableInfo);

        waitForAssert(() -> {
            assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(updateExecutableInfoFw120));
            verify(mockPublisher, times(3)).post(eventCaptor.capture());
            assertFirmwareStatusInfoEvent(THING4_UID,
                    eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1), updateExecutableInfoFw120);
        });

        firmwareUpdateService.updateFirmware(THING4_UID, FW120_EN.getUID(), null);

        waitForAssert(() -> {
            assertThat(thing4.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is(V120));
        });

        assertThat(firmwareUpdateService.getFirmwareStatusInfo(THING4_UID), is(upToDateInfo));
        verify(mockPublisher, times(4)).post(eventCaptor.capture());
        assertFirmwareStatusInfoEvent(THING4_UID, eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1),
                upToDateInfo);

        assertThat(handler4.isUpdateExecutable(), is(false));
    }

    private void updateInvalidConfigAndAssert(int period, int delay, TimeUnit timeUnit, int expectedPeriod,
            int expectedDelay, TimeUnit expectedTimeUnit) throws IOException {
        updateConfig(period, delay, timeUnit);
        waitForAssert(() -> {
            assertThat(firmwareUpdateService.getFirmwareStatusInfoJobPeriod(), is(expectedPeriod));
            assertThat(firmwareUpdateService.getFirmwareStatusInfoJobDelay(), is(expectedDelay));
            assertThat(firmwareUpdateService.getFirmwareStatusInfoJobTimeUnit(), is(expectedTimeUnit));
        });
    }

    private void assertResultInfoEvent(ThingUID thingUID, Firmware firmware, String messageKey, Locale locale,
            String text, int expectedEventCount) {
        firmwareUpdateService.removeFirmwareUpdateHandler(handler1);
        firmwareUpdateService.addFirmwareUpdateHandler(handler1);

        when(mockLocaleProvider.getLocale()).thenReturn(locale);
        when(mockTranslationProvider.getText(any(Bundle.class), eq(messageKey), any(String.class), eq(locale),
                Matchers.anyVararg())).thenReturn(text);

        firmwareUpdateService.updateFirmware(thingUID, firmware.getUID(), locale);
        waitForAssert(() -> {
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(mockPublisher, atLeast(expectedEventCount)).post(eventCaptor.capture());
            List<Event> allValues = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof FirmwareUpdateResultInfoEvent).collect(Collectors.toList());
            assertEquals(expectedEventCount, allValues.size());
            assertFailedFirmwareUpdate(THING1_UID, allValues.get(expectedEventCount - 1), text);
        });
    }

    private void assertCancellationMessage(String messageKey, String expectedEnglishMessage, Locale locale,
            int expectedEventCount) {
        firmwareUpdateService.removeFirmwareUpdateHandler(handler3);
        firmwareUpdateService.addFirmwareUpdateHandler(handler3);

        when(mockLocaleProvider.getLocale()).thenReturn(locale);

        when(mockTranslationProvider.getText(Matchers.isA(Bundle.class), eq(messageKey), any(String.class), eq(locale),
                Matchers.anyVararg())).thenReturn(expectedEnglishMessage);

        Exception exception = new RuntimeException();

        doThrow(exception).when(handler3).cancel();

        firmwareUpdateService.updateFirmware(THING4_UID, FW111_EN.getUID(), locale);
        firmwareUpdateService.cancelFirmwareUpdate(THING4_UID);

        AtomicReference<FirmwareUpdateResultInfoEvent> resultEvent = new AtomicReference<FirmwareUpdateResultInfoEvent>();
        waitForAssert(() -> {
            ArgumentCaptor<FirmwareUpdateResultInfoEvent> eventCaptor = ArgumentCaptor
                    .forClass(FirmwareUpdateResultInfoEvent.class);
            verify(mockPublisher, times(expectedEventCount)).post(eventCaptor.capture());
            assertEquals(expectedEventCount, eventCaptor.getAllValues().size());
            resultEvent.set(eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1));
        });
        assertThat(resultEvent.get().getThingUID(), is(THING4_UID));
        assertThat(resultEvent.get().getFirmwareUpdateResultInfo().getResult(), is(FirmwareUpdateResult.ERROR));
        assertThat(resultEvent.get().getFirmwareUpdateResultInfo().getErrorMessage(), is(expectedEnglishMessage));
    }

    private void assertFirmwareStatusInfoEvent(ThingUID thingUID, Event event, FirmwareStatusInfo expectedInfo) {
        assertThat(event, is(instanceOf(FirmwareStatusInfoEvent.class)));
        FirmwareStatusInfoEvent firmwareStatusInfoEvent = (FirmwareStatusInfoEvent) event;

        assertThat(firmwareStatusInfoEvent.getTopic(), containsString(thingUID.getAsString()));
        assertThat(firmwareStatusInfoEvent.getThingUID(), is(thingUID));
        assertThat(firmwareStatusInfoEvent.getFirmwareStatusInfo(), is(expectedInfo));
    }

    private void assertFailedFirmwareUpdate(ThingUID thingUID, Event event, String expectedErrorMessage) {
        assertThat(event, is(instanceOf(FirmwareUpdateResultInfoEvent.class)));
        FirmwareUpdateResultInfoEvent firmwareUpdateResultInfoEvent = (FirmwareUpdateResultInfoEvent) event;

        assertThat(firmwareUpdateResultInfoEvent.getTopic(), containsString(THING1_UID.getAsString()));
        assertThat(firmwareUpdateResultInfoEvent.getThingUID(), is(THING1_UID));
        assertThat(firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getResult(),
                is(FirmwareUpdateResult.ERROR));
        assertThat(firmwareUpdateResultInfoEvent.getFirmwareUpdateResultInfo().getErrorMessage(),
                is(expectedErrorMessage));

    }

    private FirmwareUpdateHandler addHandler(Thing thing) {
        FirmwareUpdateHandler mockHandler = mock(FirmwareUpdateHandler.class);
        when(mockHandler.getThing()).thenReturn(thing);
        doReturn(true).when(mockHandler).isUpdateExecutable();
        doAnswer(invocation -> {
            Firmware firmware = (Firmware) invocation.getArguments()[0];
            thing.setProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.getVersion());
            return null;
        }).when(mockHandler).updateFirmware(any(Firmware.class), any(ProgressCallback.class));
        firmwareUpdateService.addFirmwareUpdateHandler(mockHandler);
        return mockHandler;
    }

    private void updateConfig(int period, int delay, TimeUnit timeUnit) throws IOException {
        Map<String, Object> properties = new Hashtable<>();
        properties.put(FirmwareUpdateService.PERIOD_CONFIG_KEY, period);
        properties.put(FirmwareUpdateService.DELAY_CONFIG_KEY, delay);
        properties.put(FirmwareUpdateService.TIME_UNIT_CONFIG_KEY, timeUnit.name());
        firmwareUpdateService.modified(properties);
    }

}
