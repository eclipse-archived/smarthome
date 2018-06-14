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
package org.eclipse.smarthome.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.homematic.internal.common.HomematicConfig;
import org.eclipse.smarthome.binding.homematic.internal.misc.MiscUtils;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;

/**
 * Parses a list devices message and generates device and channel metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ListDevicesParser extends CommonRpcParser<Object[], Collection<HmDevice>> {
    private HmInterface hmInterface;
    private HomematicConfig config;

    public ListDevicesParser(HmInterface hmInterface, HomematicConfig config) {
        this.hmInterface = hmInterface;
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<HmDevice> parse(Object[] message) throws IOException {
        message = (Object[]) message[0];
        Map<String, HmDevice> devices = new HashMap<String, HmDevice>();

        for (int i = 0; i < message.length; i++) {
            Map<String, ?> data = (Map<String, ?>) message[i];
            boolean isDevice = !StringUtils.contains(toString(data.get("ADDRESS")), ":");

            if (isDevice) {
                String address = getSanitizedAddress(data.get("ADDRESS"));
                String type = MiscUtils.validateCharacters(toString(data.get("TYPE")), "Device type", "-");
                String id = toString(data.get("ID"));
                String firmware = toString(data.get("FIRMWARE"));

                devices.put(address,
                        new HmDevice(address, hmInterface, type, config.getGatewayInfo().getId(), id, firmware));
            } else {
                // channel
                String deviceAddress = getSanitizedAddress(data.get("PARENT"));
                HmDevice device = devices.get(deviceAddress);

                String type = toString(data.get("TYPE"));
                Integer number = toInteger(data.get("INDEX"));

                device.addChannel(new HmChannel(type, number));
            }
        }
        return devices.values();
    }
}
