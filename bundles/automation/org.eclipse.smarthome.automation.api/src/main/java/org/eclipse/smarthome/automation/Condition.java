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
package org.eclipse.smarthome.automation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Condition module is used into "IF" section of the {@link Rule} definition. The "IF" section defines conditions which
 * must be satisfied to continue {@link Rule} execution. Building elements of condition.
 * {@link ConfigDescriptionParameter}s and {@link Input}s are defined by {@link ConditionType}. Conditions don't have
 * {@link Output} elements.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
@NonNullByDefault
public interface Condition extends Module {

    /**
     * This method is used to get input connections of the Condition. The connections
     * are links between {@link Input}s of the current {@link Module} and {@link Output}s of other
     * {@link Module}s.
     *
     * @return map that contains the inputs of this condition.
     */
    Map<String, String> getInputs();

}
