/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

/**
 * This enum is used to present the main status of a rule.
 *
 * @author Yordan Mihaylov - Initial contribution
 * @author Kai Kreuzer - Refactored to match ThingStatus implementation
 */
public enum RuleStatus {
    DISABLED(0),
    UNINITIALIZED(1),
    INITIALIZING(2),
    IDLE(3),
    RUNNING(4);

    private final int value;

    private RuleStatus(final int newValue) {
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
