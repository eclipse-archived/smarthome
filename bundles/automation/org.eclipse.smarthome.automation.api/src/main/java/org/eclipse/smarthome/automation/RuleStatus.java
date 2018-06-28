/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation;

/**
 * This enum is used to present the main status of a rule.
 *
 * @author Yordan Mihaylov - Initial contribution
 * @author Kai Kreuzer - Refactored to match ThingStatus implementation
 */
public enum RuleStatus {
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
