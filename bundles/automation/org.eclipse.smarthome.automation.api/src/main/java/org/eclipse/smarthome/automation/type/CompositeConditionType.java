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

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * CompositeConditionType is as {@link ConditionType} which logically combines {@link Condition} modules. The composite
 * condition hides internal logic
 * between participating conditions and it can be used as a regular {@link Condition} module.
 *
 */
public class CompositeConditionType extends ConditionType {

    private List<Condition> modules;

    /**
     * Creates a CompositeTriggerDescriptor with ordered set of {@link Trigger}s
     *
     * @param UID unique id of composite condition module type
     *
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param modules is an ordered list of {@link Trigger}(s)
     * @param inputs is a {@link Set} of {@link Input} definitions.
     *
     */
    public CompositeConditionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs,
            List<Condition> modules) {
        super(UID, configDescriptions, inputs);
        this.modules = modules;

    }

    /**
     * This method is used for getting Conditions of the CompositeCoditionType.
     *
     * @return ordered set of Condition of this CompositeCoditionType.
     */
    public List<Condition> getModules() {
        return modules;
    }

}
