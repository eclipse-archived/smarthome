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

import java.util.Set;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is used to define condition types. The condition types contains
 * meta info of the {@link Condition} instances. Each condition type has unique
 * id in scope of the rule engine and defines {@link ConfigDescriptionParameter} s and {@link Input}s of the
 * {@link Condition} instance.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public class ConditionType extends ModuleType {

    private Set<Input> inputs;

    /**
     * Constructor of condition type.
     *
     * @param UID unique id of the condition type.
     *
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} definitions.
     * @param visibility defines if this type can be used by other users.
     *
     */
    public ConditionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs) {
        super(UID, configDescriptions);
        this.inputs = inputs;
    }

    /**
     * This method is used for getting the meta info of inputs defined by this
     * module type.
     *
     * @return a {@link Set} of {@link Input} definitions.
     */
    public Set<Input> getInputs() {
        return inputs;
    }
}
