/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.util.Map;

import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;

/**
 * This is a helper data structure for GSON that represents the JSON format used when having different module types
 * within a single input stream.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ModuleTypeParsingContainer {

    public Map<String, CompositeTriggerType> triggers;

    public Map<String, CompositeConditionType> conditions;

    public Map<String, CompositeActionType> actions;
}
