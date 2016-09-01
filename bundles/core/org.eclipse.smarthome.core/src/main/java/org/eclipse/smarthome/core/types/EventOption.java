/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

/**
 * Describes one possible value an event might have.
 *
 * @author Moritz Kammerer - Initial contribution and API
 */
public final class EventOption {

    private String value;
    private String label;

    /**
     * Creates a {@link EventOption} object.
     *
     * @param value
     *            value of the event
     * @param label
     *            label
     * @throws IllegalArgumentException
     *             if value is null
     */
    public EventOption(String value, String label) {
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
        return "EventOption [value=" + value + ", label=" + label + "]";
    }
}