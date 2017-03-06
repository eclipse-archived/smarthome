/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
