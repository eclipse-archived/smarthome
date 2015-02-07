/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing.beans;

import java.util.Set;

import org.eclipse.smarthome.core.types.StateDescription;

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
    private String category;
    private StateDescription stateDescription;
    private boolean advanced;

    public ChannelDefinitionBean() {

    }

    public ChannelDefinitionBean(String id, String label, String description, Set<String> tags, String category,
            StateDescription stateDescription, boolean advanced) {
        this.description = description;
        this.label = label;
        this.id = id;
        this.tags = tags;
        this.category = category;
        this.stateDescription = stateDescription;
        this.advanced = advanced;
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

    public String getCategory() {
        return category;
    }

    public StateDescription getStateDescription() {
        return stateDescription;
    }

    public boolean isAdvanced() {
        return advanced;
    }
}
