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
import org.eclipse.smarthome.io.rest.core.discovery.beans.DiscoveryResultBean;
import org.eclipse.smarthome.io.rest.core.item.ItemResource;
import org.eclipse.smarthome.io.rest.core.item.beans.GroupItemBean;
import org.eclipse.smarthome.io.rest.core.item.beans.ItemBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ChannelBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingBean;

public class BeanMapper {

    public static ItemBean mapItemToBean(Item item, boolean drillDown, String uriPath) {
        ItemBean bean = item instanceof GroupItem ? new GroupItemBean() : new ItemBean();
        fillProperties(bean, item, drillDown, uriPath);
        return bean;
    }

    public static ThingBean mapThingToBean(Thing thing) {
        return mapThingToBean(thing, null);
    }

    public static ThingBean mapThingToBean(Thing thing, String uriPath) {
        List<ChannelBean> channelBeans = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            ChannelBean channelBean = mapChannelToBean(channel);
            channelBeans.add(channelBean);
        }

        String thingUID = thing.getUID().toString();
        String bridgeUID = thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null;

        GroupItem groupItem = thing.getLinkedItem();
        GroupItemBean groupItemBean = groupItem != null ? (GroupItemBean) mapItemToBean(groupItem, true, uriPath)
                : null;

        return new ThingBean(thingUID, bridgeUID, thing.getStatusInfo(), channelBeans, thing.getConfiguration(),
                thing.getProperties(), groupItemBean);
    }

    public static ChannelBean mapChannelToBean(Channel channel) {
        List<String> linkedItemNames = new ArrayList<>();
        for (Item item : channel.getLinkedItems()) {
            linkedItemNames.add(item.getName());
        }
        return new ChannelBean(channel.getUID().getId(), channel.getAcceptedItemType().toString(), linkedItemNames);
    }

    public static DiscoveryResultBean mapDiscoveryResultToBean(DiscoveryResult discoveryResult) {
        ThingUID thingUID = discoveryResult.getThingUID();
        ThingUID bridgeUID = discoveryResult.getBridgeUID();

        return new DiscoveryResultBean(thingUID.toString(), bridgeUID != null ? bridgeUID.toString() : null,
                discoveryResult.getLabel(), discoveryResult.getFlag(), discoveryResult.getProperties());
    }

    private static void fillProperties(ItemBean bean, Item item, boolean drillDown, String uriPath) {
        if (item instanceof GroupItem && drillDown) {
            GroupItem groupItem = (GroupItem) item;
            Collection<ItemBean> members = new LinkedHashSet<ItemBean>();
            for (Item member : groupItem.getMembers()) {
                members.add(mapItemToBean(member, drillDown, uriPath));
            }
            ((GroupItemBean) bean).members = members.toArray(new ItemBean[members.size()]);
        }
        bean.name = item.getName();
        bean.state = item.getState().toString();
        bean.type = item.getClass().getSimpleName();
        if (uriPath != null) {
            bean.link = UriBuilder.fromUri(uriPath).path(ItemResource.PATH_ITEMS).path(bean.name).build()
                    .toASCIIString();
        }
        bean.label = item.getLabel();
        bean.tags = item.getTags();
        bean.category = item.getCategory();
        bean.stateDescription = item.getStateDescription();
        bean.groupNames = item.getGroupNames();
    }
}
