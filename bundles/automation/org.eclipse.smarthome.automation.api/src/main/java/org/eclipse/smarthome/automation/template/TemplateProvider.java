/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.template;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * This interface has to be implemented by all providers of {@link Template}s.
 * The {@link TemplateRegistry} uses it to get access to available {@link Template} s.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Ana Dimova - add registration property - rule.templates
 */
public interface TemplateProvider<E extends Template> extends Provider<E> {

    /**
     * This method is used to get localized Template. When the localization is not
     * specified or it is not supported a Template with default locale is
     * returned.
     *
     * @param UID    unique id of Template.
     * @param locale defines localization of label and description of the {@link Template} or null.
     * @return localized Template.
     */
    E getTemplate(String UID, Locale locale);

    /**
     * This method is used to get localized Templates defined by this provider.
     * When localization is not specified or it is not supported a Templates with
     * default localization is returned.
     *
     * @param locale defines localization of label and description of the {@link Template}s or null.
     * @return collection of localized {@link Template} provided by this provider
     */
    Collection<E> getTemplates(Locale locale);

}
