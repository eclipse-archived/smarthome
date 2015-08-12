/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

/**
 * This enum is used to present status detail of rule, which can be considered as a sub-status.
 *
 * @author Yordan Mihaylov - Initial contribution
 * @author Kai Kreuzer - Refactored to match ThingStatusDetail implementation
 */
public enum RuleStatusDetail {
    NONE(0), HANDLER_MISSING_ERROR(1), HANDLER_INITIALIZING_ERROR(2), CONFIGURATION_ERROR(3);

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
