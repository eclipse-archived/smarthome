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
import org.eclipse.smarthome.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.eclipse.smarthome.binding.mqtt.generic.internal.mapping.MqttAttributeClass;

/**
 * Callbacks to inform about the Homie Device state, statistics changes, node layout changes.
 * Meant to be used by the Homie thing handler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface DeviceCallback {
    /**
     * Called whenever the device state changed
     *
     * @param state The new state
     */
    void readyStateChanged(ReadyState state);

    /**
     * Called whenever the statistics properties changed
     *
     * @param stats The new statistics
     */
    void statisticAttributesChanged(DeviceStatsAttributes stats);

    /**
     * Called once for nodes and for each node property as well after the device has been started via
     * {@link Device#startDiscovery(DeviceCallback)}.
     *
     * Called subsequently,if the device "removes" or adds nodes or node properties.
     *
     * Is is safe to call{@link Device#collectAllProperties()} for a list of all properties within this callback
     * if {@link Device#isInitializing()} is false.
     *
     */
    void propertiesChanged();

    /**
     * Called, whenever a Homie node or property was existing before, but is not anymore.
     *
     * @param attributeClass The affected attribute class.
     */
    void subNodeRemoved(MqttAttributeClass attributeClass);
}
