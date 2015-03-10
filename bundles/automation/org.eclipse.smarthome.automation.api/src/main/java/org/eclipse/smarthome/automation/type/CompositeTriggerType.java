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

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * CompositeTriggerType is as {@link TriggerType} which logically combines {@link Trigger} modules. The composite
 * trigger hides internal logic between
 * participating actions and it can be used as a regular {@link Trigger} module.
 *
 */
public class CompositeTriggerType extends TriggerType {

    private List<Trigger> modules;

    /**
     * Creates a CompositeTriggerType with ordered set of {@link Trigger}s
     *
     * @param UID unique id of this composite trigger type.
     *
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param modules is an ordered list of {@link Trigger}(s)
     * @param outputs is a {@link Set} of {@link Output} definitions.
     *
     */
    public CompositeTriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Output> outputs,
            List<Trigger> modules) {
        super(UID, configDescriptions, outputs);
        this.modules = modules;

    }

    /**
     * This method is used for getting Triggers of the CompositeTriggerDescriptor.
     *
     * @return ordered set of Triggers defined by CompositeTriggerType
     */
    public List<Trigger> getModules() {
        return modules;
    }

}
