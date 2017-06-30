/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Utility class to easily register {@link ReadyMarker}s programmatically.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class ReadyUtil {

    /**
     * Registers a {@link ReadyMarker} with the given identification details.
     *
     * The caller needs to remember the returned {@link ServiceReference} and must make sure to unregister it at the
     * appropriate point in time.
     *
     * @param context the {@link BundleContext}
     * @param key the marker type (see {@link ReadyMarker} for constants)
     * @param identifier an identifier. What that is depends on the key.
     * @return a {@link ServiceRegistration} object
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ServiceRegistration markAsReady(BundleContext context, String key, String identifier) {
        Dictionary props = new Properties();
        props.put(key, identifier);
        return context.registerService(ReadyMarker.class.getName(), new ReadyMarker() {
        }, props);
    }

}
