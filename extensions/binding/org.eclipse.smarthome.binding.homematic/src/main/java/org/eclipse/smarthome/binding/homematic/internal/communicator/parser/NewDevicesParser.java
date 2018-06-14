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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Parses a new device event received from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class NewDevicesParser extends CommonRpcParser<Object[], List<String>> {
    @Override
    @SuppressWarnings("unchecked")
    public List<String> parse(Object[] message) throws IOException {
        List<String> adresses = new ArrayList<String>();
        if (message != null && message.length > 1) {
            message = (Object[]) message[1];
            for (int i = 0; i < message.length; i++) {
                Map<String, ?> data = (Map<String, ?>) message[i];

                String address = toString(data.get("ADDRESS"));
                boolean isDevice = !StringUtils.contains(address, ":")
                        && !StringUtils.startsWithIgnoreCase(address, "BidCos");
                if (isDevice) {
                    adresses.add(getSanitizedAddress(address));
                }
            }
        }
        return adresses;
    }

}
