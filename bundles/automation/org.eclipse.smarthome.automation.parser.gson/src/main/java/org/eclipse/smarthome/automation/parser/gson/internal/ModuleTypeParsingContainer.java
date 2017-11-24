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
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.util.List;

import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;

/**
 * This is a helper data structure for GSON that represents the JSON format used when having different module types
 * within a single input stream.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class ModuleTypeParsingContainer {

    public List<CompositeTriggerType> triggers;

    public List<CompositeConditionType> conditions;

    public List<CompositeActionType> actions;
}
