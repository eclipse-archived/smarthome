/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.util.Locale;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.internal.Activator;

/**
 * The {@link TypeResolver} can be used to resolve {@link ThingType}, {@link ChannelType} and {@link ChannelGroupType}
 * objects. It provides static methods which do a look up for a type in the according registry.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class TypeResolver {

    /**
     * Resolves a {@link ChannelType} for the given channel type UID with the default {@link Locale}.
     *
     * @param channelTypeUID channel type UID
     * @return channel type or null if no channel type was found or channel type registry is not present
     */
    public static ChannelType resolve(ChannelTypeUID channelTypeUID) {
        return resolve(channelTypeUID, null);
    }

    /**
     * Resolves a {@link ChannelType} for the given channel type UID and the given {@link Locale}.
     *
     * @param channelTypeUID channel type UID
     * @param locale locale
     * @return channel type or null if no channel type was found or channel type registry is not present
     */
    public static ChannelType resolve(ChannelTypeUID channelTypeUID, Locale locale) {
        ChannelTypeRegistry channelTypeRegistry = getChannelTypeRegistry();
        return channelTypeRegistry != null ? channelTypeRegistry.getChannelType(channelTypeUID, locale) : null;
    }

    /**
     * Resolves a {@link ChannelGroupType} for the given channel group type UID with the default {@link Locale}.
     *
     * @param channelGroupTypeUID channel group type UID
     * @return channel group type or null if no channel group type was found or channel type registry is not present
     */
    public static ChannelGroupType resolve(ChannelGroupTypeUID channelGroupTypeUID) {
        return resolve(channelGroupTypeUID, null);
    }

    /**
     * Resolves a {@link ChannelGroupType} for the given channel group type UID and the given {@link Locale}.
     *
     * @param channelGroupTypeUID channel group type UID
     * @param locale locale
     * @return channel group type or null if no channel group type was found or channel type registry is not present
     */
    public static ChannelGroupType resolve(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        ChannelTypeRegistry channelTypeRegistry = getChannelTypeRegistry();
        return channelTypeRegistry != null ? channelTypeRegistry.getChannelGroupType(channelGroupTypeUID, locale)
                : null;
    }

    /**
     * Resolves a {@link ThingType} for the given thing type UID with the default {@link Locale}.
     *
     * @param thingTypeUID thing type UID
     * @return thing type type or null if no thing type was found or thing type registry is not present
     */
    public static ThingType resolve(ThingTypeUID thingTypeUID) {
        return resolve(thingTypeUID, null);
    }

    /**
     * Resolves a {@link ThingType} for the given thing type UID and the given {@link Locale}.
     *
     * @param thingTypeUID thing type UID
     * @param locale locale
     * @return thing type type or null if no thing type was found or thing type registry is not present
     */
    public static ThingType resolve(ThingTypeUID thingTypeUID, Locale locale) {
        ThingTypeRegistry thingTypeRegistry = getThingTypeRegistry();
        return thingTypeRegistry != null ? thingTypeRegistry.getThingType(thingTypeUID, locale) : null;
    }

    private static ChannelTypeRegistry getChannelTypeRegistry() {
        return Activator.getChannelTypeRegistry();
    }

    private static ThingTypeRegistry getThingTypeRegistry() {
        return Activator.getThingTypeRegistry();
    }

}
