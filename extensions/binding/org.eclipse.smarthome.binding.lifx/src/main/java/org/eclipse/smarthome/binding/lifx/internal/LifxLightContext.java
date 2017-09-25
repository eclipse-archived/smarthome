/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler;
import org.eclipse.smarthome.binding.lifx.handler.LifxLightHandler.CurrentLightState;
import org.eclipse.smarthome.binding.lifx.internal.protocol.Products;

/**
 * The {@link LifxLightContext} shares the context of a light with {@link LifxLightHandler} helper objects.
 *
 * @author Wouter Born - Add optional host configuration parameter
 */
public class LifxLightContext {

    private final String logId;
    private final LifxLightConfig configuration;
    private final CurrentLightState currentLightState;
    private final LifxLightState pendingLightState;
    private final Products product;
    private final ScheduledExecutorService scheduler;

    public LifxLightContext(String logId, Products product, LifxLightConfig configuration,
            CurrentLightState currentLightState, LifxLightState pendingLightState, ScheduledExecutorService scheduler) {
        this.logId = logId;
        this.configuration = configuration;
        this.product = product;
        this.currentLightState = currentLightState;
        this.pendingLightState = pendingLightState;
        this.scheduler = scheduler;
    }

    public String getLogId() {
        return logId;
    }

    public LifxLightConfig getConfiguration() {
        return configuration;
    }

    public Products getProduct() {
        return product;
    }

    public CurrentLightState getCurrentLightState() {
        return currentLightState;
    }

    public LifxLightState getPendingLightState() {
        return pendingLightState;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

}
