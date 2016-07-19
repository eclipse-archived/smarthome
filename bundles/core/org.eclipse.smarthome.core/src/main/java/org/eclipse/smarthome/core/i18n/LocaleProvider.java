/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.i18n;

import java.util.Locale;

/**
 * The interface describe a provider for a locale.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public interface LocaleProvider {

    /**
     * Get a locale.
     *
     * The locale could be used e.g. as a fallback if there is no other one defined explicitly.
     *
     * @return a locale (non-null)
     */
    Locale getLocale();

}
