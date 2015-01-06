/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.link.beans;

/**
 * This is a java bean that is used to serialize links.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class ItemChannelLinkBean {

    private String channelUID;
    private String itemName;

    public ItemChannelLinkBean() {
    }

    public ItemChannelLinkBean(String itemName, String channelUID) {
        this.itemName = itemName;
        this.channelUID = channelUID;
    }

    public String getChannelUID() {
        return channelUID;
    }

    public String getItemName() {
        return itemName;
    }

}
