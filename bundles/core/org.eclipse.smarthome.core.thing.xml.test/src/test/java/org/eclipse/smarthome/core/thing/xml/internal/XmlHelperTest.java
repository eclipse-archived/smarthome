/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.xml.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Simon Kaufmann - Initial contribution and API
 *
 */
public class XmlHelperTest {

    @Test
    public void whenUIDContainsDot_shouldBeconvcertedToColon() {
        assertThat(XmlHelper.getSystemUID("system.test"), is("system:test"));
    }

    @Test
    public void whenNoPrefixIsGiven_shouldPrependSystemPrefix() {
        assertThat(XmlHelper.getSystemUID("test"), is("system:test"));
    }
}
