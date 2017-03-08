/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.Date;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@SuppressWarnings("unused")
class CreateScheduleRequest {
    private String name;
    private String description;
    private ScheduleCommand command;
    private Date time;

    public CreateScheduleRequest(String name, String description, ScheduleCommand command, Date time) {
        if (name != null && Util.stringSize(name) > 32) {
            throw new IllegalArgumentException("Schedule name can be at most 32 characters long");
        }

        if (description != null && Util.stringSize(description) > 64) {
            throw new IllegalArgumentException("Schedule description can be at most 64 characters long");
        }

        if (command == null) {
            throw new IllegalArgumentException("No schedule command specified");
        }

        this.name = name;
        this.description = description;
        this.command = command;
        this.time = time;
    }
}
