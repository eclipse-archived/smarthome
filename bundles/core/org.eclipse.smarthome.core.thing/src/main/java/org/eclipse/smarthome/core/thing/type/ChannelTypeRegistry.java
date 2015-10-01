/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@link ChannelTypeRegistry} tracks all {@link ChannelType}s and {@link ChannelGroupType}s provided by registered
 * {@link ChannelTypeProvider}s.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ChannelTypeRegistry {

    private List<ChannelTypeProvider> channelTypeProviders = new CopyOnWriteArrayList<>();

    /**
     * Returns all channel types with the default {@link Locale}.
     *
     * @return all channel types or empty list if no channel type exists
     */
    public List<ChannelType> getChannelTypes() {
        return getChannelTypes(null);
    }

    /**
     * Returns all channel types for the given {@link Locale}.
     *
     * @param locale (can be null)
     * @return all channel types or empty list if no channel type exists
     */
    public List<ChannelType> getChannelTypes(Locale locale) {
        List<ChannelType> channelTypes = new ArrayList<>();
        for (ChannelTypeProvider channelTypeProvider : channelTypeProviders) {
            channelTypes.addAll(channelTypeProvider.getChannelTypes(locale));
        }
        return Collections.unmodifiableList(channelTypes);
    }

    /**
     * Returns the channel type for the given UID with the default {@link Locale}.
     *
     * @return channel type or null if no channel type for the given UID exists
     */
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID) {
        return getChannelType(channelTypeUID, null);
    }

    /**
     * Returns the channel type for the given UID and the given {@link Locale}.
     *
     * @param locale (can be null)
     * @return channel type or null if no channel type for the given UID exists
     */
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        for (ChannelTypeProvider channelTypeProvider : channelTypeProviders) {
            ChannelType channelType = channelTypeProvider.getChannelType(channelTypeUID, locale);
            if (channelType != null) {
                return channelType;
            }
        }
        return null;
    }

    /**
     * Returns all channel group types with the default {@link Locale}.
     *
     * @return all channel group types or empty list if no channel group type exists
     */
    public List<ChannelGroupType> getChannelGroupTypes() {
        return getChannelGroupTypes(null);
    }

    /**
     * Returns all channel group types for the given {@link Locale}.
     *
     * @param locale (can be null)
     * @return all channel group types or empty list if no channel group type exists
     */
    public List<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        List<ChannelGroupType> channelGroupTypes = new ArrayList<>();
        for (ChannelTypeProvider channelTypeProvider : channelTypeProviders) {
            channelGroupTypes.addAll(channelTypeProvider.getChannelGroupTypes(locale));
        }
        return Collections.unmodifiableList(channelGroupTypes);
    }

    /**
     * Returns the channel group type for the given UID with the default {@link Locale}.
     *
     * @return channel group type or null if no channel group type for the given UID exists
     */
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID) {
        return getChannelGroupType(channelGroupTypeUID, null);
    }

    /**
     * Returns the channel group type for the given UID and the given {@link Locale}.
     *
     * @param locale (can be null)
     * @return channel group type or null if no channel group type for the given UID exists
     */
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        for (ChannelTypeProvider channelTypeProvider : channelTypeProviders) {
            ChannelGroupType channelGroupType = channelTypeProvider.getChannelGroupType(channelGroupTypeUID, locale);
            if (channelGroupType != null) {
                return channelGroupType;
            }
        }
        return null;
    }

    protected void addChannelTypeProvider(ChannelTypeProvider channelTypeProviders) {
        this.channelTypeProviders.add(channelTypeProviders);
    }

    protected void removeChannelTypeProvider(ChannelTypeProvider channelTypeProviders) {
        this.channelTypeProviders.remove(channelTypeProviders);
    }

}
