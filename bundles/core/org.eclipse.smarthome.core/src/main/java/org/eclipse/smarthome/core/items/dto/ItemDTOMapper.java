/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items.dto;

import java.util.Set;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;

import com.google.common.base.Preconditions;

/**
 * The {@link ItemDTOMapper} is an utility class to map items into item data transfer objects (DTOs).
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Stefan Bußweiler - Moved to core and renamed class to DTO mapper
 * @author Dennis Nobel - Removed dynamic data
 */
public class ItemDTOMapper {

    /**
     * Maps item DTO into item object.
     *
     * @param itemDTO the DTO
     * @param itemFactories the item factories in order to create the items
     * @return the item object
     */
    public static Item map(ItemDTO itemDTO, Set<ItemFactory> itemFactories) {
        Preconditions.checkArgument(itemDTO != null, "The argument 'itemDTO' must no be null.");
        Preconditions.checkArgument(itemFactories != null, "The argument 'itemFactories' must no be null.");

        GenericItem newItem = null;
        if (itemDTO.type != null) {
            if (itemDTO.type.equals("GroupItem")) {
                newItem = new GroupItem(itemDTO.name);
            } else {
                String itemType = itemDTO.type.substring(0, itemDTO.type.length() - 4);
                for (ItemFactory itemFactory : itemFactories) {
                    newItem = itemFactory.createItem(itemType, itemDTO.name);
                    if (newItem != null) {
                        break;
                    }
                }
            }
            if (newItem != null) {
                newItem.setLabel(itemDTO.label);
                newItem.setCategory(itemDTO.category);
                newItem.addGroupNames(itemDTO.groupNames);
                newItem.addTags(itemDTO.tags);
            }
        }
        return newItem;
    }

    /**
     * Maps item into item DTO object.
     *
     * @param item the item
     * @param drillDown the drill down
     * @param uri the uri
     * @return item DTO object
     */
    public static ItemDTO map(Item item) {
        ItemDTO itemDTO = item instanceof GroupItem ? new GroupItemDTO() : new ItemDTO();
        fillProperties(itemDTO, item);
        return itemDTO;
    }

    private static void fillProperties(ItemDTO itemDTO, Item item) {
        if (item instanceof GroupItem) {
            GroupItem groupItem = (GroupItem) item;
            GroupItemDTO groupItemDTO = (GroupItemDTO) itemDTO;
            if (groupItem.getBaseItem() != null) {
                groupItemDTO.groupType = groupItem.getBaseItem().getType();
            }
        }
        itemDTO.name = item.getName();
        itemDTO.type = item.getClass().getSimpleName();
        itemDTO.label = item.getLabel();
        itemDTO.tags = item.getTags();
        itemDTO.category = item.getCategory();
        itemDTO.groupNames = item.getGroupNames();
    }

}
