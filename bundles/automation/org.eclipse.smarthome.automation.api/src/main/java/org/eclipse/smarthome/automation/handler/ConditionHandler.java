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
package org.eclipse.smarthome.automation.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;

/**
 * This interface is implemented by external modules, which are called by the
 * RuleManager, when it has to check if the {@link Condition} is satisfied or
 * not.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public interface ConditionHandler extends ModuleHandler {

    /**
     * Method that states if the Condition is satisfied or not
     *
     * @param context is an unmodifiable map containing condition input values and snapshot of trigger output
     *                values. The output ids are defined in form: ModuleId.outputId.
     * @return true if Condition is satisfied, false otherwise
     */
    public boolean isSatisfied(Map<String, Object> context);

}
