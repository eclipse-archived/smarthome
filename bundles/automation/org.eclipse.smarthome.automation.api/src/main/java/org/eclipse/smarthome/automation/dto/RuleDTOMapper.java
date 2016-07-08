/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class RuleDTOMapper {

    public static RuleDTO map(final Rule rule) {
        final RuleDTO ruleDto = new RuleDTO();
        fillProperties(rule, ruleDto);
        return ruleDto;
    }

    public static Rule map(final RuleDTO ruleDto) {
        final Rule rule = new Rule(ruleDto.uid, TriggerDTOMapper.mapDto(ruleDto.triggers),
                ConditionDTOMapper.mapDto(ruleDto.conditions), ActionDTOMapper.mapDto(ruleDto.actions),
                ruleDto.configDescriptions, new Configuration(ruleDto.configuration), ruleDto.templateUID,
                ruleDto.visibility);
        rule.setTags(ruleDto.tags);
        rule.setName(ruleDto.name);
        rule.setDescription(ruleDto.description);
        return rule;
    }

    protected static void fillProperties(final Rule from, final RuleDTO to) {
        to.triggers = TriggerDTOMapper.map(from.getTriggers());
        to.conditions = ConditionDTOMapper.map(from.getConditions());
        to.actions = ActionDTOMapper.map(from.getActions());
        to.configuration = from.getConfiguration().getProperties();
        to.configDescriptions = from.getConfigurationDescriptions();
        to.templateUID = from.getTemplateUID();
        to.uid = from.getUID();
        to.name = from.getName();
        to.tags = from.getTags();
        to.visibility = from.getVisibility();
        to.description = from.getDescription();
    }

}
