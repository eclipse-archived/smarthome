/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
