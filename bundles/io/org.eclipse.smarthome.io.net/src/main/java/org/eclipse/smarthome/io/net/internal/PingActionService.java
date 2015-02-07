/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.internal;

import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.io.net.actions.Ping;

/**
 * This class registers an OSGi service for the Ping action.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PingActionService implements ActionService {

    public PingActionService() {
    }

    public void activate() {
    }

    public void deactivate() {
        // deallocate Resources here that are no longer needed and
        // should be reset when activating this binding again
    }

    @Override
    public String getActionClassName() {
        return Ping.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return Ping.class;
    }

}
