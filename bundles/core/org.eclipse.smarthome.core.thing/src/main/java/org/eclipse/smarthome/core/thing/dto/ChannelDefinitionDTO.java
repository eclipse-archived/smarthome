/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This is a data transfer object that is used to serialize channel definitions.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Chris Jackson - Added properties
 */
public class ChannelDefinitionDTO {

    public String description;
    public String id;
    public String label;
    public Set<String> tags;
    public Map<String, String> properties;
    private String category;
    private StateDescription stateDescription;
    private boolean advanced;

    public ChannelDefinitionDTO() {

    }

    public ChannelDefinitionDTO(String id, String label, String description, Set<String> tags, String category,
            StateDescription stateDescription, boolean advanced, Map<String, String> properties) {
        this.description = description;
        this.label = label;
        this.id = id;
        this.tags = tags;
        this.category = category;
        this.stateDescription = stateDescription;
        this.advanced = advanced;
        this.properties = properties;
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
    
    public Map<String, String> getProperties() {
        return properties;   
    }

}
