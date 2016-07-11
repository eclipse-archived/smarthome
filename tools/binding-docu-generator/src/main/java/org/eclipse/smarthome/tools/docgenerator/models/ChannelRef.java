package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.schemas.Channel;

public class ChannelRef implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.Channel> {
    /**
     * Instance from the XML parser.
     */
    private Channel delegate;

    /**
     * Default constructor.
     */
    public ChannelRef() {
    }

    /**
     * Constructor.
     *
     * @param delegate Instance from the XML parser.
     */
    public ChannelRef(Channel delegate) {
        this.delegate = delegate;
    }

    /**
     * @return Instance from the XML parser.
     */
    @Override
    public Channel getRealImpl() {
        return delegate;
    }

    /**
     * @param channel Instance from the XML parser.
     */
    @Override
    public void setModel(Channel channel) {
        this.delegate = channel;
    }

    /**
     * @return Id of the channel reference.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return Id of the channel referenced.
     */
    public String typeId() {
        return delegate.getTypeId();
    }
}
