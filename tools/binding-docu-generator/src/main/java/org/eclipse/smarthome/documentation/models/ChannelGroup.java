
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

import org.eclipse.smarthome.documentation.data.ChannelRefList;
import org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.ChannelGroupType;

/**
 * @author Alexander Kammerer <alexander.kammerer@online.de>
 *
 * Wrapper class to not fully depend on the existing models.
 */
public class ChannelGroup implements Model<ChannelGroupType> {
    /**
     * The attribute holding the original instance from the XML parser.
     */
    protected ChannelGroupType channel;

    /**
     * Default constructor.
     */
    public ChannelGroup() {
    }

    /**
     * Constructor.
     *
     * @param channel The instance from the XML parser.
     */
    public ChannelGroup(ChannelGroupType channel) {
        setModel(channel);
    }

    /**
     * @return The original instance from the XML parser.
     */
    public ChannelGroupType getRealImpl() {
        return channel;
    }

    /**
     * Set the model.
     *
     * @param channel The original instance from the XML parser.
     */
    public void setModel(ChannelGroupType channel) {
        this.channel = channel;
    }

    /**
     * @return Id of the channel.
     */
    public String id() {
        return channel.getId();
    }

    /**
     * @return Description of the channel.
     */
    public String description() {
        return channel.getDescription();
    }

    /**
     * @return Label of the channel.
     */
    public String label() {
        return channel.getLabel();
    }

    /**
     * @return All the channel associated with this group.
     */
    public ChannelRefList channels() {
        ChannelRefList channelRefs = new ChannelRefList();
        if (channel.getChannels() != null) {
            for (org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.Channel ch : channel.getChannels().getChannel()) {
                channelRefs.put(ch);
            }
        }
        return channelRefs;
    }


}
