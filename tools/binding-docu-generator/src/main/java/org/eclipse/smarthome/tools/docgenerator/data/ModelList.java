package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Model;

import java.util.ArrayList;

/**
 * This structure is implemented to fit Mustache templates.
 * It enables us to handle lists in Mustache easily.
 */
public abstract class ModelList extends ArrayList<ListElement> {
    /**
     * Adds a new object to the list.
     *
     * @param impl The concrete implementation of the model.
     */
    public void put(Object impl) {
        // Create new Model.
        Model model = getNewModel();
        model.setModel(impl);

        // Update the last element
        getLast().setIsLast(false);
        this.add(new ListElement(model));
        getLast().setIsLast(true);
    }

    /**
     * @return Returns the last element of the list.
     */
    public ListElement getLast() {
        if (!this.isEmpty()) {
            return this.get(this.size() - 1);
        } else {
            return new ListElement(null);
        }
    }

    /**
     * @return Returns whether the list is empty.
     */
    public boolean empty() {
        return this.isEmpty();
    }

    /**
     * @return Returns a new model object.
     */
    public abstract Model getNewModel();

}
