/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This class is an activator of this bundle. Opens the all used service trackers and registers the services -
 * AutomationCommands, {@link ModuleTypeProvider} and {@link TemplateProvider}.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Activator implements BundleActivator {

    private AutomationCommandsPluggable autoCommands;

    /**
     * This method initialize pluggable commands for importing, exporting, listing and removing automation objects.
     */
    @Override
    public void start(BundleContext bc) throws Exception {
        autoCommands = new AutomationCommandsPluggable(bc);
    }

    /**
     * This method close all used service trackers, unregisters the services - AutomationCommands,
     * {@link ModuleTypeProvider} and {@link TemplateProvider}.
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
        autoCommands.stop();
    }

}
