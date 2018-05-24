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
package org.eclipse.smarthome.transform.jsonpath.internal;

import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.service.component.annotations.Component;

/**
 * This class registers a service for the {@link JSonPath} action.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@Component
public class JSonPathActionAservice implements ActionService {

    @Override
    public String getActionClassName() {
        return JSonPath.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return JSonPath.class;
    }

}
