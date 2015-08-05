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

package org.eclipse.smarthome.automation.commands;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This class serves to provide runtime localization for the automation objects. They are kept localized in the memory.
 * This provides opportunity for high performance at runtime.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class Localizer {

    /**
     * This static field is a default locale language which is used for initial creation of {@code Localizer} objects.
     */
    private static String DEFAULT = Locale.ENGLISH.getDisplayLanguage();

    /**
     * This field is a runtime holder of localizations of one particular {@link ModuleType} or {@link Template}. It is a
     * map that has for keys - the {@code DisplayLanguage} and for values - the localized object, corresponding to the
     * key.
     */
    private Map<String, Object> localesProvider = new HashMap<String, Object>();

    /**
     * This constructor creates an object per each {@link ModuleType} or {@link Template}. This object is responsible
     * for localization of the provided object. Initially, it creates a default locale - {@link Locale.ENGLISH} and put
     * it to the {@link #localesProvider} for fast access on demand.
     * 
     * @param providedObject can be {@link ModuleType} or {@link Template}. It is the subject to localize.
     */
    public Localizer(Object providedObject) {
        localesProvider.put(DEFAULT, providedObject);
    }

    /**
     * This method adds a new locale language for a {@link ModuleType} or {@link Template}, to the
     * {@link #localesProvider}.
     * 
     * @param language is the {@code DisplayLanguage} of the {@link ModuleType} or {@link Template}.
     * @param providedObject is the localized object of the {@link ModuleType} or {@link Template}.
     */
    public void addLanguage(String language, Object providedObject) {
        localesProvider.put(language, providedObject);
    }

    /**
     * This method returns the set of all requested locales of one particular {@link ModuleType} or {@link Template},
     * during lifecycle of the system.
     * 
     * @return a set of all requested locales of one particular {@link ModuleType} or {@link Template}.
     */
    public Set<String> getAvailableLanguages() {
        return localesProvider.keySet();
    }

    /**
     * This method returns the localized object of one particular {@link ModuleType} or {@link Template}, corresponding
     * to the requested locale.
     * 
     * @param locale is the requested locale.
     * @return the requested localized object of one particular {@link ModuleType} or {@link Template}.
     */
    public Object getPerLocale(Locale locale) {
        if (locale == null) {
            return localesProvider.get(DEFAULT);
        }
        return getPerLanguage(locale.getDisplayLanguage());
    }

    /**
     * This method returns the localized object of one particular {@link ModuleType} or {@link Template}, corresponding
     * to the requested locale language.
     * 
     * @param language is the requested locale language.
     * @return the requested localized object of one particular {@link ModuleType} or {@link Template}.
     */
    public Object getPerLanguage(String language) {
        if (language == null) {
            return localesProvider.get(DEFAULT);
        }
        Object obj = localesProvider.get(language);
        return obj == null ? localize(language) : obj;
    }

    /**
     * This method serves to trigger the mechanism of localization, which the system has. if requested language is
     * supported by the system, will be triggered localization of the object and the result of it will be returned.
     * Otherwise, the default localized object will be returned.
     * 
     * @param language is the requested locale language.
     * @return the localization of the {@link ModuleType} or {@link Template}, for the requested language, if it is
     *         supported by the system. Otherwise, the default localized object will be returned.
     */
    private Object localize(String language) {
        // TODO Its functionality will be extended when the mechanism for localization is defined. For now, the method
        // returns the default localization.
        return localesProvider.get(DEFAULT);
    }

}
