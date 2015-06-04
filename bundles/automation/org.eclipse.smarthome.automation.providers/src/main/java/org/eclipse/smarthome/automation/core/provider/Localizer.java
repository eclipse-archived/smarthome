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

package org.eclipse.smarthome.automation.core.provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Localizer {

    private static Locale DEFAULT = Locale.getDefault();
    private Map<Locale, Object> localeProvider = new HashMap<Locale, Object>();

    /**
     * @param providedObject
     */
    public Localizer(Object providedObject) {
        localeProvider.put(DEFAULT, providedObject);
    }

    public Object localize(Locale locale) {
        if (locale == null) {
            return localeProvider.get(DEFAULT);
        }
        return localeProvider.get(locale) == null ? localize(locale, localeProvider.get(DEFAULT)) : localeProvider
                .get(locale);
    }

    private Object localize(Locale locale, Object providedObject) {
        // TODO
        return providedObject;
    }

}
