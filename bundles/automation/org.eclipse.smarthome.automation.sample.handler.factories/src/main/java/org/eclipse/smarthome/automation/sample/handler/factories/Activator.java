/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.sample.handler.factories;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Bundle Activator
 *
 * @author Vasil Ilchev - Initial Contribution
 */
public class Activator implements BundleActivator {

    static BundleContext bc;
    private SampleHandlerFactory sampleHandlerFactory;
    private SampleHandlerFactoryCommands commands;

    public void start(BundleContext context) throws Exception {
        bc = context;
        sampleHandlerFactory = new SampleHandlerFactory(bc);
        commands = new SampleHandlerFactoryCommands(sampleHandlerFactory, bc);
    }

    public void stop(BundleContext context) throws Exception {
        commands.stop();
        sampleHandlerFactory.dispose();
        commands = null;
        sampleHandlerFactory = null;
        bc = null;
    }

}
