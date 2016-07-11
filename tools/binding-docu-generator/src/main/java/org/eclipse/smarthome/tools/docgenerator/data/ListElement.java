package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Model;

/**
 * Used for Mustache templates. Works as placeholder for the concrete object implementation.
 */
public class ListElement {
    /**
     * The concrete implementation.
     */
    protected Model model;

    /**
     * Last element in the list?
     */
    protected boolean isLast;

    /**
     * @param model The model the {@link ListElement} is created for.
     */
    public ListElement(Model model) {
        this.model = model;
    }

    /**
     * Setter for isLast.
     *
     * @param isLast Is the model the last in the list?
     */
    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }

    public boolean isLast() {
        return isLast;
    }

    /**
     * Setter for element.
     *
     * @param model The model we want to handle in this {@link ListElement}.
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * @return Returns the current model element of this {@link ListElement}.
     */
    public Model getModel() {
        return model;
    }
}
