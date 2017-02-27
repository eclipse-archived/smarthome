package org.eclipse.smarthome.core.autoupdate;

/**
 * This class defines an interface to configure an auto-update service programmatical.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public interface AutoUpdateConfigurator {

    /**
     * Add an auto-update configuration for a binding ID.
     *
     * @param bindingId the ID of the binding
     * @param autoUpdate the auto-update setting
     * @throws IllegalArgumentException if there is already a configuration for the given binding ID
     */
    void addAutoUpdateByBindingIdConfig(String bindingId, boolean autoUpdate) throws IllegalArgumentException;

    /**
     * Remove an auto-update configuration for a binding ID.
     *
     * @param bindingId the ID of the binding
     * @throws IllegalArgumentException if there is no configuration for the given binding ID
     */
    void removeAutoUpdateByBindingIdConfig(String bindingId) throws IllegalArgumentException;

}
