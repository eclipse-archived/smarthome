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
package org.eclipse.smarthome.core.thing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ChannelUIDTest {

    @Test
    public void testChannelUID() {
        ChannelUID channelUID = new ChannelUID("binding", "thing-type", "thing", "group", "id");
        assertThat(channelUID.toString(), is("binding:thing-type:thing:group#id"));
        assertThat(channelUID.isInGroup(), is(true));
        assertThat(channelUID.getId(), is("group#id"));
        assertThat(channelUID.getIdWithoutGroup(), is("id"));
    }

}
