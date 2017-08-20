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
import org.eclipse.smarthome.automation.template.RuleTemplateProvider;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This class is implementation of {@link TemplateProvider}. It extends functionality of {@link AbstractFileProvider}
 * for importing the {@link RuleTemplate}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class TemplateFileProvider extends AbstractFileProvider<RuleTemplate> implements RuleTemplateProvider {

    protected TemplateRegistry<RuleTemplate> templateRegistry;

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

    @Override
    public RuleTemplate getTemplate(String UID, Locale locale) {
        return providedObjectsHolder.get(UID);
    }

    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        Collection<RuleTemplate> values = providedObjectsHolder.values();
        if (values.isEmpty()) {
            return Collections.<RuleTemplate> emptyList();
        }
        return new LinkedList<RuleTemplate>(values);
    }

    protected void setTemplateRegistry(TemplateRegistry<RuleTemplate> templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    protected void removeTemplateRegistry(TemplateRegistry<RuleTemplate> templateRegistry) {
        this.templateRegistry = null;
    }

    /**
     * This method is responsible for checking the existence of {@link ModuleType}s or {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link Template}, which to be checked.
     * @return {@code true} if {@link Template} with the same UID exists or {@code false} in the opposite
     *         case.
     */
    @Override
    protected boolean checkExistence(String uid) {
        if (templateRegistry != null && templateRegistry.get(uid) != null) {
            return true;
        }
        return false;
    }

}
