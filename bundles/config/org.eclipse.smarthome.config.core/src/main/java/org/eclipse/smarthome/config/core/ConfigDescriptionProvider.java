/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
 * The {@link ConfigDescriptionProvider} can be implemented and registered as an <i>OSGi</i>
 * service to provide {@link ConfigDescription}s. The {@link ConfigDescriptionRegistry} tracks
 * each {@link ConfigDescriptionProvider} and registers a {@link ConfigDescriptionsChangeListener} on each provider.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Michael Grammling - Initial contribution
 */
public interface ConfigDescriptionProvider {

    /**
     * Provides a collection of {@link ConfigDescription}s.
     *
     * @param locale
     *            locale
     * @return the configuration descriptions provided by this provider (not
     *         null, could be empty)
     */
    Collection<ConfigDescription> getConfigDescriptions(Locale locale);

    /**
     * Provides a {@link ConfigDescription} for the given URI.
     *
     * @param uri
     *            uri of the config description
     * @param locale
     *            locale
     *
     * @return config description or null if no config description could be
     *         found
     */
    ConfigDescription getConfigDescription(URI uri, Locale locale);

}
