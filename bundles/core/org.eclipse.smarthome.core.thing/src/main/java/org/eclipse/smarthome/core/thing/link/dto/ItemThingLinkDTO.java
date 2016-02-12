/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
public class ItemThingLinkDTO extends AbstractLinkDTO {

    public String thingUID;

    /**
     * Default constructor for deserialization e.g. by Gson.
     */
    protected ItemThingLinkDTO() {
    }

    public ItemThingLinkDTO(String itemName, String thingUID) {
        super(itemName);
        this.thingUID = thingUID;
    }

}
