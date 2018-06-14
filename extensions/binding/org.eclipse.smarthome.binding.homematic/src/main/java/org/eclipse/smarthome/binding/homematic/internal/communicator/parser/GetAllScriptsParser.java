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

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;

/**
 * Parses a Homegear message with scripts and generates the scripts.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetAllScriptsParser extends CommonRpcParser<Object[], Void> {
    private HmChannel channel;

    public GetAllScriptsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(Object[] message) throws IOException {
        message = (Object[]) message[0];
        for (int i = 0; i < message.length; i++) {
            String scriptName = ObjectUtils.toString(message[i]);
            HmDatapoint dpScript = new HmDatapoint(scriptName, scriptName, HmValueType.BOOL, Boolean.FALSE, false,
                    HmParamsetType.VALUES);
            dpScript.setInfo(scriptName);
            channel.addDatapoint(dpScript);
        }
        return null;
    }

}
