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
package org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass;

/**
 * Homie 3.x Device statistic attributes
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeviceStatsAttributes extends AbstractMqttAttributeClass {
    public int interval = 0; // In seconds

    @Override
    public Object getFieldsOf() {
        return this;
    }
}
