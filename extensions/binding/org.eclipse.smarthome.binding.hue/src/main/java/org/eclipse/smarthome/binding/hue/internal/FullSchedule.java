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
 * Detailed schedule information.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullSchedule extends Schedule {
    private String description;
    private ScheduleCommand command; // Not really appropriate for exposure
    private Date time;

    /**
     * Returns the description of the schedule.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the scheduled command.
     *
     * @return command
     */
    public ScheduleCommand getCommand() {
        return command;
    }

    /**
     * Returns the time for which the command is scheduled to be ran.
     *
     * @return scheduled time
     */
    public Date getTime() {
        return time;
    }
}
