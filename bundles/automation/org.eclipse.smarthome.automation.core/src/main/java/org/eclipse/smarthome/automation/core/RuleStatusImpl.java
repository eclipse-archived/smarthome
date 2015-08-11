/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

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

    private RuleError error;
    private Status status;

    /**
     * // * Utility constructor creating RuleStatusImpl object for initialized rule.
     * // *
     * // * @param enabled true then the rule is enabled
     * // * @param running true then the rule is executing
     * //
     */
    public RuleStatusImpl(Status status, RuleError error) {
        this.status = status;
        this.error = error;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public RuleError getError() {
        return error;
    }

    public void setError(RuleError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return status != null ? getStatusName(status) + " : " + error : "UNKNOWN";
    }

    private String getStatusName(Status status) {
        switch (status) {
            case NOT_ENABLED:
                return "NOT ENABLED";
            case NOT_INITIALIZED:
                return "NOT INITIALIZED";
            case IDLE:
                return "IDLE";
            case RUNNING:
                return "RUNNING";

            default:
                return "INKNOWN_STATUS_VALUE";
        }
    }
}
