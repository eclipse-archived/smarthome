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

import static org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants.*;

import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;

/**
 * A virtual datapoint that unifies the device and peer rssi datapoints.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class RssiVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_RSSI;
    }

    @Override
    public void initialize(HmDevice device) {
        if (isWirelessDevice(device)) {
            HmDatapoint dp = addDatapoint(device, 0, getName(), HmValueType.INTEGER, getRssiValue(device.getChannel(0)),
                    true);
            dp.setUnit("dBm");
            dp.setMinValue(Integer.MIN_VALUE);
            dp.setMaxValue(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return isWirelessDevice(dp.getChannel().getDevice())
                && (DATAPOINT_NAME_RSSI_DEVICE.equals(dp.getName()) || DATAPOINT_NAME_RSSI_PEER.equals(dp.getName()));
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmChannel channel = dp.getChannel();
        Object value = getRssiValue(channel);
        HmDatapoint vdpRssi = getVirtualDatapoint(channel);
        vdpRssi.setValue(value);
    }

    /**
     * Returns either the device or the peer rssi value.
     */
    protected Integer getRssiValue(HmChannel channel) {
        HmDatapoint dpRssiDevice = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE);
        HmDatapoint dpRssiPeer = channel.getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_PEER);

        Integer deviceValue = getDatapointValue(dpRssiDevice);
        Integer peerValue = getDatapointValue(dpRssiPeer);

        if ((deviceValue == null || deviceValue == 0) && peerValue != null) {
            return peerValue;
        }
        return deviceValue;
    }

    private Integer getDatapointValue(HmDatapoint dp) {
        if (dp == null || dp.getValue() == null || (Integer) dp.getValue() == 0) {
            return null;
        }

        return (Integer) dp.getValue();
    }

    protected boolean isWirelessDevice(HmDevice device) {
        return device.getChannel(0).getDatapoint(HmParamsetType.VALUES, DATAPOINT_NAME_RSSI_DEVICE) != null;
    }
}
