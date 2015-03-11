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
 * This interface has to be implemented by all providers of {@link Template}s.
 * The {@link TemplateRegistry} uses it to get access to available {@link Template} s.
 *
 * @author Yordan Mihaylov
 */
public interface TemplateProvider {

    /**
     * This method is used to get localized Template. When the localization is not
     * specified or it is not supported a Template with default locale is
     * returned.
     *
     * @param UID unique id of Template.
     * @param locale defines localization of label and description of the {@link Template} or null.
     * @return localized Template.
     */
    Template getTemplate(String UID, Locale locale);

    /**
     * This method is used to get localized Templates defined by this provider.
     * When localization is not specified or it is not supported a Templates with
     * default localization is returned.
     *
     * @param locale defines localization of label and description of the {@link Template}s or null.
     * @return collection of localized {@link Template} provided by this provider
     */
    Collection<Template> getTemplates(Locale locale);

}
