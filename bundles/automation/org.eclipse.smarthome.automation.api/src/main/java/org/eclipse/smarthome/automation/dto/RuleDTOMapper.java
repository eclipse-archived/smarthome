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
package org.eclipse.smarthome.automation.dto;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTOMapper;

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
                ConfigDescriptionDTOMapper.map(ruleDto.configDescriptions), new Configuration(ruleDto.configuration),
                ruleDto.templateUID, ruleDto.visibility);
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
        to.configDescriptions = ConfigDescriptionDTOMapper.mapParameters(from.getConfigurationDescriptions());
        to.templateUID = from.getTemplateUID();
        to.uid = from.getUID();
        to.name = from.getName();
        to.tags = from.getTags();
        to.visibility = from.getVisibility();
        to.description = from.getDescription();
    }

}
