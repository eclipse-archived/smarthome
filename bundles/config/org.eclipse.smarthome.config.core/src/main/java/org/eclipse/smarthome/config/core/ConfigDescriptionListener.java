package org.eclipse.smarthome.config.core;


/**
 * {@link ConfigDescriptionListener} can be implemented as a listener for added
 * and removed {@link ConfigDescription}s. It can be added to a
 * {@link ConfigDescriptionProvider}.
 * 
 * @see ConfigDescriptionProvider
 * @author Dennis Nobel - Initial contribution
 * 
 */
public interface ConfigDescriptionListener {

    /**
     * This method is called, when a {@link ConfigDescription} is added.
     * 
     * @param configDescription
     *            {@link ConfigDescription}, which was added
     */
    void configDescriptionAdded(ConfigDescription configDescription);

    /**
     * This method is called, when a {@link ConfigDescription} is removed.
     * 
     * @param configDescription
     *            {@link ConfigDescription}, which was removed
     */
    void configDescriptionRemoved(ConfigDescription configDescription);

}
