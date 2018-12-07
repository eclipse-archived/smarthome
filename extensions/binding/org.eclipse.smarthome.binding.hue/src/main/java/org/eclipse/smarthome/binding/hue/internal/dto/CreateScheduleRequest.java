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
package org.eclipse.smarthome.binding.hue.internal.dto;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class CreateScheduleRequest {
    public @Nullable String name;
    public @Nullable String description;
    public @Nullable ScheduleCommand command;
    public Date time;

    public CreateScheduleRequest(@Nullable String name, @Nullable String description, @Nullable ScheduleCommand command,
            Date time) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Schedule name can be at most 32 characters long");
        }

        if (description != null && Util.stringSize(description) > 64) {
            throw new IllegalArgumentException("Schedule description can be at most 64 characters long");
        }

        if (command == null || Util.stringSize(command.body) > 90) {
            throw new IllegalArgumentException("No schedule command specified or larger than 90 characters");
        }

        this.name = name;
        this.description = description;
        this.command = command;
        this.time = time;
    }
}
