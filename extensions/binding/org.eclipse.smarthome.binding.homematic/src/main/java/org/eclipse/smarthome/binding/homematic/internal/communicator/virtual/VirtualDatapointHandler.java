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
package org.eclipse.smarthome.binding.homematic.internal.communicator.virtual;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicClientException;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointConfig;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;

/**
 * Describes the methods used for a virtual datapoint. A virtual datapoint is generated datapoint with special
 * functions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface VirtualDatapointHandler {

    /**
     * Returns the virtual datapoint name.
     */
    public String getName();

    /**
     * Adds the virtual datapoint to the device.
     */
    public void initialize(HmDevice device);

    /**
     * Returns true, if the virtual datapoint can handle a command for the given datapoint.
     */
    public boolean canHandleCommand(HmDatapoint dp, Object value);

    /**
     * Handles the special functionality for the given virtual datapoint.
     */
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException;

    /**
     * Returns true, if the virtual datapoint can handle the event for the given datapoint.
     */
    public boolean canHandleEvent(HmDatapoint dp);

    /**
     * Handles a event to extract data required for the virtual datapoint.
     */
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp);

    /**
     * Returns the virtual datapoint in the given channel.
     */
    public HmDatapoint getVirtualDatapoint(HmChannel channel);
}
