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
package org.eclipse.smarthome.persistence.mapdb.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A GSON TypeAdapter for Eclipse SmartHome State values.
 * 
 * @author Martin Kühl - Initial contribution
 */
public class StateTypeAdapter extends TypeAdapter<State> {
    private static final String TYPE_SEPARATOR = "@@@";

    private final Logger logger = LoggerFactory.getLogger(StateTypeAdapter.class);

    @Override
    public State read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String value = reader.nextString();
        String[] parts = value.split(TYPE_SEPARATOR);
        String valueTypeName = parts[0];
        String valueAsString = parts[1];

        try {
            @SuppressWarnings("unchecked")
            Class<? extends State> valueType = (Class<? extends State>) Class.forName(valueTypeName);
            List<Class<? extends State>> types = Collections.singletonList(valueType);
            return TypeParser.parseState(types, valueAsString);
        } catch (Exception e) {
            logger.warn("Couldn't deserialize state '{}': {}", value, e.getMessage());
        }
        return null;
    }

    @Override
    public void write(JsonWriter writer, State state) throws IOException {
        if (state == null) {
            writer.nullValue();
            return;
        }
        String value = state.getClass().getName() + TYPE_SEPARATOR + state.toFullString();
        writer.value(value);
    }
}
