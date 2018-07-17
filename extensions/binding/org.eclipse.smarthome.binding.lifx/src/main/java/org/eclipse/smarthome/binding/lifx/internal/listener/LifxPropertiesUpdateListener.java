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
package org.eclipse.smarthome.binding.lifx.internal.listener;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.lifx.internal.LifxLightPropertiesUpdater;

/**
 * The {@link LifxPropertiesUpdateListener} is notified when the {@link LifxLightPropertiesUpdater} has
 * updated light properties.
 *
 * @author Wouter Born - Update light properties when online
 */
@NonNullByDefault
public interface LifxPropertiesUpdateListener {

    /**
     * Called when the {@link LifxLightPropertiesUpdater} has updated light properties.
     *
     * @param packet the updated properties
     */
    public void handlePropertiesUpdate(Map<String, String> properties);
}
