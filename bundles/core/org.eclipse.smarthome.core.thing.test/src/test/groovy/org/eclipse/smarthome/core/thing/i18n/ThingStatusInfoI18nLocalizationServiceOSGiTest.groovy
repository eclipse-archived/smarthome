/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.i18n

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingStatusInfo
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 * OSGi tests for the {@link ThingStatusInfoI18nLocalizationServiceOSGiTest}.
 *
 * @author Thomas Höfer - Initial contribution
 */
class ThingStatusInfoI18nLocalizationServiceOSGiTest extends OSGiTest {

    def thing
    def thingStatusInfoI18nLocalizationService
    def managedThingProvider
    def defaultLocale

    @Test
    void 'assert that thing status info is not changed if there is no description'() {
        def orgThingStatusInfo = thing.getStatusInfo()
        assertThat orgThingStatusInfo.getDescription(), is(null)
        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(orgThingStatusInfo)
    }

    @Test
    void 'assert that thing status info is not changed if any description is used'() {
        def expected = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "The description.")
        thing.getHandler().setThingStatusInfo(expected)

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(expected)
    }

    @Test
    void 'assert that thing status info is localized for online status without an argument'() {
        thing.getHandler().setThingStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online"))

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing is online."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing ist online."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing is online."))
    }

    @Test
    void 'assert that thing status info is localized for offline status without an argument'() {
        thing.getHandler().setThingStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.without-param"))

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing is offline."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing ist offline."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing is offline."))
    }

    @Test
    void 'assert that thing status info is localized for offline status with one argument'() {
        thing.getHandler().setThingStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.with-one-param [\"5\"]"))

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of 5 failed logins."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing ist nach 5 fehlgeschlagenen Anmeldeversuchen offline."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of 5 failed logins."))
    }

    @Test
    void 'assert that thing status info is localized for offline status with two arguments'() {
        thing.getHandler().setThingStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.with-two-params [\"@text/communication-problems\", \"120\"]"))

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of communication problems. Pls try again after 120 seconds."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing ist wegen Kommunikationsprobleme offline. Bitte versuchen Sie es in 120 Sekunden erneut."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of communication problems. Pls try again after 120 seconds."))
    }

    @Test
    void 'assert that thing status info is localized for malformed arguments'() {
        thing.getHandler().setThingStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.with-two-params   [   \"@text/communication-problems\", ,      \"120\"  ]"))

        def thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of communication problems. Pls try again after 120 seconds."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing ist wegen Kommunikationsprobleme offline. Bitte versuchen Sie es in 120 Sekunden erneut."))

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH)
        assertThat thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Thing is offline because of communication problems. Pls try again after 120 seconds."))
    }

    @Test
    void 'assert that unhandled thing can be handled'() {
        def info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(ThingBuilder.create(new ThingTypeUID("xyz:abc"), "123").build(), null)
        assertThat info, is(ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build())
    }

    @Test(expected=IllegalArgumentException)
    void 'assert that given null thing uid is rejected'() {
        thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(null, null)
    }

    @Before
    void setup() {
        def localeProvider = getService(LocaleProvider)
        assertThat localeProvider, is(notNullValue())
        defaultLocale = localeProvider.getLocale()

        setDefaultLocale(Locale.ENGLISH)

        registerVolatileStorageService()

        def simpleThingHandlerFactory = new SimpleThingHandlerFactory()
        simpleThingHandlerFactory.activate([getBundleContext: { bundleContext }] as ComponentContext)
        registerService(simpleThingHandlerFactory, ThingHandlerFactory.class.name)

        thing = ThingBuilder.create(new ThingTypeUID("aaa:bbb"), "ccc").build()

        managedThingProvider = getService(ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        managedThingProvider.add(thing)

        waitForAssert({
            assertThat thing.getStatus(), is(ThingStatus.ONLINE)
        })

        thingStatusInfoI18nLocalizationService = getService(ThingStatusInfoI18nLocalizationService)
        assertThat thingStatusInfoI18nLocalizationService, is(notNullValue())
    }

    @After
    void tearDown() {
        managedThingProvider.remove(thing.getUID())
        setDefaultLocale(defaultLocale)
    }

    class SimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            new SimpleThingHandler(thing)
        }
    }

    class SimpleThingHandler extends BaseThingHandler {

        SimpleThingHandler(Thing thing) {
            super(thing)
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            //do nothing
        }

        @Override
        public void initialize() {
            updateStatus(ThingStatus.ONLINE);
        }

        void setThingStatusInfo(ThingStatusInfo thingStatusInfo) {
            updateStatus(thingStatusInfo.getStatus(), thingStatusInfo.getStatusDetail(), thingStatusInfo.getDescription())
        }
    }
}