/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.dto.RuleDTOMapper;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class EnrichedRuleDTOMapper extends RuleDTOMapper {

    public static EnrichedRuleDTO map(final Rule rule, final RuleRegistry ruleRegistry) {
        final EnrichedRuleDTO enrichedRuleDto = new EnrichedRuleDTO();
        fillProperties(rule, enrichedRuleDto);
        enrichedRuleDto.enabled = ruleRegistry.isEnabled(rule.getUID());
        enrichedRuleDto.status = ruleRegistry.getStatusInfo(rule.getUID());
        return enrichedRuleDto;
    }

}
