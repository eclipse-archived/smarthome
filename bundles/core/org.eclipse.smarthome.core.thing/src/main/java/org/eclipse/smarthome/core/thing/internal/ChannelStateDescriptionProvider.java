/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionProvider;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ChannelStateDescriptionProvider} provides localized {@link StateDescription}s from the type of a
 * {@link Channel} bounded to an {@link Item}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ChannelStateDescriptionProvider implements StateDescriptionProvider {

    private final Logger logger = LoggerFactory.getLogger(ChannelStateDescriptionProvider.class);

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private ThingRegistry thingRegistry;
    private Integer rank;

    protected void activate(Map<String, Object> properties) {
        Object serviceRanking = properties.get(Constants.SERVICE_RANKING);
        if (serviceRanking instanceof Integer) {
            rank = (Integer) serviceRanking;
        } else {
            rank = 0;
        }
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public StateDescription getStateDescription(String itemName, Locale locale) {
        Set<ChannelUID> boundChannels = itemChannelLinkRegistry.getBoundChannels(itemName);
        if (!boundChannels.isEmpty()) {
            ChannelUID channelUID = boundChannels.iterator().next();
            Channel channel = thingRegistry.getChannel(channelUID);
            if (channel != null) {
                ChannelType channelType = thingTypeRegistry.getChannelType(channel, locale);
                if (channelType != null) {
                    StateDescription state = channelType.getState();
                    if ((channelType.getItemType() != null) && ((state == null) || (state.getPattern() == null))) {
                        String pattern = null;
                        if (channelType.getItemType().equalsIgnoreCase("String")) {
                            pattern = "%s";
                        } else if (channelType.getItemType().equalsIgnoreCase("Number")) {
                            pattern = "%.0f";
                        }
                        if (pattern != null) {
                            logger.trace("Provide a default pattern {} for item {}", pattern, itemName);
                            if (state == null) {
                                state = new StateDescription(null, null, null, pattern, false, null);
                            } else {
                                state = new StateDescription(state.getMinimum(), state.getMaximum(), state.getStep(),
                                        pattern, state.isReadOnly(), state.getOptions());
                            }
                        }
                    }
                    return state;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

}
