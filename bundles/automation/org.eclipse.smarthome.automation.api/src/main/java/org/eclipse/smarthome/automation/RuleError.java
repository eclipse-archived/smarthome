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

    /**
     * Error code constant of missing module type handler. RuleError has this code when a module of the rule can't
     * find its module type handler.
     */
    public static final int ERROR_CODE_MISSING_HANDLER = 1;

    /**
     * Error code constant of missing module type. Rule error has this code when the rule contains module of undefined
     * type.
     */
    public static final int ERROR_CODE_MISSING_MODULE_TYPE = 2;

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
    public int getCode();

}
