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

package org.eclipse.smarthome.automation.template;

import java.util.Collection;
import java.util.Locale;

/**
 * This interface provides functionality to get available {@link Template}s. The {@link Template} can be returned
 * localized depending on locale parameter.
 * When the parameter is not specified or there is no such localization
 * resources the returned template is localized with default locale.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface TemplateRegistry {

    /**
     * This method is used to get template of specified by type.
     *
     * @param uid the an unique id in scope of registered templates
     * @param locale user locale
     * @return template instance or null.
     */
    public <T extends Template> T get(String uid, Locale locale);

    /**
     * This method is used for getting the templates filtered by tag.
     *
     * @param tag specifies the filter for getting the templates, if it is <code>null</code> then returns all templates.
     * @return the templates, which correspond to the specified filter.
     */
    public <T extends Template> Collection<T> getByTag(String tag, Locale locale);

    /**
     * This method is used for getting the templates, specified by type module,
     * i.e. {@link ActionTemplate}, {@link ConditionTemplate}, {@link TriggerTemplate} and etc.
     *
     * @param moduleType the class of module which is looking for.
     * @return collection of templates, corresponding to specified type
     */
    public <T extends Template> Collection<T> get(Class<T> moduleType, Locale locale);

}
