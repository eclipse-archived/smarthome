/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.List;
import java.util.Set;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterDTO;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterGroupDTO;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This is a data transfer object that is used with to serialize channel types.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ChannelTypeDTO {

    public List<ConfigDescriptionParameterDTO> parameters;
    public List<ConfigDescriptionParameterGroupDTO> parameterGroups;
    public String description;
    public String label;
    public String category;
    public String itemType;
    public String kind;
    public StateDescription stateDescription;
    public Set<String> tags;
    public String UID;
    public boolean advanced;

    public ChannelTypeDTO() {
    }

    public ChannelTypeDTO(String UID, String label, String description, String category, String itemType,
            ChannelKind kind, List<ConfigDescriptionParameterDTO> parameters,
            List<ConfigDescriptionParameterGroupDTO> parameterGroups, StateDescription stateDescription,
            Set<String> tags, boolean advanced) {
        this.UID = UID;
        this.label = label;
        this.description = description;
        this.category = category;
        this.parameters = parameters;
        this.parameterGroups = parameterGroups;
        this.stateDescription = stateDescription;
        this.tags = tags;
        this.kind = kind.toString();
        this.itemType = itemType;
        this.advanced = advanced;
    }
}
