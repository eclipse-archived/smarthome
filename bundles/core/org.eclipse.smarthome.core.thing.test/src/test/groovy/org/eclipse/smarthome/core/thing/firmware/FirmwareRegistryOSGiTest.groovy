/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware

import static org.eclipse.smarthome.core.thing.firmware.Constants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Testing the {@link FirmwareRegistry}.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class FirmwareRegistryOSGiTest extends OSGiTest {

    private static final int EXPECT_0_FIRMWARES = 0
    private static final int EXPECT_2_FIRMWARES = 2
    private static final int EXPECT_3_FIRMWARES = 3
    private static final int FW1 = 0
    private static final int FW2 = 1
    private static final int FW3 = 2

    private Locale defaultLocale

    private FirmwareRegistry firmwareRegistry

    def mock1 = [
        getFirmware: { firmwareUID, locale ->
            if(locale.equals(Locale.ENGLISH)) {
                if(firmwareUID.equals(FW111_EN.getUID())) {
                    FW111_EN
                } else if(firmwareUID.equals(FW112_EN.getUID())){
                    FW112_EN
                }
            } else {
                if(firmwareUID.equals(FW111_DE.getUID())) {
                    FW111_DE
                } else if(firmwareUID.equals(FW112_DE.getUID())){
                    FW112_DE
                }
            }
        },
        getFirmwares: { thingTypeUID, locale ->
            if(!thingTypeUID.equals(THING_TYPE_UID1)) {
                return [] as Set
            }
            if(locale.equals(Locale.ENGLISH)) {
                [
                    FW111_EN,
                    FW112_EN
                ] as Set
            } else {
                [
                    FW111_DE,
                    FW112_DE
                ] as Set
            }
        }] as FirmwareProvider

    def mock2 = [
        getFirmware: { firmwareUID, locale ->
            if(firmwareUID.equals(FW111_FIX_EN.getUID())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FW111_FIX_EN
                } else {
                    FW111_FIX_DE
                }
            } else if(firmwareUID.equals(FWALPHA_EN.getUID())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWALPHA_EN
                } else {
                    FWALPHA_DE
                }
            } else if(firmwareUID.equals(FWBETA_EN.getUID())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWBETA_EN
                } else {
                    FWBETA_DE
                }
            } else if(firmwareUID.equals(FWGAMMA_EN.getUID())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWGAMMA_EN
                } else {
                    FWGAMMA_DE
                }
            }
        },
        getFirmwares: { thingTypeUID, locale ->
            if(thingTypeUID.equals(THING_TYPE_UID1)) {
                if(locale.equals(Locale.ENGLISH)) {
                    [FW111_FIX_EN] as Set
                } else {
                    [FW111_FIX_DE] as Set
                }
            } else if(thingTypeUID.equals(THING_TYPE_UID2)){
                if(locale.equals(Locale.ENGLISH)) {
                    [
                        FWALPHA_EN,
                        FWBETA_EN,
                        FWGAMMA_EN
                    ] as Set
                } else {
                    [
                        FWALPHA_DE,
                        FWBETA_DE,
                        FWGAMMA_DE] as Set
                }
            }
        }] as FirmwareProvider

    @Before
    void setup() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)

        firmwareRegistry = getService(FirmwareRegistry)
        assertThat firmwareRegistry, is(notNullValue())
        firmwareRegistry.firmwareProviders.clear()

        registerService(mock1)
    }

    @After
    void teardown() {
        Locale.setDefault(defaultLocale)
        unregisterMocks()
    }

    @Test
    void 'assert that registry works with single provider'() {
        def firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_DE)
        assertThat firmwares[FW2], is(FW111_DE)

        def firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V111));
        assertThat firmware, is(FW111_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V112), Locale.ENGLISH);
        assertThat firmware, is(FW112_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V112), Locale.GERMAN);
        assertThat firmware, is(FW112_DE)
    }

    @Test
    void 'assert that registry works with several providers'() {
        def firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID2)
        assertThat firmwares.size(), is(0)

        registerService(mock2)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_FIX_EN)
        assertThat firmwares[FW3], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_FIX_EN)
        assertThat firmwares[FW3], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID1, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_DE)
        assertThat firmwares[FW2], is(FW111_FIX_DE)
        assertThat firmwares[FW3], is(FW111_DE)

        def firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V111_FIX));
        assertThat firmware, is(FW111_FIX_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V111_FIX), Locale.ENGLISH);
        assertThat firmware, is(FW111_FIX_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, V111_FIX), Locale.GERMAN);
        assertThat firmware, is(FW111_FIX_DE)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID2)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_EN)
        assertThat firmwares[FW2], is(FWBETA_EN)
        assertThat firmwares[FW3], is(FWALPHA_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID2, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_EN)
        assertThat firmwares[FW2], is(FWBETA_EN)
        assertThat firmwares[FW3], is(FWALPHA_EN)

        firmwares = firmwareRegistry.getFirmwares(THING_TYPE_UID2, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_DE)
        assertThat firmwares[FW2], is(FWBETA_DE)
        assertThat firmwares[FW3], is(FWALPHA_DE)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VALPHA))
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VALPHA), Locale.ENGLISH)
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VALPHA), Locale.GERMAN)
        assertThat firmware, is(FWALPHA_DE)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VBETA))
        assertThat firmware, is(FWBETA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VBETA), Locale.ENGLISH)
        assertThat firmware, is(FWBETA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VBETA), Locale.GERMAN)
        assertThat firmware, is(FWBETA_DE)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VGAMMA))
        assertThat firmware, is(FWGAMMA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VGAMMA), Locale.ENGLISH)
        assertThat firmware, is(FWGAMMA_EN)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VGAMMA), Locale.GERMAN)
        assertThat firmware, is(FWGAMMA_DE)
    }

    @Test
    void 'assert that registry returns empty set for unknown thing type uid and getFirmwares operation'() {
        def firmwares = firmwareRegistry.getFirmwares(UNKNOWN_THING_TYPE_UID)
        assertThat firmwares.size(), is(EXPECT_0_FIRMWARES)

        firmwares = firmwareRegistry.getFirmwares(UNKNOWN_THING_TYPE_UID)
        assertThat firmwares.size(), is(EXPECT_0_FIRMWARES)

        firmwares = firmwareRegistry.getFirmwares(UNKNOWN_THING_TYPE_UID)
        assertThat firmwares.size(), is(EXPECT_0_FIRMWARES)
    }


    @Test
    void 'assert that registry returns null for unknown firmware uid and getFirmware operation'() {
        def firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID1, Constants.UNKNOWN))
        assertThat firmware, is(null)

        firmware = firmwareRegistry.getFirmware(new FirmwareUID(THING_TYPE_UID2, VALPHA), Locale.GERMAN);
        assertThat firmware, is(null)
    }

    void 'assert that registry returns correct latest firmware'() {
        registerService(mock2)

        def firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID1)
        assertThat firmware, is(FW112_EN)

        firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID1, Locale.ENGLISH)
        assertThat firmware, is(FW112_EN)

        firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID1, Locale.GERMAN)
        assertThat firmware, is(FW112_DE)

        firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID2)
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID2, Locale.ENGLISH)
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getLatestFirmware(THING_TYPE_UID2, Locale.GERMAN)
        assertThat firmware, is(FWALPHA_DE)
    }

    @Test
    void 'void assert that registry returns null for unknown thing type uid and getLatestFirmware operation()'() {
        def firmware = firmwareRegistry.getLatestFirmware(UNKNOWN_THING_TYPE_UID);
        assertThat firmware, is(null)

        firmware = firmwareRegistry.getLatestFirmware(UNKNOWN_THING_TYPE_UID, Locale.GERMAN);
        assertThat firmware, is(null)
    }

    @Test(expected=NullPointerException)
    void 'assert that firmware uid is checked for getFirmware without locale'() {
        firmwareRegistry.getFirmware(null)
    }

    @Test(expected=NullPointerException)
    void 'assert that firmware uid is checked for getFirmware with locale'() {
        firmwareRegistry.getFirmware(null, null)
    }

    @Test(expected=NullPointerException)
    void 'assert that firmware uid is checked for getFirmwares without locale'() {
        firmwareRegistry.getFirmwares(null)
    }

    @Test(expected=NullPointerException)
    void 'assert that firmware uid is checked for getFirmwares with locale'() {
        firmwareRegistry.getFirmwares(null, null)
    }
}
