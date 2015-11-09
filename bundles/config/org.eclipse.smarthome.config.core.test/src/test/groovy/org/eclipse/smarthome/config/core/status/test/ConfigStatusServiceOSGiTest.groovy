/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.status.ConfigStatusInfo
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage
import org.eclipse.smarthome.config.core.status.ConfigStatusProvider
import org.eclipse.smarthome.config.core.status.ConfigStatusService
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap

/**
 * Testing the {@link ConfigStatusService} OSGi service.
 *
 * @author Thomas Höfer - Initial contribution
 */
class ConfigStatusServiceOSGiTest extends OSGiTest {

    ConfigStatusService configStatusService

    private static final String ENTITY_ID1 = "entity1"
    private static final String ENTITY_ID2 = "entity2"
    private static final String UNKNOWN = "unknown"

    private static final Locale LOCALE_DE = new Locale("de")
    private static final Locale LOCALE_EN = new Locale("en")

    private static final String PARAM1="param1"
    private static final String PARAM2="param2"

    private static final String MSG1_DE = "German 1"
    private static final String MSG2_DE = "German 2"
    private static final String MSG3_DE = "German 3"
    private static final String MSG1_EN = "English 1"
    private static final String MSG2_EN = "English 2"
    private static final String MSG3_EN = "English 3"

    private final ConfigStatusMessage PARAM1_MSG1_DE = ConfigStatusMessage.Builder.information(PARAM1, MSG1_DE).build()
    private final ConfigStatusMessage PARAM1_MSG2_DE = ConfigStatusMessage.Builder.warning(PARAM1, MSG2_DE).withStatusCode(1).build()
    private final ConfigStatusMessage PARAM1_MSG3_DE = ConfigStatusMessage.Builder.error(PARAM1, MSG3_DE).withStatusCode(1).build()
    private final ConfigStatusMessage PARAM2_MSG1_DE = ConfigStatusMessage.Builder.pending(PARAM2, MSG1_DE).build()
    private final ConfigStatusMessage PARAM1_MSG1_EN = ConfigStatusMessage.Builder.information(PARAM1, MSG1_EN).build()
    private final ConfigStatusMessage PARAM1_MSG2_EN = ConfigStatusMessage.Builder.warning(PARAM1, MSG2_EN).withStatusCode(1).build()
    private final ConfigStatusMessage PARAM1_MSG3_EN = ConfigStatusMessage.Builder.error(PARAM1, MSG3_EN).withStatusCode(1).build()
    private final ConfigStatusMessage PARAM2_MSG1_EN = ConfigStatusMessage.Builder.pending(PARAM2, MSG1_EN).build()

    private final SetMultimap<String, ConfigStatusMessage> messagesPerEntityDe = HashMultimap.create()
    private final SetMultimap<String, ConfigStatusMessage> messagesPerEntityEn = HashMultimap.create()

    ConfigStatusServiceOSGiTest() {
        messagesPerEntityDe.put(ENTITY_ID1, PARAM1_MSG1_DE)
        messagesPerEntityDe.put(ENTITY_ID1, PARAM1_MSG2_DE)
        messagesPerEntityDe.put(ENTITY_ID1, PARAM1_MSG3_DE)
        messagesPerEntityDe.put(ENTITY_ID2, PARAM2_MSG1_DE)

        messagesPerEntityEn.put(ENTITY_ID1, PARAM1_MSG1_EN)
        messagesPerEntityEn.put(ENTITY_ID1, PARAM1_MSG2_EN)
        messagesPerEntityDe.put(ENTITY_ID1, PARAM1_MSG3_EN)
        messagesPerEntityEn.put(ENTITY_ID2, PARAM2_MSG1_EN)
    }

    @Before
    void setUp() {
        registerService([post:{event->}] as EventPublisher)

        configStatusService = getService(ConfigStatusService)
        assertThat configStatusService, is(notNullValue())

        registerConfigStatusProviderMock(ENTITY_ID1)
        registerConfigStatusProviderMock(ENTITY_ID2)
    }

    @Test
    void 'assert that null is returned for entity without registered validator'() {
        ConfigStatusInfo info = configStatusService.getConfigStatus("unknown", null);
        assertThat "Config status info is null.", info, is(null)
    }

    @Test
    void 'assert that config status messages are returned for entity'() {
        test(ENTITY_ID1, LOCALE_DE, messagesPerEntityDe.get(ENTITY_ID1))
        test(ENTITY_ID2, LOCALE_DE, messagesPerEntityDe.get(ENTITY_ID2))
    }

    @Test
    void 'assert that locale is used'() {
        test(ENTITY_ID1, LOCALE_DE, messagesPerEntityDe.get(ENTITY_ID1))
        test(ENTITY_ID2, LOCALE_DE, messagesPerEntityDe.get(ENTITY_ID2))

        test(ENTITY_ID1, LOCALE_EN, messagesPerEntityEn.get(ENTITY_ID1))
        test(ENTITY_ID2, LOCALE_EN, messagesPerEntityEn.get(ENTITY_ID2))
    }

    private void test(String entityId, Locale locale, Collection<ConfigStatusMessage> expectedMessages) {
        ConfigStatusInfo info = configStatusService.getConfigStatus(entityId, locale)
        assertConfigStatusInfo(info, new ConfigStatusInfo(expectedMessages))
    }

    private void assertConfigStatusInfo(ConfigStatusInfo actual, ConfigStatusInfo expected) {
        assertThat actual, equalTo(expected)
    }

    private void registerConfigStatusProviderMock(String entityId) {
        registerService([
            supportsEntity: { opEntityId ->
                entityId.equals(opEntityId) ? true : false
            },
            getConfigStatus: { locale ->
                LOCALE_DE.equals(locale) ? new ConfigStatusInfo(messagesPerEntityDe.get(entityId)) : new ConfigStatusInfo(messagesPerEntityEn.get(entityId))
            },
            setConfigStatusCallback: {
            }
        ]  as ConfigStatusProvider)
    }
}
