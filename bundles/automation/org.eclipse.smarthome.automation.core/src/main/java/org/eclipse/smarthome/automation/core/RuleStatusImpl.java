/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.List;

import org.eclipse.smarthome.automation.RuleError;
import org.eclipse.smarthome.automation.RuleStatus;

/**
 * This class is used to present status of rule. The status has following properties:
 * The rule can be enable/disable - this property can be set by the user when the rule
 * must stop work for temporary period of time. The rule can me running when it is
 * executing triggered data. The rule can be not initialized when some of module handlers
 * are not available.
 * 
 * @author Yordan Mihaylov
 */
public class RuleStatusImpl implements RuleStatus {

    private boolean enabled;
    private boolean running;
    private boolean initialize;
    private List<RuleError> errors;

    /**
     * Utility constructor creating RuleStatusImpl object for initialized rule.
     * 
     * @param enabled true then the rule is enabled
     * @param running true then the rule is executing
     */
    public RuleStatusImpl(boolean enabled, boolean running) {
        this(enabled, running, true, null);
    }

    public RuleStatusImpl(boolean enabled, boolean running, boolean initialize, List<RuleError> errors) {
        this.enabled = enabled;
        this.running = running;
        this.initialize = initialize;
        this.errors = errors;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isInitialize() {
        return initialize;
    }

    public List<RuleError> getErrors() {
        return errors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object status) {
        if (status instanceof RuleStatus) {
            RuleStatus s = (RuleStatus) status;
            if (isEnabled() == s.isEnabled() && isRunning() == s.isRunning() && isInitialize() == s.isInitialize()) {
                if (s.getErrors() == null) {
                    return getErrors() == null;
                }
                return s.getErrors().equals(getErrors());
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (isEnabled() ? 1 : 0) + (isInitialize() ? 1 << 2 : 0) + (isRunning() ? 1 << 3 : 0)
                + (errors != null ? errors.hashCode() : 0);

    }
}
