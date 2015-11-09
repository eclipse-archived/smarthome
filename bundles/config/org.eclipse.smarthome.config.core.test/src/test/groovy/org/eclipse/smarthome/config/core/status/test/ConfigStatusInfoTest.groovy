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
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage.Type
import org.junit.Test

/**
 * Testing the {@link ConfigStatusInfo}.
 *
 * @author Thomas Höfer - Initial contribution
 */
class ConfigStatusInfoTest {

    private static final String PARAM1 = "param1"
    private static final String PARAM2 = "param2"
    private static final String PARAM3 = "param3"
    private static final String PARAM4 = "param4"
    private static final String PARAM5 = "param5"
    private static final String PARAM6 = "param6"

    private static final String INFO1 = "info1"
    private static final String INFO2 = "info2"
    private static final String WARNING1 = "warning1"
    private static final String WARNING2 = "warning2"
    private static final String ERROR1 = "error1"
    private static final String ERROR2 = "error2"

    private static final ConfigStatusMessage MSG1 = createMessage(PARAM1, Type.INFORMATION, INFO1)
    private static final ConfigStatusMessage MSG2 = createMessage(PARAM2, Type.INFORMATION, INFO2, 1)
    private static final ConfigStatusMessage MSG3 = createMessage(PARAM3, Type.WARNING, WARNING1)
    private static final ConfigStatusMessage MSG4 = createMessage(PARAM4, Type.WARNING, WARNING2, 1)
    private static final ConfigStatusMessage MSG5 = createMessage(PARAM5, Type.ERROR, ERROR1)
    private static final ConfigStatusMessage MSG6 = createMessage(PARAM6, Type.PENDING, ERROR2, 1)

    private static final List ALL = [
        MSG1,
        MSG2,
        MSG3,
        MSG4,
        MSG5,
        MSG6
    ]

    @Test
    void 'assert correct config error handling for empty result object'() {
        ConfigStatusInfo info = new ConfigStatusInfo()
        assertThat info.getConfigStatusMessages().size(), is(0)
    }

    @Test
    void 'assert correct config status info handling using constructor'() {
        assertConfigStatusInfo(new ConfigStatusInfo(ALL))
    }

    @Test
    void 'assert correct config error handling using addConfigErrors'() {
        ConfigStatusInfo info = new ConfigStatusInfo()
        info.add(ALL)
        assertConfigStatusInfo(info)
    }

    @Test
    void 'assert correct config error handling using addConfigError'() {
        ConfigStatusInfo info = new ConfigStatusInfo()
        for(ConfigStatusMessage configStatusMessage : ALL) {
            info.add(configStatusMessage)
        }
        assertConfigStatusInfo(info)
    }

    private void assertConfigStatusInfo(ConfigStatusInfo info) {
        assertThat info.getConfigStatusMessages().size(), is(ALL.size())
        assertThat info.getConfigStatusMessages(), hasItems(MSG1, MSG2, MSG3, MSG4, MSG5, MSG6)

        assertThat info.getConfigStatusMessages(Type.INFORMATION).size(), is(2);
        assertThat info.getConfigStatusMessages(Type.INFORMATION), hasItems(MSG1, MSG2);

        assertThat info.getConfigStatusMessages(Type.WARNING).size(), is(2);
        assertThat info.getConfigStatusMessages(Type.WARNING), hasItems(MSG3, MSG4);

        assertThat info.getConfigStatusMessages(Type.ERROR).size(), is(1);
        assertThat info.getConfigStatusMessages(Type.ERROR), hasItems(MSG5);

        assertThat info.getConfigStatusMessages(Type.PENDING).size(), is(1);
        assertThat info.getConfigStatusMessages(Type.PENDING), hasItems(MSG6);

        assertThat info.getConfigStatusMessages(Type.INFORMATION, Type.WARNING).size(), is(4);
        assertThat info.getConfigStatusMessages(Type.INFORMATION, Type.WARNING), hasItems(MSG1, MSG2, MSG3, MSG4);

        assertThat info.getConfigStatusMessages(PARAM1).size(), is(1)
        assertThat info.getConfigStatusMessages(PARAM1), hasItem(MSG1)

        assertThat info.getConfigStatusMessages(PARAM2).size(), is(1)
        assertThat info.getConfigStatusMessages(PARAM2), hasItem(MSG2)

        assertThat info.getConfigStatusMessages(PARAM3, PARAM4).size(), is(2)
        assertThat info.getConfigStatusMessages(PARAM3, PARAM4), hasItems(MSG3, MSG4)

        assertThat info.getConfigStatusMessages("unknown").size(), is(0)
    }

    def static ConfigStatusMessage createMessage(String paramName, Type type, String message, Integer statusCode=null) {
        return new ConfigStatusMessage.Builder(paramName, type, message).withStatusCode(statusCode).build()
    }
}
