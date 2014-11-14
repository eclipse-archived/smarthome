/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.Set;

/**
 * This is a java bean that is used to serialize channel definitions.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class ChannelDefinitionBean {

    public String description;
    public String id;
    public String label;
    public Set<String> tags;

    public ChannelDefinitionBean() {

    }

    public ChannelDefinitionBean(String id, String label, String description, Set<String> tags) {
        this.description = description;
        this.label = label;
        this.id = id;
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getTags() {
        return tags;
    }

}
