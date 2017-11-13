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
package org.eclipse.smarthome.automation.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;

/**
 * This interface is implemented by external modules which are called by the
 * RuleEngine to execute {@link Action}s of the {@link Rule}s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface ActionHandler extends ModuleHandler {

    /**
     * The Method is called by the RuleEngine to execute a {@link Rule} {@link Action}.
     *
     *
     * @param context is an unmodifiable map containing action input values and snapshot of output values of triggers
     *            and executed actions. The output ids are defined
     *            in form: ModuleId.outputId
     * @return values map of values which must be set to outputs of the {@link Action} (may be null).
     */
    public Map<String, Object> execute(Map<String, Object> context);

}
