/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;

/**
 * Implements an open/close boolean value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class OpenCloseValue extends Value {
    private final String openString;
    private final String closeString;

    /**
     * Creates a contact Open/Close type.
     */
    public OpenCloseValue() {
        super(CoreItemFactory.CONTACT, Stream.of(OpenClosedType.class, StringType.class).collect(Collectors.toList()));
        this.openString = OpenClosedType.OPEN.name();
        this.closeString = OpenClosedType.CLOSED.name();
    }

    /**
     * Creates a new contact Open/Close value.
     *
     * @param openValue The ON value string. This will be compared to MQTT messages.
     * @param closeValue The OFF value string. This will be compared to MQTT messages.
     */
    public OpenCloseValue(@Nullable String openValue, @Nullable String closeValue) {
        super(CoreItemFactory.CONTACT, Stream.of(OpenClosedType.class, StringType.class).collect(Collectors.toList()));
        this.openString = openValue == null ? OpenClosedType.OPEN.name() : openValue;
        this.closeString = closeValue == null ? OpenClosedType.CLOSED.name() : closeValue;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        if (command instanceof OpenClosedType) {
            state = (OpenClosedType) command;
        } else {
            final String updatedValue = command.toString();
            if (openString.equals(updatedValue)) {
                state = OpenClosedType.OPEN;
            } else if (closeString.equals(updatedValue)) {
                state = OpenClosedType.CLOSED;
            } else {
                state = OpenClosedType.valueOf(updatedValue);
            }
        }
    }

    @Override
    public String getMQTTpublishValue() {
        return (state == OpenClosedType.OPEN) ? openString : closeString;
    }
}
