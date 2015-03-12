/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.rfc147.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class Rfc147Activator implements BundleActivator {

    private static volatile Rfc147Manager manager;

    public static Rfc147Manager getManager() {
        return manager;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        manager = new Rfc147Manager(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        manager = null;
    }

}
