/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.items.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.internal.items.GroupFunctionHelper;
import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.LoggerFactory;

/**
 * The {@link ItemDTOMapper} is an utility class to map items into item data transfer objects (DTOs).
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Stefan Bußweiler - Moved to core and renamed class to DTO mapper
 * @author Dennis Nobel - Removed dynamic data
 */
@NonNullByDefault
public class ItemDTOMapper {

    private static final GroupFunctionHelper GROUP_FUNCTION_HELPER = new GroupFunctionHelper();

    /**
     * Maps item DTO into item object.
     *
     * @param itemDTO the DTO
     * @param itemFactories the item factories in order to create the items
     * @return the item object
     */
    public static @Nullable ActiveItem map(ItemDTO itemDTO, Set<ItemFactory> itemFactories) {
        if (itemDTO == null) {
            throw new IllegalArgumentException("The argument 'itemDTO' must no be null.");
        }
        if (itemFactories == null) {
            throw new IllegalArgumentException("The argument 'itemFactories' must no be null.");
        }

        GenericItem newItem = null;
        if (itemDTO.type != null) {
            if (itemDTO instanceof GroupItemDTO && itemDTO.type.equals(GroupItem.TYPE)) {
                GroupItemDTO groupItemDTO = (GroupItemDTO) itemDTO;
                GenericItem baseItem = null;
                if (!StringUtils.isEmpty(groupItemDTO.groupType)) {
                    baseItem = createItem(groupItemDTO.groupType, itemDTO.name, itemFactories);
                }
                GroupFunction function = new GroupFunction.Equality();
                if (groupItemDTO.function != null) {
                    function = mapFunction(baseItem, groupItemDTO.function);
                }
                newItem = new GroupItem(itemDTO.name, baseItem, function);
            } else {
                String itemType = itemDTO.type;
                newItem = createItem(itemType, itemDTO.name, itemFactories);
            }
            if (newItem != null) {
                if (itemDTO.label != null) {
                    newItem.setLabel(itemDTO.label);
                }
                if (itemDTO.category != null) {
                    newItem.setCategory(itemDTO.category);
                }
                if (itemDTO.groupNames != null) {
                    newItem.addGroupNames(itemDTO.groupNames);
                }
                if (itemDTO.tags != null) {
                    newItem.addTags(itemDTO.tags);
                }
            }
        }
        return newItem;
    }

    public static GroupFunction mapFunction(@Nullable Item baseItem, GroupFunctionDTO function) {
        List<State> args = parseStates(baseItem, function.params);

        return GROUP_FUNCTION_HELPER.createGroupFunction(function, args, getDimension(baseItem));
    }

    private static @Nullable Class<? extends Quantity<?>> getDimension(@Nullable Item baseItem) {
        if (baseItem instanceof NumberItem) {
            return ((NumberItem) baseItem).getDimension();
        }

        return null;
    }

    private static List<State> parseStates(@Nullable Item baseItem, String @Nullable [] params) {
        List<State> states = new ArrayList<State>();

        if (params == null || baseItem == null) {
            return states;
        }

        for (String param : params) {
            State state = TypeParser.parseState(baseItem.getAcceptedDataTypes(), param);
            if (state == null) {
                LoggerFactory.getLogger(ItemDTOMapper.class).warn(
                        "State '{}' is not valid for a group item with base type '{}'",
                        new Object[] { param, baseItem.getType() });
                states.clear();
                break;
            } else {
                states.add(state);
            }
        }
        return states;
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
                groupItemDTO.function = mapFunction(groupItem.getFunction());
            }
        }

        itemDTO.name = item.getName();
        itemDTO.type = item.getType();
        itemDTO.label = item.getLabel();
        itemDTO.tags = item.getTags();
        itemDTO.category = item.getCategory();
        itemDTO.groupNames = item.getGroupNames();
    }

    public static @Nullable GroupFunctionDTO mapFunction(@Nullable GroupFunction function) {
        if (function == null) {
            return null;
        }

        GroupFunctionDTO dto = new GroupFunctionDTO();
        dto.name = function.getClass().getSimpleName().toUpperCase();
        List<String> params = new ArrayList<>();
        for (State param : function.getParameters()) {
            params.add(param.toString());
        }
        if (!params.isEmpty()) {
            dto.params = params.toArray(new String[params.size()]);
        }

        return dto;
    }

    /**
     * helper: Create new item with name and type
     *
     * @param itemType type of the item
     * @param itemname name of the item
     * @return the newly created item
     */
    private static @Nullable GenericItem createItem(String itemType, String itemname, Set<ItemFactory> itemFactories) {
        GenericItem newItem = null;
        for (ItemFactory itemFactory : itemFactories) {
            newItem = itemFactory.createItem(itemType, itemname);
            if (newItem != null) {
                break;
            }
        }
        return newItem;
    }

}
