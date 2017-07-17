/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

/**
 * An enum of available compare operators
 * @author Benedikt Niehues
 *
 */
public enum CompareOperators {
    EQUALS("="),
    LT("<"),
    GT(">"),
    NOTEQUAL("!="),
    LT_EQ("<="),
    GT_EQ(">="),
    BETWEEN("IN");

    final String expression;

    CompareOperators(String expression) {
        this.expression = expression;
    }

}
