/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

import org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.ChannelGroup;

public class ChannelGroupRef implements Model<ChannelGroup> {
    /**
     * Instance from the XML parser.
     */
    private ChannelGroup channel;

    /**
     * Default constructor.
     */
    public ChannelGroupRef() {
    }

    /**
     * Constructor.
     *
     * @param channel The instance from the XML parser.
     */
    public ChannelGroupRef(ChannelGroup channel) {
        setModel(channel);
    }

    /**
     * @return The instance from the XML parser.
     */
    public ChannelGroup getRealImpl() {
        return channel;
    }

    /**
     * Set the model.
     *
     * @param channel Instance from the XML parser.
     */
    public void setModel(ChannelGroup channel) {
        this.channel = channel;
    }

    /**
     * @return Id of the channel group reference.
     */
    public String id() {
        return channel.getId();
    }

    /**
     * @return Id of the channel group.
     */
    public String typeId() {
        return channel.getTypeId();
    }
}
