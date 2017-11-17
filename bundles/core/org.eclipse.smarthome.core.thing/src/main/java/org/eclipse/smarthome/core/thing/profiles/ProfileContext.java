/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * The profile's context
 *
 * It gives access to related information like the profile's configuration or a scheduler.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ProfileContext {

    /**
     * Get the profile's configuration object
     *
     * @return the configuration
     */
    Configuration getConfiguration();

    /**
     * Get a scheduler to be used within profiles (if needed at all)
     *
     * @return the scheduler
     */
    ScheduledExecutorService getExecutorService();

}
