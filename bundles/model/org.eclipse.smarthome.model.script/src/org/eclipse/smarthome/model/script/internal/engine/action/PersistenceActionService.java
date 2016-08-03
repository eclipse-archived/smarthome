/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.model.persistence.extensions.PersistenceExtensions;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * This class registers an OSGi service for the Persistence action.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class PersistenceActionService implements ActionService {

    public PersistenceActionService() {
    }

    public void activate() {
    }

    public void deactivate() {
    }

    @Override
    public String getActionClassName() {
        return PersistenceExtensions.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return PersistenceExtensions.class;
    }

}
