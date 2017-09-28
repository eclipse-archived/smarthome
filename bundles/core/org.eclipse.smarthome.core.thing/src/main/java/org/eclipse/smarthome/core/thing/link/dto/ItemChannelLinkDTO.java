/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link.dto;

import java.util.Map;

/**
 * This is a data transfer object that is used to serialize links.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemChannelLinkDTO extends AbstractLinkDTO {

    public String channelUID;
    public Map<String, Object> configuration;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected ItemChannelLinkDTO() {
    }

    public ItemChannelLinkDTO(String itemName, String channelUID, Map<String, Object> configuration) {
        super(itemName);
        this.channelUID = channelUID;
        this.configuration = configuration;
    }

}
