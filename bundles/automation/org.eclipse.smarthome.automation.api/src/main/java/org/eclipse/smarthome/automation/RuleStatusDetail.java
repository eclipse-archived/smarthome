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
 * This enum is used to present status detail of rule, which can be considered as a sub-status.
 *
 * @author Yordan Mihaylov - Initial contribution
 * @author Kai Kreuzer - Refactored to match ThingStatusDetail implementation
 */
public enum RuleStatusDetail {
    NONE(0),
    HANDLER_MISSING_ERROR(1),
    HANDLER_INITIALIZING_ERROR(2),
    CONFIGURATION_ERROR(3),
    TEMPLATE_MISSING_ERROR(4),
    INVALID_RULE(5),
    DISABLED(6);

    private final int value;

    private RuleStatusDetail(final int newValue) {
        value = newValue;
    }

    /**
     * Gets the value of a thing status detail.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
