/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.integration.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class is a {@link TemplateProvider} test implementation.
 *
 * @author Ana Dimova
 *
 */
public class TestTemplateProvider implements Provider<RuleTemplate>, TemplateProvider {

    private RuleTemplate[] ruleTemplates;

    public TestTemplateProvider(RuleTemplate[] ruleTemplates) {
        super();
        this.ruleTemplates = ruleTemplates;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> T getTemplate(String UID, Locale locale) {
        for (RuleTemplate template : ruleTemplates) {
            if (template.getUID().equals(UID)) {
                return (T) template;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> Collection<T> getTemplates(Locale locale) {
        return (Collection<T>) getAll();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
        for (RuleTemplate template : ruleTemplates) {
            listener.added(this, template);
        }
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return Arrays.asList(ruleTemplates);
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {

    }

}
