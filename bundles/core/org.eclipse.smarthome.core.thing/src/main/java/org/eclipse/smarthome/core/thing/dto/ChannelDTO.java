/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.List;
import java.util.Map;

/**
 * This is a data transfer object that is used to serialize channels.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Chris Jackson - Added properties
 */
public class ChannelDTO {

    public List<String> linkedItems;
    public String id;
    public String itemType;
    public Map<String, String> properties;

    public ChannelDTO() {
    }

    public ChannelDTO(String id, String itemType, List<String> linkedItems, Map<String, String> properties) {
        this.id = id;
        this.itemType = itemType;
        this.linkedItems = linkedItems;
        this.properties = properties;
    }

}
