/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

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

    public enum Status {
        NOT_ENABLED(0),
        NOT_INITIALIZED(1),
        IDLE(2),
        RUNNING(3);

        private final int value;

        private Status(final int newValue) {
            value = newValue;
        }

        /**
         * Gets the value of a rule status.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    public Status getStatus();

    /**
     * Gets rule error related with this status.
     *
     * @return {@link RuleError}s or null when there is no error related with status.
     */
    public RuleError getError();

}
