/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.dto.RuleDTO;
import org.eclipse.smarthome.automation.dto.RuleDTOMapper;
import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;

/**
 * Implementation of a rule provider that uses the storage service for persistence
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Persistence implementation
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class ManagedRuleProvider extends AbstractManagedProvider<Rule, String, RuleDTO> implements RuleProvider {

    @Override
    protected String getKey(Rule element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "automation_rules";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected Rule toElement(String key, RuleDTO persistableElement) {
        return RuleDTOMapper.map(persistableElement);
    }

    @Override
    protected RuleDTO toPersistableElement(Rule element) {
        return RuleDTOMapper.map(element);
    }

}
