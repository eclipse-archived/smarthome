/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class is a wrapper of multiple {@link TemplateProvider}s, responsible for importing the {@link Template}s from
 * local file system.
 * <p>
 * It provides functionality for tracking {@link Parser} services and provides common functionality for notifying the
 * {@link ProviderChangeListener}s for adding, updating and removing the {@link Template}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class MultipleTemplateFileProvider extends AbstractMultipleFileProvider<RuleTemplate, TemplateFileProvider>
        implements TemplateProvider {

    @Override
    protected void modified(Map<String, Object> config) {
        String roots = (String) config.get(ROOTS);
        if (roots != null) {
            this.roots = roots.split(",");
            for (String root : providers.keySet()) {
                if (!roots.contains(root)) {
                    TemplateFileProvider provider = providers.remove(root);
                    provider.deactivate();
                }
            }
        }
        for (int i = 0; i < this.roots.length; i++) {
            if (!providers.containsKey(this.roots[i])) {
                initializeProvider(this.roots[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> T getTemplate(String UID, Locale locale) {
        T template = null;
        for (TemplateFileProvider provider : providers.values()) {
            template = (T) provider.getTemplate(UID, locale);
            if (template != null) {
                return template;
            }
        }
        return template;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> Collection<T> getTemplates(Locale locale) {
        Collection<T> templates = new ArrayList<T>();
        for (TemplateFileProvider provider : providers.values()) {
            templates.addAll((Collection<T>) provider.getTemplates(locale));
        }
        return templates;
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return getTemplates(null);
    }

    private void initializeProvider(String root) {
        TemplateFileProvider provider = new TemplateFileProvider(root);
        provider.addProviderChangeListener(this);
        provider.activate(parsers);
        providers.put(root, provider);
    }

}
