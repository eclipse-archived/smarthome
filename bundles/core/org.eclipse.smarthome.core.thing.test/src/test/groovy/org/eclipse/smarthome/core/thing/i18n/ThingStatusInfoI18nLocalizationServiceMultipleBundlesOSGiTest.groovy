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
package org.eclipse.smarthome.core.thing.i18n

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingStatusDetail
import org.eclipse.smarthome.core.thing.ThingStatusInfo
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.magic.binding.handler.MagicColorLightHandler
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.service.component.ComponentContext

/**
 * OSGi tests for the {@link ThingStatusInfoI18nLocalizationServiceOSGiTest} that involve looking up translations
 * in a bundle different of the ThingHandler's bundle. For this purpose, this test provides a thing handler that
 * extends a thing handler from the magic bundle.
 *
 * @author Henning Sudbrock - Initial contribution
 */
class ThingStatusInfoI18nLocalizationServiceMultipleBundlesOSGiTest extends OSGiTest {

    def thing
    def thingStatusInfoI18nLocalizationService
    def managedThingProvider
    def defaultLocale

    @Before
    void setup() {
        def localeProvider = getService(LocaleProvider)
        assertThat localeProvider, is(notNullValue())
        defaultLocale = localeProvider.getLocale()

        setDefaultLocale(Locale.ENGLISH)

        registerVolatileStorageService()

        def thingHandlerFactory = new ExtendingOtherBundleThingHandlerFactory()
        thingHandlerFactory.activate([getBundleContext: { bundleContext }] as ComponentContext)
        registerService(thingHandlerFactory, ThingHandlerFactory.class.name)

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

    @Test
    void 'assert that translation from superclass bundle is used'() {
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/channel-type.magic.alert.label"))
        def info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMANY)
        assertThat info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Alarm"))
    }

    @Test
    void 'assert that no translation is used if not found in superclass'() {
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/some-fake-label-for-this-test"))
        def info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMANY)
        assertThat info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/some-fake-label-for-this-test"))
    }

    @Test
    void  'assert that arguments for description translation are also translated'() {
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/offline.with-two-params [@text/channel-type.magic.alert.label, 60]"))
        def info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMANY)
        assertThat info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing ist wegen Alarm offline. Bitte versuchen Sie es in 60 Sekunden erneut."))
    }

    class ExtendingOtherBundleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            new ExtendingOtherBundleThingHandler(thing)
        }
    }

    class ExtendingOtherBundleThingHandler extends MagicColorLightHandler {
        public ExtendingOtherBundleThingHandler(Thing thing) {
            super(thing)
        }
    }
}
