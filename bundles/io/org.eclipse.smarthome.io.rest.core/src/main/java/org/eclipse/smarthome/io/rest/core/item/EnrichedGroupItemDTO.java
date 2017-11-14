/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.item;

import org.eclipse.smarthome.core.items.dto.GroupFunctionDTO;
import org.eclipse.smarthome.core.items.dto.GroupItemDTO;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This is an enriched data transfer object that is used to serialize group items.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class EnrichedGroupItemDTO extends EnrichedItemDTO {

    public EnrichedGroupItemDTO(ItemDTO itemDTO, EnrichedItemDTO[] members, String link, String state,
            String transformedState, StateDescription stateDescription) {
        super(itemDTO, link, state, transformedState, stateDescription);
        this.members = members;
        this.groupType = ((GroupItemDTO) itemDTO).groupType;
        this.function = ((GroupItemDTO) itemDTO).function;
    }

    public EnrichedItemDTO[] members;
    public String groupType;
    public GroupFunctionDTO function;

}
