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
package org.eclipse.smarthome.binding.hue.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.FullLight;
import org.eclipse.smarthome.binding.hue.internal.StateUpdate;

/**
 * Access to the Hue system for light handlers.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
public interface HueClient {

    /**
     * Register a light status listener.
     *
     * @param lightStatusListener the light status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean registerLightStatusListener(LightStatusListener lightStatusListener);

    /**
     * Unregister a light status listener.
     *
     * @param lightStatusListener the light status listener
     * @return {@code true} if the collection of listeners has changed as a result of this call
     */
    boolean unregisterLightStatusListener(LightStatusListener lightStatusListener);

    /**
     * Get the light by its ID.
     *
     * @param lightId the light ID
     * @return the full light representation of {@code null} if it could not be found
     */
    @Nullable
    FullLight getLightById(String lightId);

    /**
     * Updated the given light.
     *
     * @param light the light to be updated
     * @param stateUpdate the state update
     */
    void updateLightState(FullLight light, StateUpdate stateUpdate);

}
