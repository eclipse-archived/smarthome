/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

/**
 * @author Wouter Born - Add support for MultiZone light control
 */
public enum ApplicationRequest {

    /**
     * Don't apply the requested changes until a message with APPLY or APPLY_ONLY is sent.
     */
    NO_APPLY(0x00),

    /**
     * Apply the changes immediately and apply any pending changes.
     */
    APPLY(0x01),

    /**
     * Ignore the requested changes in this message and only apply pending changes.
     */
    APPLY_ONLY(0x02);

    private final int value;

    private ApplicationRequest(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this application request.
     *
     * @return the integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the {@link ApplicationRequest} for the given integer value.
     *
     * @param value the integer value
     * @return the {@link ApplicationRequest} or <code>null</code>, if no {@link ApplicationRequest} exists for the
     *         given value
     */
    public static ApplicationRequest fromValue(int value) {
        for (ApplicationRequest ar : values()) {
            if (ar.getValue() == value) {
                return ar;
            }
        }

        return null;
    }
}
