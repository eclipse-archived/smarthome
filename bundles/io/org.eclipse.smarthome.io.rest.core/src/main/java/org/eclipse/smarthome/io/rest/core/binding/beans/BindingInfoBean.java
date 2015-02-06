/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.binding.beans;

import java.util.Set;

import org.eclipse.smarthome.io.rest.core.thing.beans.ThingTypeBean;

/**
 * This is a java bean that is used to serialize binding info objects to JSON.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class BindingInfoBean {

    public String author;
    public String description;
    public String id;
    public String name;
    public Set<ThingTypeBean> thingTypes;

    public BindingInfoBean() {
    }

    public BindingInfoBean(String id, String name, String author, String description, Set<ThingTypeBean> thingTypes) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.thingTypes = thingTypes;
    }

}
