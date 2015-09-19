
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

import org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.Channel;

public class ChannelRef implements Model<org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.Channel> {
    /**
     * Instance from the XML parser.
     */
    private Channel channel;

    /**
     * Default constructor.
     */
    public ChannelRef() {
    }

    /**
     * Constructor.
     *
     * @param channel Instance from the XML parser.
     */
    public ChannelRef(Channel channel) {
        setModel(channel);
    }

    /**
     * @return Instance from the XML parser.
     */
    public Channel getRealImpl() {
        return channel;
    }

    /**
     * @param channel Instance from the XML parser.
     */
    public void setModel(Channel channel) {
        this.channel = channel;
    }

    /**
     * @return Id of the channel reference.
     */
    public String id() {
        return channel.getId();
    }

    /**
     * @return Id of the channel referenced.
     */
    public String typeId() {
        return channel.getTypeId();
    }
}
