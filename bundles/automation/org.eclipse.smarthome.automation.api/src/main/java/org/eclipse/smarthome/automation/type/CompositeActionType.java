/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.type;

import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * CompositeActionType is as {@link ActionType} which logically combines {@link Action} instances. The composite action
 * hides internal logic and inner
 * connections between participating actions and it can be used as a regular {@link Action} module.
 */
public class CompositeActionType extends ActionType {

    private List<Action> modules;

    /**
     * Creates a CompositeActionType with ordered set of {@link Action}s
     *
     * @param UID unique id of this module type
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param modules LinkedHashSet of {@link Module}(s)
     * @param inputs is a {@link Set} of {@link Input} definitions.
     * @param outputs is a {@link Set} of {@link Output} definitions.
     *
     */
    public CompositeActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs,
            Set<Output> outputs, List<Action> modules) {
        super(UID, configDescriptions, inputs, outputs);
        this.modules = modules;

    }

    /**
     * This method is used for getting Actions of the CompositeActionDescriptor.
     *
     * @return ordered set of modules of this CompositeActionDescriptor
     */
    public List<Action> getModules() {
        return modules;
    }

}
