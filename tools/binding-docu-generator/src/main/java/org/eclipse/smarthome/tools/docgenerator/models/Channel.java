package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.schemas.ChannelType;

/**
 * Wrapper class to not fully depend on the existing models.
 */
public class Channel implements Model<ChannelType> {
    /**
     * The object we obtained by the XML parser.
     */
    private ChannelType delegate;

    /**
     * Default constructor.
     */
    public Channel() {
    }

    /**
     * @param delegate The object from the XML parser.
     */
    public Channel(ChannelType delegate) {
        this.delegate = delegate;
    }

    /**
     * @return Returns the {@link ChannelType} instance.
     */
    @Override
    public ChannelType getRealImpl() {
        return delegate;
    }

    /**
     * Setter for model.
     *
     * @param channel The real model.
     */
    @Override
    public void setModel(ChannelType channel) {
        this.delegate = channel;
    }

    /**
     * @return The id of the channel.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return The item type of the channel.
     */
    public String itemType() {
        return delegate.getItemType();
    }

    /**
     * @return The state of the channel.
     */
    public State state() {
        return new State(delegate.getState());
    }

    /**
     * @return The description of the channel.
     */
    public String description() {
        return delegate.getDescription();
    }

    /**
     * @return The label of the channel.
     */
    public String label() {
        return delegate.getLabel();
    }

    /**
     * @return The category of the channel.
     */
    public String category() {
        return delegate.getCategory();
    }

    /**
     * @return A list of URIs for the configuration.
     */
    public String configDescriptionRef() {
        if (delegate.getConfigDescriptionRef() != null) {
            return delegate.getConfigDescriptionRef().getUri();
        } else {
            return "";
        }
    }

    /**
     * @return The configuration of the channel.
     */
    public ConfigDescription configDescription() {
        if (delegate.getConfigDescription() != null) {
            return new ConfigDescription(delegate.getConfigDescription());
        } else {
            return null;
        }
    }
}
