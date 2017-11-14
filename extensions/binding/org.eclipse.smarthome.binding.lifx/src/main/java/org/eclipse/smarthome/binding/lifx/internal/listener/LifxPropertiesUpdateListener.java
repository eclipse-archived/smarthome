/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.listener;

import java.util.Map;

import org.eclipse.smarthome.binding.lifx.internal.LifxLightPropertiesUpdater;

/**
 * The {@link LifxPropertiesUpdateListener} is notified when the {@link LifxLightPropertiesUpdater} has
 * updated light properties.
 *
 * @author Wouter Born - Update light properties when online
 */
public interface LifxPropertiesUpdateListener {

    /**
     * Called when the {@link LifxLightPropertiesUpdater} has updated light properties.
     *
     * @param packet the updated properties
     */
    public void handlePropertiesUpdate(Map<String, String> properties);
}
