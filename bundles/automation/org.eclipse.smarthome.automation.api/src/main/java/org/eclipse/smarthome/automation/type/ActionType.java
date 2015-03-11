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

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This interface provides common functionality for creating {@link Action} instances. The actions are part of "THEN"
 * section of the {@link Rule}. Each
 * action ModuleType is defined by unique id and defines meta info for {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s of
 * created {@link Action} instances.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public class ActionType extends ModuleType {

    private Set<Input> inputs;
    private Set<Output> outputs;

    /**
     * Constructor of ActionDescriptor.
     *
     * @param UID unique id of action type
     *
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} definitions.
     */
    public ActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs) {
        this(UID, configDescriptions, inputs, null);
    }

    /**
     * Default constructor of ActionDescriptor. Constructs an empty
     * ActionDescriptor.
     *
     * @param UID unique id of action type
     *
     * @param configDescriptions is a {@link Set} of configuration descriptions.
     * @param inputs is a {@link Set} of {@link Input} definitions.
     * @param outputs is a {@link Set} of {@link Output} definitions.
     */
    public ActionType(String UID, Set<ConfigDescriptionParameter> configDescriptions, Set<Input> inputs,
            Set<Output> outputs) {
        super(UID, configDescriptions);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * This method is used for getting the meta info of inputs defined by this
     * descriptor.<br/>
     *
     * @return a {@link Set} of {@link Input} definitions.
     */
    public Set<Input> getInputs() {
        return inputs;
    }

    /**
     * This method is used for getting the meta info of outputs defined by this
     * descriptor.<br/>
     *
     * @return a {@link Set} of {@link Output} definitions.
     */
    public Set<Output> getOutputs() {
        return outputs;
    }

}
