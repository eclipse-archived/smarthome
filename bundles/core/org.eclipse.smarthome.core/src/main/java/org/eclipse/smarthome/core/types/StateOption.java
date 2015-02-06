/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

/**
 * Describes one possible value an item might have.
 *
 * @author Dennis Nobel - Initial contribution
 */
public final class StateOption {

    private String value;
    private String label;

    /**
     * Creates a {@link StateOption} object.
     *
     * @param value
     *            value of the item
     * @param label
     *            label
     * @throws IllegalArgumentException
     *             if value is null
     */
    public StateOption(String value, String label) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null.");
        }
        this.value = value;
        this.label = label;
    }

    /**
     * Returns the label (can be null).
     *
     * @return label (can be null)
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the value (can not be null).
     *
     * @return value (can not be null)
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "StateOption [value=" + value + ", label=" + label + "]";
    }
}