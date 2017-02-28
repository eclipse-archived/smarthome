package org.eclipse.smarthome.core.autoupdate;

import org.eclipse.smarthome.core.thing.ChannelUID;

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
     * @throws IllegalStateException if there is already a configuration by channel UID of that binding ID
     */
    void addAutoUpdateByBindingIdConfig(String bindingId, boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove an auto-update configuration for a binding ID.
     *
     * @param bindingId the ID of the binding
     * @throws IllegalArgumentException if there is no configuration for the given binding ID
     */
    void removeAutoUpdateByBindingIdConfig(String bindingId) throws IllegalArgumentException;

    /**
     * Add an auto-update configuration for a channel UID.
     *
     * @param channelUID the channel UID
     * @param autoUpdate the auto-update setting
     * @throws IllegalArgumentException if there is already a configuration for the given channel UID
     * @throws IllegalStateException if there is already a configuration for the binding ID of that channel
     */
    void addAutoUpdateByChannelUidConfig(ChannelUID channelUid, boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove an auto-update configuration for a channel UID.
     *
     * @param channelUID the channel UID
     * @throws IllegalArgumentException if there is no configuration for the given channel UID
     */
    void removeAutoUpdateByChannelUidConfig(ChannelUID channelUid) throws IllegalArgumentException;

}
