/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.RuleError;

/**
 * @author Yordan Mihaylov
 *
 */
public class RuleErrorImpl implements RuleError {

    private String message;
    private RuleError.Code code;

    public RuleErrorImpl(RuleError.Code code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public RuleError.Code getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code != null ? "(" + getErrorName(code) + ") - " + message : "UNKNOWN";
    }

    private String getErrorName(Code code) {
        switch (code) {
            case ERROR_CODE_MISSING_HANDLER:
                return "MISSING HANDLER";
            case ERROR_CODE_MISSING_MODULE_TYPE:
                return "MISSING MODULE TYPE";

            default:
                return "UNKNOWN ERROR CODE ";
        }
    }
}
