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
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Actions are the part of "THEN" section of the {@link Rule} definition. Elements of this section are expected result
 * of {@link Rule} execution. The Action can have {@link Output} elements. These actions are used to process input data
 * as source data of other Actions. Building elements of actions ( {@link ConfigDescriptionParameter}s, {@link Input}s
 * and {@link Output}s) are defined by {@link ActionType}
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
@NonNullByDefault
public interface Action extends Module {

    /**
     * This method is used to get input connections of the Action. The connections
     * are links between {@link Input}s of the this {@link Module} and {@link Output}s
     * of other {@link Module}s.
     *
     * @return map that contains the inputs of this action.
     */
    Map<String, String> getInputs();

}
