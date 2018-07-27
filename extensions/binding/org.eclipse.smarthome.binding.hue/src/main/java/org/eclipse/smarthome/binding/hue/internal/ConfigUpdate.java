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

import java.util.ArrayList;

/**
 * Collection of updates
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author Samuel Leisering - refactored config updates,
 *
 */
public class ConfigUpdate {

    protected ArrayList<Command> commands = new ArrayList<>();

    public ConfigUpdate() {
        super();
    }

    protected String toJson() {
        StringBuilder json = new StringBuilder("{");

        for (int i = 0; i < commands.size(); i++) {
            json.append(commands.get(i).toJson());
            if (i < commands.size() - 1) {
                json.append(",");
            }
        }

        json.append("}");

        return json.toString();
    }

    /**
     * Returns the message delay recommended by Philips
     * Regarding to this article: https://developers.meethue.com/documentation/hue-system-performance
     */
    public Integer getMessageDelay() {
        return commands.size() * 40;
    }

}