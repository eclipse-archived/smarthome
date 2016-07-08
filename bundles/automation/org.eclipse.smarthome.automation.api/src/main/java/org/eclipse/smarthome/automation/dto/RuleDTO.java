/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * This is a data transfer object that is used to serialize rules.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class RuleDTO {

    public List<TriggerDTO> triggers;
    public List<ConditionDTO> conditions;
    public List<ActionDTO> actions;
    public Map<String, Object> configuration;
    public List<ConfigDescriptionParameter> configDescriptions;
    public String templateUID;
    public String uid;
    public String name;
    public Set<String> tags;
    public Visibility visibility;
    public String description;

}
