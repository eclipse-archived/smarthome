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

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.eclipse.smarthome.binding.homematic.internal.model.TclScriptDataEntry;
import org.eclipse.smarthome.binding.homematic.internal.model.TclScriptDataList;

/**
 * Parses a TclRega script result containing variables and scripts.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuVariablesAndScriptsParser extends CommonRpcParser<TclScriptDataList, Void> {
    private HmChannel channel;

    public CcuVariablesAndScriptsParser(HmChannel channel) {
        this.channel = channel;
    }

    @Override
    public Void parse(TclScriptDataList resultList) throws IOException {
        if (resultList.getEntries() != null) {
            for (TclScriptDataEntry entry : resultList.getEntries()) {
                HmDatapoint dp = channel.getDatapoint(HmParamsetType.VALUES, entry.name);
                if (dp != null) {
                    dp.setValue(convertToType(entry.value));
                } else {
                    dp = new HmDatapoint();
                    dp.setName(entry.name);
                    dp.setInfo(entry.name);
                    dp.setDescription(entry.description);
                    dp.setType(HmValueType.parse(entry.valueType));
                    dp.setValue(convertToType(entry.value));
                    if (dp.isIntegerType()) {
                        dp.setMinValue(toInteger(entry.minValue));
                        dp.setMaxValue(toInteger(entry.maxValue));
                    } else {
                        dp.setMinValue(toDouble(entry.minValue));
                        dp.setMaxValue(toDouble(entry.maxValue));
                    }
                    dp.setReadOnly(entry.readOnly);
                    dp.setUnit(entry.unit);

                    String[] result = StringUtils.splitByWholeSeparatorPreserveAllTokens(entry.options, ";");
                    dp.setOptions(result == null || result.length == 0 ? null : result);

                    if (dp.getOptions() != null) {
                        dp.setMinValue(0);
                        dp.setMaxValue(dp.getOptions().length - 1);
                    }

                    dp.setParamsetType(HmParamsetType.VALUES);
                    channel.addDatapoint(dp);
                }
            }
        }
        return null;
    }
}
