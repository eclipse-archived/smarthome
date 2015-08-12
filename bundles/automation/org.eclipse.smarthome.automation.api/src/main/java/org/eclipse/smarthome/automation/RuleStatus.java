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
        DISABLED(0),
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

    public enum StatusDetail {
        NONE(0),
        HANDLER_MISSING_ERROR(1),
        HANDLER_INITIALIZING_ERROR(2),
        CONFIGURATION_ERROR(3);

        private final int value;

        private StatusDetail(final int newValue) {
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

    public StatusDetail getStatusDetail();

    public String getDescription();
}
