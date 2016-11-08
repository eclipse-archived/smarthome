/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.template;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.common.registry.Registry;

/**
 * This interface provides functionality to get available {@link Template}s. The {@link Template} can be returned
 * localized depending on locale parameter.
 * When the parameter is not specified or there is no such localization
 * resources the returned template is localized with default locale.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 * @param <T>
 */
public interface TemplateRegistry<E extends Template> extends Registry<E, String> {

    /**
     * This method is used to get template of specified by type.
     *
     * @param uid the an unique id in scope of registered templates
     * @param locale user locale
     * @return template instance or null.
     */
    public E get(String uid, Locale locale);

    /**
     * This method is used for getting the templates filtered by tag.
     *
     * @param tag specifies the filter for getting the templates, if it is <code>null</code> then returns all templates.
     * @return the templates, which correspond to the specified filter.
     */
    public Collection<E> getByTag(String tag);

    /**
     * This method is used for getting the templates filtered by tag.
     *
     * @param tag specifies the filter for getting the templates, if it is <code>null</code> then returns all templates.
     * @return the templates, which correspond to the specified filter.
     */
    public Collection<E> getByTag(String tag, Locale locale);

    /**
     * This method is used for getting the templates filtered by tags.
     *
     * @param tags set of tags which specifies the filter for getting the templates, if it is <code>null</code> then
     *            returns all templates.
     * @return collection of templates, which correspond to the filter.
     */
    public Collection<E> getByTags(String... tags);

    /**
     * This method is used for getting the templates filtered by tags.
     *
     * @param tags set of tags which specifies the filter for getting the templates, if it is <code>null</code> then
     *            returns all templates.
     * @return the templates, which correspond to the the filter.
     */
    public Collection<E> getByTags(Locale locale, String... tags);

    /**
     * This method is used for getting all templates, localized by specified locale,
     *
     * @param moduleType the class of module which is looking for.
     * @return collection of templates, corresponding to specified type
     */
    public Collection<E> getAll(Locale locale);

}
