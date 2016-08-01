/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.dto;

/**
 * This is a data transfer object that is used to serialize links.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemChannelLinkDTO extends AbstractLinkDTO {

    public String channelUID;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected ItemChannelLinkDTO() {
    }

    public ItemChannelLinkDTO(String itemName, String channelUID) {
        super(itemName);
        this.channelUID = channelUID;
    }

}
