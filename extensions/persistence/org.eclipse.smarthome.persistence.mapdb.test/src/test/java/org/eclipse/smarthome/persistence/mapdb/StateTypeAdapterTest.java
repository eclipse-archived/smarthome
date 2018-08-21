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
package org.eclipse.smarthome.persistence.mapdb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.persistence.mapdb.internal.StateTypeAdapter;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author Martin Kühl - Initial contribution
 */
public class StateTypeAdapterTest {
    Gson mapper = new GsonBuilder()
        .registerTypeHierarchyAdapter(State.class, new StateTypeAdapter())
        .create();

    @Test
    public void readWriteRoundtripShouldRecreateTheWrittenState() {
        assertThat(roundtrip(OnOffType.ON), is(equalTo(OnOffType.ON)));
        assertThat(roundtrip(PercentType.HUNDRED), is(equalTo(PercentType.HUNDRED)));
        assertThat(roundtrip(HSBType.GREEN), is(equalTo(HSBType.GREEN)));
        assertThat(roundtrip(StringType.valueOf("test")), is(equalTo(StringType.valueOf("test"))));
    }

    private State roundtrip(State state) {
        return mapper.fromJson(mapper.toJson(state), State.class);
    }
}
