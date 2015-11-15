/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.basedriver.impl;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.impl.EnOceanBaseDriver;

/**
 * Activator.
 */
public class EnOceanBundleActivator implements BundleActivator {
    private EnOceanBaseDriver basedriver;

    public void start(BundleContext bc) {
        basedriver = new EnOceanBaseDriver(bc);
        basedriver.start();
    }

    public void stop(BundleContext bc) {
        basedriver.stop();
    }
}
