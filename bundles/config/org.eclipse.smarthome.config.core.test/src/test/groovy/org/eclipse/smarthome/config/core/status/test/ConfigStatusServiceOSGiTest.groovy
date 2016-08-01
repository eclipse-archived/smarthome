/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.text.MessageFormat

import org.eclipse.smarthome.config.core.status.ConfigStatusInfo
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage
import org.eclipse.smarthome.config.core.status.ConfigStatusProvider
import org.eclipse.smarthome.config.core.status.ConfigStatusService
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.i18n.I18nProvider
import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

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

    private static final String PARAM1 = "param1"
    private static final String PARAM2= "param2"
    private static final String PARAM3= "param3"

    private static final String MSG_KEY1 = "msg1"
    private static final String MSG_KEY2 = "msg2"
    private static final String MSG_KEY3 = "msg3"

    private static final String MSG1_DE = "German 1"
    private static final String MSG2_DE = "German 2 - {0}"
    private static final String MSG3_DE = "German 3 - {0}"
    private static final String MSG1_EN = "English 1"
    private static final String MSG2_EN = "English 2 - {0}"
    private static final String MSG3_EN = "English 3 - {0}"

    private static final String ARGS = "args"

    private final ConfigStatusMessage PARAM1_MSG1 = ConfigStatusMessage.Builder.information(PARAM1).withMessageKeySuffix(MSG_KEY1).build()
    private final ConfigStatusMessage PARAM2_MSG2 = ConfigStatusMessage.Builder.warning(PARAM2).withMessageKeySuffix(MSG_KEY2).withStatusCode(1).withArguments(ARGS).build()
    private final ConfigStatusMessage PARAM3_MSG3 = ConfigStatusMessage.Builder.error(PARAM3).withMessageKeySuffix(MSG_KEY3).withStatusCode(2).withArguments(ARGS).build()

    private final Collection messagesEntity1 = new ArrayList()
    private final Collection messagesEntity2 = new ArrayList()

    private final Collection messagesEntity1De = new ArrayList()
    private final Collection messagesEntity2De = new ArrayList()

    private final Collection messagesEntity1En = new ArrayList()
    private final Collection messagesEntity2En = new ArrayList()

    @Before
    void setUp() {
        messagesEntity1.add(PARAM1_MSG1)
        messagesEntity1.add(PARAM2_MSG2)
        messagesEntity1De.add(new ConfigStatusMessage(PARAM1_MSG1.parameterName, PARAM1_MSG1.type, MSG1_DE, PARAM1_MSG1.statusCode))
        messagesEntity1De.add(new ConfigStatusMessage(PARAM2_MSG2.parameterName, PARAM2_MSG2.type, MessageFormat.format(MSG2_DE, ARGS), PARAM2_MSG2.statusCode))
        messagesEntity1En.add(new ConfigStatusMessage(PARAM1_MSG1.parameterName, PARAM1_MSG1.type, MSG1_EN, PARAM1_MSG1.statusCode))
        messagesEntity1En.add(new ConfigStatusMessage(PARAM2_MSG2.parameterName, PARAM2_MSG2.type, MessageFormat.format(MSG2_EN, ARGS), PARAM2_MSG2.statusCode))

        messagesEntity2.add(PARAM2_MSG2)
        messagesEntity2.add(PARAM3_MSG3)
        messagesEntity2De.add(new ConfigStatusMessage(PARAM2_MSG2.parameterName, PARAM2_MSG2.type, MessageFormat.format(MSG2_DE, ARGS), PARAM2_MSG2.statusCode))
        messagesEntity2De.add(new ConfigStatusMessage(PARAM3_MSG3.parameterName, PARAM3_MSG3.type, MessageFormat.format(MSG3_DE, ARGS), PARAM3_MSG3.statusCode))
        messagesEntity2En.add(new ConfigStatusMessage(PARAM2_MSG2.parameterName, PARAM2_MSG2.type, MessageFormat.format(MSG2_EN, ARGS), PARAM2_MSG2.statusCode))
        messagesEntity2En.add(new ConfigStatusMessage(PARAM3_MSG3.parameterName, PARAM3_MSG3.type, MessageFormat.format(MSG3_EN, ARGS), PARAM3_MSG3.statusCode))

        registerService([post:{ event->
            }] as EventPublisher)

        registerService([getLocale: {
                return new Locale("en", "US")
            }] as LocaleProvider)
        registerI18nProvider()

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
        test(ENTITY_ID1, LOCALE_DE, messagesEntity1De)
        test(ENTITY_ID2, LOCALE_DE, messagesEntity2De)

        test(ENTITY_ID1, LOCALE_EN, messagesEntity1En)
        test(ENTITY_ID2, LOCALE_EN, messagesEntity2En)
    }

    private void test(String entityId, Locale locale, Collection<ConfigStatusMessage> expectedMessages) {
        ConfigStatusInfo info = configStatusService.getConfigStatus(entityId, locale)
        assertConfigStatusInfo(info, new ConfigStatusInfo(expectedMessages))
    }

    private void assertConfigStatusInfo(ConfigStatusInfo actual, ConfigStatusInfo expected) {
        assertThat actual, equalTo(expected)
    }

    private void registerConfigStatusProviderMock(final String entityId) {
        registerService([
            supportsEntity: { opEntityId ->
                entityId.equals(opEntityId) ? true : false
            },
            getConfigStatus: {
                entityId.equals(ENTITY_ID1) ? messagesEntity1 : messagesEntity2;
            },
            setConfigStatusCallback: {
            }
        ]  as ConfigStatusProvider)
    }

    private void registerI18nProvider() {
        registerService([
            getText: { bundle, key, defaultText, locale, args ->
                if(locale.equals(LOCALE_DE)) {
                    if(key.endsWith(MSG_KEY1)) {
                        MSG1_DE
                    } else if(key.endsWith(MSG_KEY2)) {
                        MessageFormat.format(MSG2_DE, ARGS)
                    } else if(key.endsWith(MSG_KEY3)){
                        MessageFormat.format(MSG3_DE, ARGS)
                    }
                } else {
                    if(key.endsWith(MSG_KEY1)) {
                        MSG1_EN
                    } else if(key.endsWith(MSG_KEY2)) {
                        MessageFormat.format(MSG2_EN, ARGS)
                    } else if(key.endsWith(MSG_KEY3)){
                        MessageFormat.format(MSG3_EN, ARGS)
                    }
                }
            }
        ]  as I18nProvider)
    }
}
