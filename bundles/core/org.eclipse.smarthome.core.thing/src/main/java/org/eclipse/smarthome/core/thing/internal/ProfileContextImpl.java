/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;

/**
 * {@link ProfileContext} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ProfileContextImpl implements ProfileContext {

    private static final String THREAD_POOL_NAME = "profiles";
    private final Configuration configuration;

    public ProfileContextImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ExecutorService getExecutorService() {
        return ThreadPoolManager.getPool(THREAD_POOL_NAME);
    }

}
