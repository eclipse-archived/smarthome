/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.template;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateRegistryImpl implements TemplateRegistry {

    private TemplateManager templateManager;

    public TemplateRegistryImpl(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public <T extends Template> T get(String key) {
        return templateManager.getTemplate(key);
    }

    @Override
    public <T extends Template> T get(String uid, Locale locale) {
        return templateManager.getTemplate(uid, locale);
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag) {
        return getByTag(tag, null);
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag, Locale locale) {
        return templateManager.getTemplatesByTag(tag, locale);
    }

    @Override
    public <T extends Template> Collection<T> getAll() {
        return getAll(null);
    }

    @Override
    public <T extends Template> Collection<T> getAll(Locale locale) {
        return templateManager.getTemplates(locale);
    }

    public void dispose() {
        templateManager.dispose();
    }

}
