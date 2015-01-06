/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAX-RS to serialize channels to JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 *
 */
@XmlRootElement(name = "channel")
public class ChannelBean {

    public String boundItem;

    public String id;

    public String itemType;

    public ChannelBean() {
    }

    public ChannelBean(String id, String itemType, String boundItem) {
        this.itemType = itemType;
        this.id = id;
        this.boundItem = boundItem;
    }

}
