
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

import org.eclipse.smarthome.documentation.schemas.thing_description.v1_0.ChannelType;

/**
 * @author Alexander Kammerer <alexander.kammerer@online.de>
 *
 * Wrapper class to not fully depend on the existing models.
 */
public class Channel implements Model<ChannelType> {
    /**
     * The object we obtained by the XML parser.
     */
    protected ChannelType channel;

    /**
     * Default constructor.
     */
    public Channel() {
    }

    /**
     * @param channel The object from the XML parser.
     */
    public Channel(ChannelType channel) {
        setModel(channel);
    }

    /**
     * @return Returns the {@link ChannelType} instance.
     */
    public ChannelType getRealImpl() {
        return channel;
    }

    /**
     * Setter for model.
     *
     * @param channel The real model.
     */
    public void setModel(ChannelType channel) {
        this.channel = channel;
    }

    /**
     * @return The id of the channel.
     */
    public String id() {
        return channel.getId();
    }

    /**
     * @return The item type of the channel.
     */
    public String itemType() {
        return channel.getItemType();
    }

    /**
     * @return The state of the channel.
     */
    public State state() {
        return new State(channel.getState());
    }

    /**
     * @return The description of the channel.
     */
    public String description() {
        return channel.getDescription();
    }

    /**
     * @return The label of the channel.
     */
    public String label() {
        return channel.getLabel();
    }

    /**
     * @return The category of the channel.
     */
    public String category() {
        return channel.getCategory();
    }

    /**
     * @return A list of URIs for the configuration.
     */
    public String configDescriptionRef() {
        if (channel.getConfigDescriptionRef() != null) {
            return channel.getConfigDescriptionRef().getUri();
        } else {
            return "";
        }
    }

    /**
     * @return The configuration of the channel.
     */
    public ConfigDescription configDescription() {
        if (channel.getConfigDescription() != null) {
            return new ConfigDescription(channel.getConfigDescription());
        } else {
            return null;
        }
    }
}
