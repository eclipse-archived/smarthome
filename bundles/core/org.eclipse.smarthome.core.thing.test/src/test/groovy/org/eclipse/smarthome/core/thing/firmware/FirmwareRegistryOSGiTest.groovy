/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.firmware

import static org.eclipse.smarthome.core.thing.firmware.Constants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware
import org.eclipse.smarthome.core.thing.testutil.i18n.DefaultLocaleSetter
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.cm.ConfigurationAdmin

/**
 * Testing the {@link FirmwareRegistry}.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Dimitar Ivanov - Adapted the test for registry using thing and version instead of firmware UID
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

    def basicFirmwareProviderMock = [
        getFirmware: {Thing thing, version, locale ->
            if(!thing.equals(thing1)) {
                return null
            }

            if(locale.equals(Locale.ENGLISH)) {
                if(version.equals(FW111_EN.getVersion())) {
                    FW111_EN
                } else if(version.equals(FW112_EN.getVersion())){
                    FW112_EN
                }
            }else {
                if(version.equals(FW111_DE.getVersion())) {
                    FW111_DE
                } else if(version.equals(FW112_DE.getVersion())){
                    FW112_DE
                }
            }
        },
        getFirmwares:{ Thing thing, locale->
            if(!thing.equals(thing1)) {
                return [] as Set
            }
            if(locale.equals(Locale.ENGLISH)) {
                [FW111_EN, FW112_EN
                ] as Set
            }else {
                [FW111_DE, FW112_DE
                ] as Set
            }
        }] as FirmwareProvider

    def additionalFirmwareProviderMock = [
        getFirmware: { thing, version, locale ->
            if(version.equals(FW111_FIX_EN.getVersion())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FW111_FIX_EN
                } else {
                    FW111_FIX_DE
                }
            } else if(version.equals(FWALPHA_EN.getVersion())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWALPHA_EN
                } else {
                    FWALPHA_DE
                }
            } else if(version.equals(FWBETA_EN.getVersion())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWBETA_EN
                } else {
                    FWBETA_DE
                }
            } else if(version.equals(FWGAMMA_EN.getVersion())) {
                if(locale.equals(Locale.ENGLISH)) {
                    FWGAMMA_EN
                } else {
                    FWGAMMA_DE
                }
            }
        },
        getFirmwares: {Thing thing, locale ->
            if(thing.getThingTypeUID().equals(THING_TYPE_UID1)) {
                if(locale.equals(Locale.ENGLISH)) {
                    [FW111_FIX_EN] as Set
                } else {
                    [FW111_FIX_DE] as Set
                }
            } else if(thing.getThingTypeUID().equals(THING_TYPE_UID2)){
                if(locale.equals(Locale.ENGLISH)) {
                    [FWALPHA_EN, FWBETA_EN, FWGAMMA_EN] as Set
                } else {
                    [FWALPHA_DE, FWBETA_DE, FWGAMMA_DE] as Set
                }
            }
        }] as FirmwareProvider

    Thing thing1
    Thing thing2
    Thing thing3

    @Before
    void setup() {
        def localeProvider = getService(LocaleProvider)
        assertThat localeProvider, is(notNullValue())
        defaultLocale = localeProvider.getLocale()

        new DefaultLocaleSetter(getService(ConfigurationAdmin)).setDefaultLocale(Locale.ENGLISH)
        waitForAssert {
            assertThat localeProvider.getLocale(), is(Locale.ENGLISH)
        }

        firmwareRegistry = getService(FirmwareRegistry)
        assertThat firmwareRegistry, is(notNullValue())
        firmwareRegistry.firmwareProviders.clear()

        registerService(basicFirmwareProviderMock)

        thing1 = ThingBuilder.create(THING_TYPE_UID1, THING1_ID).build();
        thing2 = ThingBuilder.create(THING_TYPE_UID1, THING2_ID).build();
        thing3 = ThingBuilder.create(THING_TYPE_UID2, THING3_ID).build();
    }

    @After
    void teardown() {
        new DefaultLocaleSetter(getService(ConfigurationAdmin)).setDefaultLocale(defaultLocale)
        waitForAssert {
            assertThat getService(LocaleProvider).getLocale(), is(defaultLocale)
        }
    }

    @Test
    void 'assert that registry works with single provider'(){
        def firmwares = firmwareRegistry.getFirmwares(thing1)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(thing1, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(thing1, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_DE)
        assertThat firmwares[FW2], is(FW111_DE)

        def firmware = firmwareRegistry.getFirmware(thing1, V111);
        assertThat firmware, is(FW111_EN)

        firmware = firmwareRegistry.getFirmware(thing1, V112, Locale.ENGLISH);
        assertThat firmware, is(FW112_EN)

        firmware = firmwareRegistry.getFirmware(thing1, V112, Locale.GERMAN);
        assertThat firmware, is(FW112_DE)
    }

    @Test
    void 'assert that registry works with several providers'(){
        def firmwares = firmwareRegistry.getFirmwares(thing1)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)

        firmwares = firmwareRegistry.getFirmwares(thing2)
        assertThat firmwares.size(), is(0)

        registerService(additionalFirmwareProviderMock)

        firmwares = firmwareRegistry.getFirmwares(thing1)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_FIX_EN)
        assertThat firmwares[FW3], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(thing1, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_FIX_EN)
        assertThat firmwares[FW3], is(FW111_EN)

        firmwares = firmwareRegistry.getFirmwares(thing1, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_DE)
        assertThat firmwares[FW2], is(FW111_FIX_DE)
        assertThat firmwares[FW3], is(FW111_DE)

        def firmware = firmwareRegistry.getFirmware(thing1, V111_FIX);
        assertThat firmware, is(FW111_FIX_EN)

        firmware = firmwareRegistry.getFirmware(thing1, V111_FIX, Locale.ENGLISH);
        assertThat firmware, is(FW111_FIX_EN)

        firmware = firmwareRegistry.getFirmware(thing1, V111_FIX, Locale.GERMAN);
        assertThat firmware, is(FW111_FIX_DE)

        firmwares = firmwareRegistry.getFirmwares(thing3)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_EN)
        assertThat firmwares[FW2], is(FWBETA_EN)
        assertThat firmwares[FW3], is(FWALPHA_EN)

        firmwares = firmwareRegistry.getFirmwares(thing3, Locale.ENGLISH)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_EN)
        assertThat firmwares[FW2], is(FWBETA_EN)
        assertThat firmwares[FW3], is(FWALPHA_EN)

        firmwares = firmwareRegistry.getFirmwares(thing3, Locale.GERMAN)
        assertThat firmwares.size(), is(EXPECT_3_FIRMWARES)
        assertThat firmwares[FW1], is(FWGAMMA_DE)
        assertThat firmwares[FW2], is(FWBETA_DE)
        assertThat firmwares[FW3], is(FWALPHA_DE)

        firmware = firmwareRegistry.getFirmware(thing3, VALPHA)
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VALPHA, Locale.ENGLISH)
        assertThat firmware, is(FWALPHA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VALPHA, Locale.GERMAN)
        assertThat firmware, is(FWALPHA_DE)

        firmware = firmwareRegistry.getFirmware(thing3, VBETA)
        assertThat firmware, is(FWBETA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VBETA, Locale.ENGLISH)
        assertThat firmware, is(FWBETA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VBETA, Locale.GERMAN)
        assertThat firmware, is(FWBETA_DE)

        firmware = firmwareRegistry.getFirmware(thing3, VGAMMA)
        assertThat firmware, is(FWGAMMA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VGAMMA, Locale.ENGLISH)
        assertThat firmware, is(FWGAMMA_EN)

        firmware = firmwareRegistry.getFirmware(thing3, VGAMMA, Locale.GERMAN)
        assertThat firmware, is(FWGAMMA_DE)
    }

    @Test
    void 'assert that registry returns empty set for thing and getFirmwares operation'(){
        def firmwares = firmwareRegistry.getFirmwares(thing3)
        assertThat firmwares.size(), is(EXPECT_0_FIRMWARES)

        firmwares = firmwareRegistry.getFirmwares(thing3,null)
        assertThat firmwares.size(), is(EXPECT_0_FIRMWARES)
    }

    @Test
    void 'assert that registry returns null for unknown firmware uid and getFirmware operation'(){
        def firmware = firmwareRegistry.getFirmware(thing1, Constants.UNKNOWN)
        assertThat firmware, is(null)

        firmware = firmwareRegistry.getFirmware(thing2, VALPHA, Locale.GERMAN);
        assertThat firmware, is(null)
    }

    void 'assert that registry returns correct latest firmware'(){
        registerService(additionalFirmwareProviderMock)

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
    void 'assert that firmware properties are provided'(){
        registerService(additionalFirmwareProviderMock)

        def firmware = firmwareRegistry.getFirmware(thing3,FWALPHA_DE.getVersion())
        assertThat firmware, is(notNullValue())
        assertThat firmware.getProperties(), is(notNullValue())
        assertThat firmware.getProperties().isEmpty(), is(true)

        firmware = firmwareRegistry.getFirmware(thing3,FWBETA_DE.getVersion())
        assertThat firmware, is(notNullValue())
        assertThat firmware.getProperties(), is(notNullValue())
        assertThat firmware.getProperties().size(), is(1)
        assertThat firmware.getProperties().get(Firmware.PROPERTY_REQUIRES_FACTORY_RESET), is("true")

        firmware = firmwareRegistry.getFirmware(thing3, FWGAMMA_DE.getVersion())
        assertThat firmware, is(notNullValue())
        assertThat firmware.getProperties(), is(notNullValue())
        assertThat firmware.getProperties().size(), is(2)
        assertThat firmware.getProperties().get("prop1"), is("a")
        assertThat firmware.getProperties().get("prop2"), is("b")
    }

    @Test(expected=UnsupportedOperationException)
    void 'assert that firmware properties are immutable'(){
        def fw = firmwareRegistry.getFirmware(thing1, FW112_EN.getVersion())
        assertThat fw, is(notNullValue())
        fw.getProperties().put("test", null)
    }

    @Test
    void 'assert that registry returns null for unknown thing type uid and getLatestFirmware operation'() {
        def firmware = firmwareRegistry.getLatestFirmware(thing3);
        assertThat firmware, is(null)

        firmware = firmwareRegistry.getLatestFirmware(thing3, Locale.GERMAN);
        assertThat firmware, is(null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that thing is checked for getFirmware without locale'() {
        firmwareRegistry.getFirmware(null,null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that firmware uid is checked for getFirmware with locale'() {
        firmwareRegistry.getFirmware(null,null,null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that firmware uid is checked for getFirmwares without locale'() {
        firmwareRegistry.getFirmwares(null)
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that firmware uid is checked for getFirmwares with locale'() {
        firmwareRegistry.getFirmwares(null, null)
    }

    @Test
    void 'assert firmware provider returning null results'(){
        unregisterService(basicFirmwareProviderMock)

        def nullProviderMock = [
            getFirmware: {Thing thing, version, locale ->
                return null;
            },
            getFirmwares:{ Thing thing, locale->
                return null;
            }] as FirmwareProvider

        registerService(nullProviderMock)

        firmwareRegistry.getFirmwares(thing3);
        firmwareRegistry.getFirmware(thing3,FWALPHA_DE.getVersion())
    }

    @Test
    void 'assert firmware that invalid providers are skipped'(){
        def nullProviderMock = [
            getFirmware: {Thing thing, version, locale ->
                return null;
            },
            getFirmwares:{ Thing thing, locale->
                return [null] as Set
            }] as FirmwareProvider

        registerService(nullProviderMock)

        def firmwares = firmwareRegistry.getFirmwares(thing1)
        assertThat firmwares.size(), is(EXPECT_2_FIRMWARES)
        assertThat firmwares[FW1], is(FW112_EN)
        assertThat firmwares[FW2], is(FW111_EN)
    }
}