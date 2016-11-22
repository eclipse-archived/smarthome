/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;

/**
 * This class is implementation of {@link TemplateProvider}. It extends functionality of {@link AbstractFileProvider}
 * for importing the {@link RuleTemplate}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class TemplateFileProvider extends AbstractFileProvider<RuleTemplate> implements TemplateProvider {

    public TemplateFileProvider() {
        super("templates");
    }

    @Override
    protected String getUID(RuleTemplate providedObject) {
        return providedObject.getUID();
    }

    @Override
    public Collection<RuleTemplate> getAll() {
        return getTemplates(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> T getTemplate(String UID, Locale locale) {
        return (T) providedObjectsHolder.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Template> Collection<T> getTemplates(Locale locale) {
        Collection<RuleTemplate> values = providedObjectsHolder.values();
        if (values.isEmpty()) {
            return Collections.<T>emptyList();
        }
        return (Collection<T>) new LinkedList<RuleTemplate>(values);
    }

}
