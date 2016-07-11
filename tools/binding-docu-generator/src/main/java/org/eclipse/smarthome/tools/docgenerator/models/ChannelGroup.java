package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.data.ChannelRefList;
import org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroupType;

/**
 * Wrapper class to not fully depend on the existing models.
 */
public class ChannelGroup implements Model<ChannelGroupType> {
    /**
     * The attribute holding the original instance from the XML parser.
     */
    private ChannelGroupType delegate;

    /**
     * Default constructor.
     */
    public ChannelGroup() {
    }

    /**
     * Constructor.
     *
     * @param delegate The instance from the XML parser.
     */
    public ChannelGroup(ChannelGroupType delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The original instance from the XML parser.
     */
    @Override
    public ChannelGroupType getRealImpl() {
        return delegate;
    }

    /**
     * Set the model.
     *
     * @param channel The original instance from the XML parser.
     */
    @Override
    public void setModel(ChannelGroupType channel) {
        this.delegate = channel;
    }

    /**
     * @return Id of the channel.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return Description of the channel.
     */
    public String description() {
        return delegate.getDescription();
    }

    /**
     * @return Label of the channel.
     */
    public String label() {
        return delegate.getLabel();
    }

    /**
     * @return All the channel associated with this group.
     */
    public ChannelRefList channels() {
        ChannelRefList channelRefs = new ChannelRefList();
        if (delegate.getChannelRefs() != null) {
            for (org.eclipse.smarthome.tools.docgenerator.schemas.Channel ch : delegate.getChannelRefs().getChannel()) {
                channelRefs.put(ch);
            }
        }
        return channelRefs;
    }


}
