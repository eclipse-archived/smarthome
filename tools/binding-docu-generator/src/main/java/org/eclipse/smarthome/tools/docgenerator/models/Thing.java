/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.data.ChannelGroupRefList;
import org.eclipse.smarthome.tools.docgenerator.data.ChannelRefList;
import org.eclipse.smarthome.tools.docgenerator.schemas.ChannelGroup;
import org.eclipse.smarthome.tools.docgenerator.schemas.ThingType;

/**
 * Wrapper class to not fully depend on the existing models.
 */
public class Thing implements Model<ThingType> {
    /**
     * The instance from the XML parser.
     */
    private ThingType delegate;

    /**
     * Default constructor.
     */
    public Thing() {
    }

    /**
     * Constructor.
     *
     * @param delegate Original instance from the XML parser.
     */
    public Thing(ThingType delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The instance from the XML parser.
     */
    @Override
    public ThingType getRealImpl() {
        return delegate;
    }

    /**
     * Set the model.
     *
     * @param type Original instance from the XML parser.
     */
    @Override
    public void setModel(ThingType type) {
        this.delegate = type;
    }

    /**
     * @return Id of the thing.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return Label of the thing.
     */
    public String label() {
        return delegate.getLabel();
    }

    /**
     * @return Description of the thing.
     */
    public String description() {
        return delegate.getDescription();
    }

    /**
     * @return Configuration reference of the thing.
     */
    public String configDescriptionRef() {
        if (delegate.getConfigDescriptionRef() != null) {
            return delegate.getConfigDescriptionRef().getUri();
        } else {
            return "";
        }
    }

    /**
     * @return A list of channels.
     */
    public ChannelRefList channels() {
        ChannelRefList channelRefs = new ChannelRefList();
        if (delegate.getChannels() != null) {
            for (org.eclipse.smarthome.tools.docgenerator.schemas.Channel channel : delegate.getChannels().getChannel()) {
                channelRefs.put(channel);
            }
        }
        return channelRefs;
    }

    /**
     * @return A list of channel groups.
     */
    public ChannelGroupRefList channelGroups() {
        ChannelGroupRefList channels = new ChannelGroupRefList();
        if (delegate.getChannelGroups() != null) {
            for (ChannelGroup group : delegate.getChannelGroups().getChannelGroup()) {
                channels.put(group);
            }
        }
        return channels;
    }

    /**
     * @return The configuration for the thing.
     */
    public ConfigDescription configDescriptions() {
        if (delegate.getConfigDescription() != null) {
            return new ConfigDescription(delegate.getConfigDescription());
        } else {
            return null;
        }
    }


}
