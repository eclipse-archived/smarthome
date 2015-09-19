
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.data;

import org.eclipse.smarthome.documentation.models.Model;

/**
 * @author Alexander Kammerer <alexander.kammerer@online.de>
 *
 * Used for Mustache templates. Works as placeholder for the concrete object implementation.
 */
public class ListElement {
    /**
     * The concrete implementation.
     */
    protected Model element;

    /**
     * Last element in the list?
     */
    protected boolean isLast;

    /**
     * @param model The model the {@link ListElement} is created for.
     */
    public ListElement(Model model) {
        setModel(model);
    }

    /**
     * Setter for isLast.
     *
     * @param isLast Is the model the last in the list?
     */
    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public boolean last() {
        return isLast;
    }

    /**
     * Setter for element.
     * @param model The model we want to handle in this {@link ListElement}.
     */
    public void setModel(Model model) {
        this.element = model;
    }

    /**
     * @return Returns the current model of this {@link ListElement}.
     */
    public Model model() {
        return element;
    }
}
