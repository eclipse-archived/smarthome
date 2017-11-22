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
package org.eclipse.smarthome.model.script.internal.engine.action;

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
