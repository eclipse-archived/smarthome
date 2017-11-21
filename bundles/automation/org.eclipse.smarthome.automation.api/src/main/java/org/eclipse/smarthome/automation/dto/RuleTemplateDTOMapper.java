/**
* Copyright (c) 2015, 2017 by Bosch Software Innovations and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/
package org.eclipse.smarthome.automation.dto;

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionDTOMapper;

/**
 * This is a utility class to convert between the Rule Templates and RuleTemplateDTO objects.
 *
 * @author Ana Dimova - Initial contribution
 *
 */
public class RuleTemplateDTOMapper {
    public static RuleTemplateDTO map(final RuleTemplate template) {
        final RuleTemplateDTO templateDTO = new RuleTemplateDTO();
        fillProperties(template, templateDTO);
        return templateDTO;
    }

    protected static void fillProperties(final RuleTemplate from, final RuleTemplateDTO to) {
        to.label = from.getLabel();
        to.uid = from.getUID();
        to.tags = from.getTags();
        to.description = from.getDescription();
        to.visibility = from.getVisibility();
        to.configDescriptions = ConfigDescriptionDTOMapper.mapParameters(from.getConfigurationDescriptions());
        to.triggers = TriggerDTOMapper.map(from.getTriggers());
        to.conditions = ConditionDTOMapper.map(from.getConditions());
        to.actions = ActionDTOMapper.map(from.getActions());
    }
}
