/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.io.rest.core.discovery.beans.DiscoveryResultBean;
import org.eclipse.smarthome.io.rest.core.item.ItemResource;
import org.eclipse.smarthome.io.rest.core.item.beans.GroupItemBean;
import org.eclipse.smarthome.io.rest.core.item.beans.ItemBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ChannelBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingBean;

public class BeanMapper {

    public static ItemBean mapItemToBean(Item item, boolean drillDown, String uriPath) {
        ItemBean bean;
        if (item instanceof GroupItem && drillDown) {
            GroupItem groupItem = (GroupItem) item;
            GroupItemBean groupBean = new GroupItemBean();
            Collection<ItemBean> members = new LinkedHashSet<ItemBean>();
            for (Item member : groupItem.getMembers()) {
                members.add(mapItemToBean(member, false, uriPath));
            }
            groupBean.members = members.toArray(new ItemBean[members.size()]);
            bean = groupBean;
        } else {
            bean = new ItemBean();
        }
        bean.name = item.getName();
        bean.state = item.getState().toString();
        bean.type = item.getClass().getSimpleName();
        bean.link = UriBuilder.fromUri(uriPath).path(ItemResource.PATH_ITEMS).path(bean.name).build().toASCIIString();
        bean.tags = item.getTags();

        return bean;
    }

    public static ThingBean mapThingToBean(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        List<ChannelBean> channelBeans = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            ChannelBean channelBean = mapChannelToBean(channel, itemChannelLinkRegistry);
            channelBeans.add(channelBean);
        }

        String thingUID = thing.getUID().toString();
        String bridgeUID = thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null;

        return new ThingBean(thingUID, bridgeUID, thing.getStatus(), channelBeans, thing.getConfiguration());
    }

    public static ChannelBean mapChannelToBean(Channel channel, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        String boundItem = itemChannelLinkRegistry.getBoundItem(channel.getUID());
        return new ChannelBean(channel.getUID().getId(), channel.getAcceptedItemType().toString(), boundItem);
    }

    public static DiscoveryResultBean mapDiscoveryResultToBean(DiscoveryResult discoveryResult) {
        ThingUID thingUID = discoveryResult.getThingUID();
        ThingUID bridgeUID = discoveryResult.getBridgeUID();

        return new DiscoveryResultBean(thingUID.toString(), bridgeUID != null ? bridgeUID.toString() : null,
                discoveryResult.getLabel(), discoveryResult.getFlag(), discoveryResult.getProperties());
    }
}
