/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.report;

/**
 * Exception thrown from the code analyis tool, if violations with high priority are found.
 
 * @author Svilen Valkanov
 *
 */
public class HighPriorityViolationException extends Exception {
    private static final long serialVersionUID = 1L;

    public HighPriorityViolationException(String message) {
        super(message);
    }

}
