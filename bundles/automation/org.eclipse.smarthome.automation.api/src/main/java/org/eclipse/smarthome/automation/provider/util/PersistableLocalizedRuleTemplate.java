/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.automation.template.RuleTemplate;

public class PersistableLocalizedRuleTemplate {

    public String vendorId;
    public String vendorVersion;
    public String url;

    public Set<String> languages;

    public Set<PersistableRuleTemplate> localizedTemplates = new HashSet<PersistableRuleTemplate>();

    /**
     * This constructor is used for deserialization of the localized {@link RuleTemplate}s.
     */
    public PersistableLocalizedRuleTemplate() {
    }
}
