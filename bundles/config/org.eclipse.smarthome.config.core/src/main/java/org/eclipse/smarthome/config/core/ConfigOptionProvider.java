/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;

/**
 * The {@link ConfigOptionProvider} can be implemented and registered as an <i>OSGi</i>
 * service to provide {@link ConfigDescription}s options.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - added support for contexts
 */
public interface ConfigOptionProvider {

    /**
     * Provides a collection of {@link ParameterOptions}s.
     *
     * @deprecated Use {@link getParameterOptions} with context instead.
     *
     * @param uri the uri of the config description
     * @param param the parameter name for which the requested options shall be returned
     * @param locale the locale in which the result is expected
     * @return the configuration options provided by this provider if any or {@code null} otherwise
     */
    @Deprecated
    Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale);

    /**
     * Provides a collection of {@link ParameterOptions}s.
     *
     * @param uri the uri of the config description
     * @param param the parameter name for which the requested options shall be returned
     * @param context the defined context of the parameter
     * @param locale the locale in which the result is expected
     * @return the configuration options provided by this provider if any or {@code null} otherwise
     */
    default Collection<ParameterOption> getParameterOptions(URI uri, String param, String context, Locale locale) {
        return getParameterOptions(uri, param, locale);
    }
}
