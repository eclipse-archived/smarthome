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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.mqtt.generic.internal.homie300.DeviceAttributes.ReadyState;

/**
 * Some callbacks meant to be used by the homie thing handler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface DeviceCallback {
    /**
     * Called whenever the device state changes
     *
     * @param state
     */
    void readyStateChanged(ReadyState state);

    void statisticAttributesChanged(DeviceStatsAttributes stats);

    /**
     * Called once for nodes and for each nodes properties as well after the device has been started via
     * {@link Device#start(DeviceCallback)}. Called subsequently,
     * if the device "removes" or adds nodes or node properties. Is is safe to call
     * {@link Device#collectAllProperties()} for a list of all properties within this callback
     * if {@link Device#isInitializing()} is false.
     *
     * @param properties
     */
    void propertiesChanged();

    /**
     * Called, whenever a homie node or property was existing before, but is not anymore.
     *
     * @param p
     */
    void subNodeRemoved(Subscribable p);
}