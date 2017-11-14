/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.test;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.io.rest.sse.internal.util.SseUtil
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test

class SseResourceOSGiTest extends OSGiTest {

    @Test
    public void testValidInvalidFilters() {

        //invalid
        assertThat SseUtil.isValidTopicFilter("smarthome/.*"), is(false)
        assertThat SseUtil.isValidTopicFilter("smarthome/\\w*/"), is(false)
        assertThat SseUtil.isValidTopicFilter("sm.*/test/"), is(false)
        assertThat SseUtil.isValidTopicFilter("smarthome.*"), is(false)

        //valid
        assertThat SseUtil.isValidTopicFilter("smarthome"), is(true)
        assertThat SseUtil.isValidTopicFilter(""), is(true)
        assertThat SseUtil.isValidTopicFilter(", smarthome/*"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome,qivicon"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome , qivicon"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome,    qivicon"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome/test"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome/test/test/test/test/test"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome/test/test/test/test/test,    smarthome/test/test/test/test/test"), is(true)
        assertThat SseUtil.isValidTopicFilter("smarthome/test/test/test/test/test,    smarthome/test/test/test/test/test, smarthome,qivicon"), is(true)
        assertThat SseUtil.isValidTopicFilter("////////////"), is(true)
        assertThat SseUtil.isValidTopicFilter("*/added"), is(true)
        assertThat SseUtil.isValidTopicFilter("*added"), is(true)
        
        
    }

    @Test
    public void testFilterMatchers() {
        def regexes = SseUtil.convertToRegex("smarthome/*/test/test/test/test,    smarthome/test/*/test/test/test, smarthome,qivicon")

        assertThat "smarthome/test/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/ASDF/test/test/test".matches(regexes[0]), is(false);

        assertThat "smarthome/test/test/test/test/test".matches(regexes[1]), is(true);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[1]), is(false);
        assertThat "smarthome/asdf/ASDF/test/test/test".matches(regexes[1]), is(false);

        assertThat "smarthome/test/test/test/test/test".matches(regexes[2]), is(true);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[2]), is(true);
        assertThat "smarthome/asdf/ASDF/test/test/test".matches(regexes[2]), is(true);

        assertThat "smarthome/test/test/test/test/test".matches(regexes[3]), is(false);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[3]), is(false);
        assertThat "qivicon/asdf/ASDF/test/test/test".matches(regexes[3]), is(true);
    }

    @Test
    public void testMoreFilterMatchers() {
        def regexes = SseUtil.convertToRegex(",    *, smarthome/items/*/added, smarthome/items")

        assertThat "smarthome/test/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/ASDF/test/test/test".matches(regexes[0]), is(true);

        assertThat "smarthome/test/test/test/test/test".matches(regexes[1]), is(false);
        assertThat "smarthome/items/anyitem/added".matches(regexes[1]), is(true);
        assertThat "smarthome/items/anyitem/removed".matches(regexes[1]), is(false);

        assertThat "smarthome/items/anyitem/added".matches(regexes[2]), is(true);
        assertThat "smarthome/items/anyitem/removed".matches(regexes[2]), is(true);
        assertThat "smarthome/items/anyitem/updated".matches(regexes[2]), is(true);
        assertThat "smarthome/things/anything/updated".matches(regexes[2]), is(false);
    }

    @Test
    public void testEvenMoreFilterMatchers() {
        def regexes = SseUtil.convertToRegex("")

        assertThat "smarthome/test/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/test/test/test/test".matches(regexes[0]), is(true);
        assertThat "smarthome/asdf/ASDF/test/test/test".matches(regexes[0]), is(true);

        regexes = SseUtil.convertToRegex("*/added")
        assertThat "smarthome/items/anyitem/added".matches(regexes[0]), is(true);
        assertThat "smarthome/items/anyitem/removed".matches(regexes[0]), is(false);
        
        regexes = SseUtil.convertToRegex("*added")
        assertThat "smarthome/items/anyitem/added".matches(regexes[0]), is(true);
        assertThat "smarthome/items/anyitem/removed".matches(regexes[0]), is(false);
    }
}
