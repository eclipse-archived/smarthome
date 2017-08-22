/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.framework.BundleContext;

// Set the path were the certificate is created to the working directory
class NetworkServerTlsProviderEx extends NetworkServerTlsProvider {
    Date currentDate = new Date();

    public NetworkServerTlsProviderEx(ComputeConfigurationDifference utils) {
        this.utils = utils;
    }

    @SuppressWarnings("null")
    @Override
    public void activated(BundleContext bundleContext, Map<String, Object> config) throws NoSuchAlgorithmException {
        configuration = new Configuration(config).as(ServiceConfiguration.class);
    }

    @Override
    protected Date dateNow() {
        return currentDate;
    }
}