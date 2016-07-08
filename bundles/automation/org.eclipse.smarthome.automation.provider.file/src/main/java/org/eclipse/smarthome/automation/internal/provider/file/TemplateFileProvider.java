/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final String TEMPLATES_ROOT = "automation/template";

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
    public void activate() {
        super.activate();
        importResources(new File(TEMPLATES_ROOT));
    }

    @Override
    protected void updateProvidedObjectsHolder(URL url, Set<RuleTemplate> providedObjects) {
        if (providedObjects != null && !providedObjects.isEmpty()) {
            List<String> uids = new ArrayList<String>();
            for (RuleTemplate ruleT : providedObjects) {
                String uid = ruleT.getUID();
                RuleTemplate oldRuleT = getOldElement(uid);
                notifyListeners(oldRuleT, ruleT);
                uids.add(uid);
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, ruleT);
                }
            }
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, uids);
            }
        }
    }

    @Override
    protected String getSourcePath() {
        return TEMPLATES_ROOT;
    }

    @Override
    protected void removeElements(List<String> objectsForRemove) {
        for (String removededObject : objectsForRemove) {
            RuleTemplate rtRemoved;
            synchronized (providedObjectsHolder) {
                rtRemoved = providedObjectsHolder.remove(removededObject);
            }
            notifyListeners(rtRemoved);
        }
    }

}
