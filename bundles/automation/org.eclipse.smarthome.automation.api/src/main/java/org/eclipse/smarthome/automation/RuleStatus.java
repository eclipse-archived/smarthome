/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.List;

/**
 * This interface is used to present status of rule. The status has following properties:
 * The rule can be enable/disable - this property can be set by the user when the rule
 * must stop work for temporary period of time. The rule can me running when it is
 * executing triggered data. The rule can be not initialized when some of module handlers
 * are not available.
 *
 * @author Yordan Mihaylov
 */
public interface RuleStatus {

    /**
     * Gets enable status of the rule. When it is disabled the rule must not be executed.
     * This property can be set by the user through the {@link RuleRegistry#setEnabled(String, boolean)} method.
     * 
     * @return true when the rule is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Gets running status of the rule. The is running when it executes triggered data.
     * 
     * @return true when it is running, false when the rule is idle.
     */
    boolean isRunning();

    /**
     * Gets initialization status of the rule. It is initialized when the rule engine accept the rule without
     * {@link RuleError}s
     * 
     * @return true when it is initialized, false when rule error is appeared.
     */
    boolean isInitialize();

    /**
     * Gets rule error of not initialized rule.
     * 
     * @return list of {@link RuleError}s or null when the rule is initialized.
     */
    public List<RuleError> getErrors();
}
