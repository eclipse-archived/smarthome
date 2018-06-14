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

import static org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_PRESS;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.homematic.internal.misc.MiscUtils;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual String datapoint which adds a PRESS datapoint for each key with a PRESS_SHORT and PRESS_LONG datapoint to
 * simulate a key trigger.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PressVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(PressVirtualDatapointHandler.class);

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_PRESS;
    }

    @Override
    public void initialize(HmDevice device) {
        for (HmChannel channel : device.getChannels()) {
            if (channel.hasPressDatapoint()) {
                HmDatapoint dp = addDatapoint(device, channel.getNumber(), getName(), HmValueType.STRING, null, false);
                dp.setTrigger(true);
                dp.setOptions(new String[] { "SHORT", "LONG", "LONG_RELEASE", "CONT" });
            }
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return dp.isPressDatapoint();
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmDatapoint vdp = getVirtualDatapoint(dp.getChannel());
        if (MiscUtils.isTrueValue(dp.getValue())) {
            String value = StringUtils.substringAfter(dp.getName(), "_");
            if (ArrayUtils.contains(vdp.getOptions(), value)) {
                vdp.setValue(value);
            } else {
                logger.warn("Unknown value '{}' for PRESS virtual datapoint, only {} allowed", value,
                        StringUtils.join(vdp.getOptions(), ","));
            }
        } else {
            vdp.setValue(null);
        }
    }
}
