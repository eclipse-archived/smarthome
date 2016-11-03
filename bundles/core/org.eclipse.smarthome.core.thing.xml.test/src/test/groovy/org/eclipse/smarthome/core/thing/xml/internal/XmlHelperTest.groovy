/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.junit.Test

/**
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
class XmlHelperTest {

    @Test
    void 'assert that dot is converted to colon'() {
        assertThat XmlHelper.getSystemUID("system.test"), is("system:test")
    }

    @Test
    void 'assert that system namespace is prepended if no prefix was given'() {
        assertThat XmlHelper.getSystemUID("test"), is("system:test")
    }
}
