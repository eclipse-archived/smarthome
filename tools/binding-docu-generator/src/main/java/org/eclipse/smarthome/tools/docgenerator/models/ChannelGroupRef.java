/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

public class ChannelGroupRef implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup> {
    /**
     * Instance from the XML parser.
     */
    private org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup delegate;

    /**
     * Default constructor.
     */
    public ChannelGroupRef() {
    }

    /**
     * Constructor.
     *
     * @param delegate The instance from the XML parser.
     */
    public ChannelGroupRef(org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The instance from the XML parser.
     */
    @Override
    public org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup getRealImpl() {
        return delegate;
    }

    /**
     * Set the model.
     *
     * @param channel Instance from the XML parser.
     */
    @Override
    public void setModel(org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup channel) {
        this.delegate = channel;
    }

    /**
     * @return Id of the channel group reference.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return Id of the channel group.
     */
    public String typeId() {
        return delegate.getTypeId();
    }
}
