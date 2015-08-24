/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.BaseConditionHandler;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConditionHandler implementation to check item state
 * 
 * @author Benedikt Niehues
 *
 */
public class ItemStateConditionHandler extends BaseConditionHandler {

	private final Logger logger = LoggerFactory.getLogger(ItemStateConditionHandler.class);

	public static final String ITEM_STATE_CONDITION = "ItemStateCondition";

	private ItemRegistry itemRegistry;

	/**
	 * Constants for Config-Parameters corresponding to Definition in
	 * ItemModuleTypeDefinition.json
	 */
	private static final String ITEM_NAME = "itemName";
	private static final String OPERATOR = "operator";
	private static final String STATE = "state";

	public ItemStateConditionHandler(Condition condition, List<ModuleType> moduleTypes) {
		super(condition, moduleTypes);
	}

	/**
	 * setter for itemRegistry, used by DS
	 * 
	 * @param itemRegistry
	 */
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	/**
	 * unsetter for itemRegistry used by DS
	 * 
	 * @param itemRegistry
	 */
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}

	@Override
	public void dispose() {
		itemRegistry = null;
	}

	@Override
	protected boolean evaluateCondition(Map<String, Object> resolvedInputs, Map<String, Object> resolvedConfiguration) {
		String itemName = (String) resolvedConfiguration.get(ITEM_NAME);
		String state = (String) resolvedConfiguration.get(STATE);
		String operator = (String) resolvedConfiguration.get(OPERATOR);
		if (operator == null || state == null || itemName == null) {
			logger.error("Module is not well configured: itemName={}  operator={}  state = {}", itemName, operator,
					state);
			return false;
		}
		if (itemRegistry == null){
			logger.error("The ItemRegistry is not available to evaluate the condition.");
			return false;
		}
		try {
			Item item = itemRegistry.getItem(itemName);
			State compareState = TypeParser.parseState(item.getAcceptedDataTypes(), state);
			State itemState = item.getState();
			logger.debug("checking if {} (State={}) {} {}", itemName, itemState, operator, compareState);
			switch (operator) {
			case "=":
				logger.debug("ConditionSatisfied --> " + itemState.equals(compareState));
				return itemState.equals(compareState);
			case "!=":
				return !itemState.equals(compareState);
			case "<":
				if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
					return ((DecimalType) itemState).compareTo((DecimalType) compareState) < 0 ? true : false;
				}
				break;
			case ">":
				if (itemState instanceof DecimalType && compareState instanceof DecimalType) {
					return ((DecimalType) itemState).compareTo((DecimalType) compareState) > 0 ? true : false;
				}
				break;
			default:
				break;
			}
		} catch (ItemNotFoundException e) {
			logger.error("Item with Name " + itemName + " not found in itemRegistry");
			return false;
		}
		return false;
	}

}
