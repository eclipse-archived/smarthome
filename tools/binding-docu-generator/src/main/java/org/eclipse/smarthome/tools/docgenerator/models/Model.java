package org.eclipse.smarthome.tools.docgenerator.models;

/**
 * The models implementing this interface act as an additional layer between the data we
 * parse from the XML files and the concrete use of these models in the template system.
 */
public interface Model<T> {
    /**
     * @param impl Setter for the model.
     */
    void setModel(T impl);

    /**
     * @return Returns the {@link T} object.
     */
    T getRealImpl();
}
