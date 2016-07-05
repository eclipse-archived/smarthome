/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.rest.internal.dto;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatusInfo;

/**
 * This is a data transfer object that is used to serialize rules with dynamic data like the status.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class EnrichedRuleDTO extends RuleDTO {

    public boolean enabled;
    public RuleStatusInfo status;

    public EnrichedRuleDTO(final Rule rule, final RuleRegistry ruleRegistry) {
        super(rule);
        enabled = ruleRegistry.isEnabled(rule.getUID());
        status = ruleRegistry.getStatus(rule.getUID());
    }
}
