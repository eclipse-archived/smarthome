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
