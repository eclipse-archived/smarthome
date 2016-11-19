/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.TemplateProvider;

/**
 * This class is implementation of {@link TemplateProvider}. It extends functionality of {@link AbstractFileProvider}
 * for importing the {@link RuleTemplate}s from local files.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TemplateFileProvider extends AbstractFileProvider<RuleTemplate> implements TemplateProvider {

    private final String templatesRoot = "templates";

    public TemplateFileProvider(String root) {
        WatchingDir = root + File.separator + templatesRoot;
        super.activate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RuleTemplate getTemplate(String UID, Locale locale) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.get(UID);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        synchronized (providedObjectsHolder) {
            return !providedObjectsHolder.isEmpty() ? providedObjectsHolder.values()
                    : Collections.<RuleTemplate>emptyList();
        }
    }

    @Override
    protected String getUID(RuleTemplate providedObject) {
        return providedObject.getUID();
    }

}
