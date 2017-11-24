/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import org.eclipse.smarthome.core.items.ActiveItem;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
    public static ActiveItem map(ItemDTO itemDTO, Set<ItemFactory> itemFactories) {
        Preconditions.checkArgument(itemDTO != null, "The argument 'itemDTO' must no be null.");
        Preconditions.checkArgument(itemFactories != null, "The argument 'itemFactories' must no be null.");

        GenericItem newItem = null;
        if (itemDTO.type != null) {
            if (itemDTO instanceof GroupItemDTO && itemDTO.type.equals(GroupItem.TYPE)) {
                GroupItemDTO groupItemDTO = (GroupItemDTO) itemDTO;
                GenericItem baseItem = null;
                if (!Strings.isNullOrEmpty(groupItemDTO.groupType)) {
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

    public static GroupFunction mapFunction(Item baseItem, GroupFunctionDTO function) {
        List<State> args = new ArrayList<State>();

        GroupFunction groupFunction = null;
        switch (function.name.toUpperCase()) {
            case "AND":
                args = parseStates(baseItem, function.params);
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.And(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "OR":
                args = parseStates(baseItem, function.params);
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.Or(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "NAND":
                args = parseStates(baseItem, function.params);
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.NAnd(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'NOT AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "NOR":
                args = parseStates(baseItem, function.params);
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.NOr(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'NOT OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "COUNT":
                if (function.params != null && function.params.length == 1) {
                    State countParam = new StringType(function.params[0]);
                    groupFunction = new ArithmeticGroupFunction.Count(countParam);
                } else {
                    LoggerFactory.getLogger(ItemDTOMapper.class)
                            .error("Group function 'COUNT' requires one argument. Using Equality instead.");
                }
                break;
            case "AVG":
                groupFunction = new ArithmeticGroupFunction.Avg();
                break;
            case "SUM":
                groupFunction = new ArithmeticGroupFunction.Sum();
                break;
            case "MIN":
                groupFunction = new ArithmeticGroupFunction.Min();
                break;
            case "MAX":
                groupFunction = new ArithmeticGroupFunction.Max();
                break;
            case "EQUAL":
                groupFunction = new GroupFunction.Equality();
                break;
            default:
                LoggerFactory.getLogger(ItemDTOMapper.class)
                        .error("Unknown group function '{}'. Using Equality instead.", function.name);
        }

        if (groupFunction == null) {
            groupFunction = new GroupFunction.Equality();
        }

        return groupFunction;
    }

    private static List<State> parseStates(Item baseItem, String[] params) {
        List<State> states = new ArrayList<State>();

        if (params == null) {
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

    public static GroupFunctionDTO mapFunction(GroupFunction function) {
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
    private static GenericItem createItem(String itemType, String itemname, Set<ItemFactory> itemFactories) {
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
