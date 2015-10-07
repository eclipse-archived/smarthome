/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.transform.internal;

import org.eclipse.smarthome.core.transform.actions.Transformation;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * This class registers an OSGi service for the Transformation action.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class TransformationActionService implements ActionService {

    public TransformationActionService() {
    }

    public void activate() {
    }

    public void deactivate() {
        // deallocate Resources here that are no longer needed and
        // should be reset when activating this binding again
    }

    @Override
    public String getActionClassName() {
        return Transformation.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return Transformation.class;
    }

}
