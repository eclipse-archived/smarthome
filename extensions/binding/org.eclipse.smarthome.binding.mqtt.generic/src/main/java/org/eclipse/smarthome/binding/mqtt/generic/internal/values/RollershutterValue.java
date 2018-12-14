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
package org.eclipse.smarthome.binding.mqtt.generic.internal.values;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Implements an rollershutter value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RollershutterValue extends Value {
    private final @Nullable String upString;
    private final @Nullable String downString;
    private final @Nullable String stopString;

    /**
     * Creates a new rollershutter value.
     *
     * @param upString The UP value string. This will be compared to MQTT messages.
     * @param downString The DOWN value string. This will be compared to MQTT messages.
     * @param stopString The STOP value string. This will be compared to MQTT messages.
     */
    public RollershutterValue(@Nullable String upString, @Nullable String downString, @Nullable String stopString) {
        super(CoreItemFactory.ROLLERSHUTTER,
                Stream.of(UpDownType.class, StopMoveType.class, PercentType.class, StringType.class)
                        .collect(Collectors.toList()));
        this.upString = upString;
        this.downString = downString;
        this.stopString = stopString;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        if (command instanceof UpDownType) {
            PercentType newState = ((UpDownType) command).as(PercentType.class);
            if (newState == null) { // Never happens
                return;
            }
            state = newState;
        } else if (command instanceof StopMoveType) {
            if (command.equals(StopMoveType.STOP)) {
                state = UnDefType.UNDEF;
            }
        } else if (command instanceof PercentType) {
            state = (PercentType) command;
        } else {
            final String updatedValue = command.toString();
            if (updatedValue.equals(upString)) {
                state = PercentType.ZERO;
            } else if (updatedValue.equals(downString)) {
                state = PercentType.HUNDRED;
            } else if (updatedValue.equals(stopString)) {
                state = UnDefType.UNDEF;
            } else {
                state = PercentType.valueOf(updatedValue);
            }
        }
    }

    @Override
    public String getMQTTpublishValue() {
        return (state == UnDefType.UNDEF) ? "0" : String.valueOf(((PercentType) state).intValue());
    }
}
