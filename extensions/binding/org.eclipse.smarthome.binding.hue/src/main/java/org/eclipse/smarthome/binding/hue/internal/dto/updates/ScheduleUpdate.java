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
package org.eclipse.smarthome.binding.hue.internal.dto.updates;

import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains all fields that can be updated on the schedule rest endpoint.
 *
 * @author David Graeff - Initial contribution
 */
public class ScheduleUpdate {
    public @Nullable Date time;
    public @Nullable String description;
    public @Nullable String name;
}
