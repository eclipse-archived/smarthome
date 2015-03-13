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

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This class is used to define trigger types. The triggers types contains meta
 * info of the {@link Trigger} instances. Each trigger type has unique id in
 * scope of the rule engine and defines {@link ConfigDescriptionParameter}s and {@link Output}s of the {@link Trigger}
 * instance.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TriggerType extends ModuleType {

    private Set<Output> outputs;

    /**
     * Constructor of TriggerType.
     *
     * @param UID unique id of the trigger type
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param outputs is a {@link Set} of {@link Output} definitions.
     */
    public TriggerType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Output> outputs) {
        super(UID, configDescriptions);
        this.outputs = outputs;
    }

    /**
     * This method is used for getting the meta info of inputs defined by this
     * module type.<br/>
     *
     * @return a {@link Set} of {@link Input} definitions.
     */
    public Set<Output> getOutputs() {
        return outputs;
    }

}
