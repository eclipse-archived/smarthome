/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** 
 * Provides a static thread pool 
 *
 * @author Andre Fuechsel - initial contribution
 */
public class DiscoveryThreadPool {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5); 
    
    /** 
     * Get the static thread pool. 
     * 
     * @return scheduler object to schedule a thread
     */
    public static ScheduledExecutorService getScheduler() { 
        return scheduler; 
    }
}
