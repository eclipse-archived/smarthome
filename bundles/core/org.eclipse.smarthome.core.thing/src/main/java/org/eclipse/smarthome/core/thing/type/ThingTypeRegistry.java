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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;

import com.google.common.collect.Lists;

/**
 * The {@link ThingTypeRegistry} tracks all {@link ThingType}s provided by registered {@link ThingTypeProvider}s.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Dennis Nobel - Added locale support
 */
public class ThingTypeRegistry {

    private List<ThingTypeProvider> thingTypeProviders = new CopyOnWriteArrayList<>();

    /**
     * Returns all thing types.
     *
     * @param locale
     *            locale (can be null)
     * @return all thing types
     */
    public List<ThingType> getThingTypes(Locale locale) {
        List<ThingType> thingTypes = new ArrayList<>();
        for (ThingTypeProvider thingTypeProvider : thingTypeProviders) {
            thingTypes.addAll(thingTypeProvider.getThingTypes(locale));
        }
        return Collections.unmodifiableList(thingTypes);
    }

    /**
     * Returns all thing types.
     *
     * @return all thing types
     */
    public List<ThingType> getThingTypes() {
        return getThingTypes((Locale) null);
    }

    /**
     * Returns thing types for a given binding id.
     *
     * @param bindingId
     *            binding id
     * @param locale
     *            locale (can be null)
     * @return thing types for given binding id
     */
    public List<ThingType> getThingTypes(String bindingId, Locale locale) {
        List<ThingType> thingTypesForBinding = Lists.newArrayList();

        for (ThingType thingType : getThingTypes()) {
            if (thingType.getBindingId().equals(bindingId)) {
                thingTypesForBinding.add(thingType);
            }
        }

        return Collections.unmodifiableList(thingTypesForBinding);
    }

    /**
     * Returns thing types for a given binding id.
     *
     * @param bindingId
     *            binding id
     * @return thing types for given binding id
     */
    public List<ThingType> getThingTypes(String bindingId) {
        return getThingTypes(bindingId, null);
    }

    /**
     * Returns a thing type for a given thing type UID.
     *
     * @param thingTypeUID
     *            thing type UID
     * @param locale
     *            locale (can be null)
     * @return thing type for given UID or null if no thing type with this UID
     *         was found
     */
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        for (ThingTypeProvider thingTypeProvider : thingTypeProviders) {
            ThingType thingType = thingTypeProvider.getThingType(thingTypeUID, locale);
            if (thingType != null) {
                return thingType;
            }
        }

        return null;
    }

    /**
     * Returns a thing type for a given thing type UID.
     *
     * @param thingTypeUID
     *            thing type UID
     * @return thing type for given UID or null if no thing type with this UID
     *         was found
     */
    public ThingType getThingType(ThingTypeUID thingTypeUID) {
        return getThingType(thingTypeUID, null);
    }

    /**
     * Returns the channel type for a given channel UID.
     *
     * <p>
     * <strong>Attention:</strong> If you iterate over multiple channels to find the according channel types, please
     * fetch the thing type first using
     * {@link ThingTypeRegistry#getThingType(ThingTypeUID)} and use
     * {@link ThingType#getChannelType(ChannelUID)} afterwards.
     * </p>
     *
     * @param channelUID channel UID
     * @return channel type or null if no channel type was found
     */
    public ChannelType getChannelType(ChannelUID channelUID) {
        return getChannelType(channelUID, null);
    }

    /**
     * Returns the channel type for a given channel UID and locale.
     *
     * <p>
     * <strong>Attention:</strong> If you iterate over multiple channels to find the according channel types, please
     * fetch the thing type first using
     * {@link ThingTypeRegistry#getThingType(ThingTypeUID)} and use
     * {@link ThingType#getChannelType(ChannelUID)} afterwards.
     * </p>
     *
     * @param channelUID channel UID
     * @param locale locale (can be null)
     * @return channel type or null if no channel type was found
     */
    public ChannelType getChannelType(ChannelUID channelUID, Locale locale) {
        ThingType thingType = this.getThingType(channelUID.getThingTypeUID(), locale);
        if (thingType != null) {
            ChannelTypeUID channelTypeUID = thingType.getChannelTypeUID(channelUID);
            return TypeResolver.resolve(channelTypeUID);
        }
        return null;
    }

    protected void addThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        if (thingTypeProvider != null) {
            this.thingTypeProviders.add(thingTypeProvider);
        }
    }

    protected void removeThingTypeProvider(ThingTypeProvider thingTypeProvider) {
        if (thingTypeProvider != null) {
            this.thingTypeProviders.remove(thingTypeProvider);
        }
    }

}
