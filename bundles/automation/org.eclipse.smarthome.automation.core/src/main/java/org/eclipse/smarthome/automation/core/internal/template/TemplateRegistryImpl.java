/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal.template;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;

/**
 * The implementation of {@link TemplateRegistry} that is registered as a service.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateRegistryImpl implements TemplateRegistry {

    private TemplateManager templateManager;

    public TemplateRegistryImpl(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public <T extends Template> T get(String key) {
        return templateManager.get(key);
    }

    @Override
    public <T extends Template> T get(String uid, Locale locale) {
        return templateManager.get(uid, locale);
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag) {
        return getByTag(tag, null);
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag, Locale locale) {
        return templateManager.getByTag(tag, locale);
    }

    @Override
    public <T extends Template> Collection<T> getByTags(String... tags) {
        return getByTags(null, tags);
    }

    @Override
    public <T extends Template> Collection<T> getByTags(Locale locale, String... tags) {
        Set<String> tagSet = tags != null ? new HashSet<String>(Arrays.asList(tags)) : null;
        return templateManager.getByTags(tagSet, locale);
    }

    @Override
    public <T extends Template> Collection<T> getAll() {
        return getAll(null);
    }

    @Override
    public <T extends Template> Collection<T> getAll(Locale locale) {
        return templateManager.getAll(locale);
    }

    public void dispose() {
        templateManager.dispose();
    }

}
