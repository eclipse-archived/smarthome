/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.core.ephemeris.EphemerisManager;
import org.eclipse.smarthome.model.script.actions.Ephemeris;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * This class registers an OSGi service for the ephemeris action.
 *
 * @author Gaël L'hopital - Initial contribution and API
 */
public class EphemerisActionService implements ActionService {

    public static EphemerisManager ephemerisManager;

    @Override
    public String getActionClassName() {
        return Ephemeris.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return Ephemeris.class;
    }

    protected void setEphemerisManager(EphemerisManager ephemerisManager) {
        EphemerisActionService.ephemerisManager = ephemerisManager;
    }

    protected void unsetEphemerisManager(EphemerisManager ephemerisManager) {
        EphemerisActionService.ephemerisManager = null;
    }

}
