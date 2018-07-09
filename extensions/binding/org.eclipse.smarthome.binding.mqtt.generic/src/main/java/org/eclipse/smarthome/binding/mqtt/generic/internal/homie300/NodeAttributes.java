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
package org.eclipse.smarthome.binding.mqtt.generic.internal.homie300;

import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MapToField;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.TopicPrefix;

/**
 * Homie 3.x Node attributes
 *
 * @author David Graeff - Initial contribution
 */
@TopicPrefix
public class NodeAttributes {
    public String name;
    public String type;
    public @MapToField(splitCharacter = ",") String[] properties;
    public String array;
}
