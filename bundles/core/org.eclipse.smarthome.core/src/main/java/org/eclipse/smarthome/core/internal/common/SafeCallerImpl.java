/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.internal.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.SafeCaller;
import org.eclipse.smarthome.core.common.SafeCallerBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * Implementation of the {@link SafeCaller} API.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
@Component(configurationPid = "org.eclipse.smarthome.safecaller", immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SafeCallerImpl implements SafeCaller {

    private static final String SAFE_CALL_POOL_NAME = "safeCall";

    @NonNullByDefault({})
    private ScheduledExecutorService watcher;

    @NonNullByDefault({})
    private SafeCallManagerImpl manager;

    @Activate
    public void activate(@Nullable Map<String, Object> properties) {
        watcher = Executors.newSingleThreadScheduledExecutor();
        manager = new SafeCallManagerImpl(watcher, getPoolName(), false);
        modified(properties);
    }

    @Modified
    public void modified(@Nullable Map<String, Object> properties) {
        if (properties != null) {
            String enabled = (String) properties.get("singleThread");
            manager.setEnforceSingleThreadPerIdentifier("true".equalsIgnoreCase(enabled));
        }
    }

    @Deactivate
    public void deactivate() {
        if (watcher != null) {
            watcher.shutdownNow();
            watcher = null;
        }
        manager = null;
    }

    @Override
    public <T> SafeCallerBuilder<T> create(T target, Class<T> interfaceType) {
        return new SafeCallerBuilderImpl<T>(target, new Class<?>[] { interfaceType }, manager);
    }

    @Override
    public <T> SafeCallerBuilder<T> create(T target) {
        return new SafeCallerBuilderImpl<T>(target, getAllInterfaces(target), manager);
    }

    protected String getPoolName() {
        return SAFE_CALL_POOL_NAME;
    }

    private static <T> Class<?>[] getAllInterfaces(T target) {
        Set<Class<?>> ret = new HashSet<>();
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            ret.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return ret.toArray(new Class<?>[ret.size()]);
    }

}
