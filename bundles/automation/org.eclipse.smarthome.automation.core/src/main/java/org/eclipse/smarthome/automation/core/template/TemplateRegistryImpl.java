/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core.template;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TemplateRegistryImpl extends AbstractRegistry<Template, String> implements TemplateRegistry {

    private TemplateManager templateManager;

    public TemplateRegistryImpl(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.core.common.registry.Registry#get(java.lang.Object)
     */
    @Override
    public Template get(String key) {
        return templateManager.getTemplate(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.template.TemplateRegistry#get(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends Template> T get(String uid, Locale locale) {
        return (T) templateManager.getTemplate(uid, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.template.TemplateRegistry#getByTag(java.lang.String, java.util.Locale)
     */
    @Override
    public <T extends Template> Collection<T> getByTag(String tag, Locale locale) {
        return (Collection<T>) templateManager.getTemplatesByTag(tag, locale);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.automation.template.TemplateRegistry#get(java.lang.Class, java.util.Locale)
     */
    @Override
    public <T extends Template> Collection<T> getAll(Locale locale) {
        return (Collection<T>) templateManager.getTemplates(locale);
    }

    /**
   *
   */
    public void dispose() {
        templateManager.dispose();
    }

    @Override
    public <T extends Template> Collection<T> getByTag(String tag) {
        return getByTag(tag, null);
    }

}
