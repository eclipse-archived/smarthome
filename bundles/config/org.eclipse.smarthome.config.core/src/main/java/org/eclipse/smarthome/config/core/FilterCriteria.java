/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

/**
 * The {@link FilterCriteria} specifies a filter for dynamic selection list
 * providers of a {@link ConfigDescriptionParameter}.
 * <p>
 * The {@link FilterCriteria} and its name is related to the context of the containing
 * {@link ConfigDescriptionParameter}.
 *
 * @author Alex Tugarev - Initial Contribution
 * @author Markus Rathgeb - Add default constructor for deserialization
 *
 */
public class FilterCriteria {

    private String value;
    private String name;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected FilterCriteria() {
    }

    public FilterCriteria(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [name=\"" + name + "\", value=\"" + value + "\"]";
    }

}
