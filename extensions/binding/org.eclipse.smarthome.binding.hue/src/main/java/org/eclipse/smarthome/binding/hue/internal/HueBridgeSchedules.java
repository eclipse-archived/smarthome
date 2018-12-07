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
package org.eclipse.smarthome.binding.hue.internal;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.dto.CreateScheduleRequest;
import org.eclipse.smarthome.binding.hue.internal.dto.Schedule;
import org.eclipse.smarthome.binding.hue.internal.dto.ScheduleCommand;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.ScheduleUpdate;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.UnauthorizedException;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 * Scheduler functionality. This is not used by the binding right now.
 *
 * @author David Graeff - Initial contribution, factored out of {@link HueBridge}.
 */
public class HueBridgeSchedules {
    private HueBridge bridge;

    HueBridgeSchedules(HueBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Schedules a new command to be run at the specified time.
     *
     * @param name name [0..32]
     * @param description description [0..64]
     * @param time time to run command
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws IllegalArgumentException thrown if the scheduled command is larger than 90 bytes
     */
    public CompletableFuture<ScheduleCommand> createSchedule(@Nullable String name, @Nullable String description,
            Date time, ScheduleCommand command) throws IOException, ApiException {

        String body = bridge.gson.toJson(new CreateScheduleRequest(name, description, command, time));
        AsyncHttpClient.Result result = bridge.http.post(bridge.getAbsoluteURL("schedules"), body);

        bridge.handleErrors(result);
        return CompletableFuture.completedFuture(command);
    }

    /**
     * Changes a schedule.
     *
     * @param schedule schedule
     * @param update changes
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public void setSchedule(Schedule schedule, ScheduleUpdate update) throws IOException, ApiException {
        String body = bridge.gson.toJson(update);
        AsyncHttpClient.Result result = bridge.http.put(bridge.getAbsoluteURL("schedules/" + Util.enc(schedule.id)),
                body);

        bridge.handleErrors(result);
    }

    /**
     * Delete a schedule.
     *
     * @param schedule schedule
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the schedule no longer exists
     */
    public void deleteSchedule(Schedule schedule) throws IOException, ApiException {
        AsyncHttpClient.Result result = bridge.http.delete(bridge.getAbsoluteURL("schedules/" + Util.enc(schedule.id)));
        bridge.handleErrors(result);
    }
}
