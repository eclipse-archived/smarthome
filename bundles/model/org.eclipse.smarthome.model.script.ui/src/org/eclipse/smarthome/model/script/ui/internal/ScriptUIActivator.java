/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.ui.internal;

import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass.
 */
public class ScriptUIActivator extends ScriptActivator {

    public static ServiceTracker<ActionService, ActionService> actionServiceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        actionServiceTracker = new ServiceTracker<ActionService, ActionService>(context, ActionService.class, null);
        actionServiceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        actionServiceTracker.close();
        super.stop(context);
    }

}
