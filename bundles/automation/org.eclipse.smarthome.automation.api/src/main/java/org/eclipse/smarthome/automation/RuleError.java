/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

/**
 * This interface defines common description of rule error. The rule error occurs when a rule can't be initialized for
 * some reason.
 *
 * @author Yordan Mihaylov
 */
public interface RuleError {

    public enum Code {
        ERROR_CODE_MISSING_HANDLER(1),
        ERROR_CODE_MISSING_MODULE_TYPE(2);

        private final int value;

        private Code(final int newValue) {
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

    /**
     * Gets description of the problem
     *
     * @return error message
     */
    public String getMessage();

    /**
     * Gets code of the error
     *
     * @return error code.
     */
    public Code getCode();

}
