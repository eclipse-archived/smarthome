/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation;

/**
 * @author Yordan Mihaylov
 *
 */
public interface RuleError {

    public static final int ERROR_CODE_MISSING_HANDLER = 1;

    public static final int ERROR_CODE_MISSING_MODULE_TYPE = 2;

    public String getMessage();

    public int getCode();

}
