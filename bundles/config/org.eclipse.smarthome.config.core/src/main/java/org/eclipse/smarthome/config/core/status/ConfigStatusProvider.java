/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import java.util.Locale;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link ConfigStatusProvider} can be implemented and registered as an <i>OSGi</i> service to provide status
 * information for {@link Configuration}s of entities. The {@link ConfigStatusService} tracks each
 * {@link ConfigStatusProvider} and provides the corresponding {@link ConfigStatusInfo} by the operation
 * {@link ConfigStatusService#getConfigStatus(String, Locale)}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public interface ConfigStatusProvider {

    /**
     * Retrieves the configuration status information for the {@link Configuration} of the entity if this configuration
     * status provider supports the entity.
     *
     * @param locale the locale to be used for the corresponding internationalized configuration status messages
     *
     * @return the requested configuration status information
     */
    ConfigStatusInfo getConfigStatus(Locale locale);

    /**
     * Determines if the {@link ConfigStatusProvider} instance can provide the configuration status information for the
     * given entity.
     *
     * @param entityId the id of the entity whose configuration status information is to be provided
     *
     * @return true, if the {@link ConfigStatusProvider} instance supports the given entity, otherwise false
     */
    boolean supportsEntity(String entityId);

    /**
     * Sets the given {@link ConfigStatusCallback} for the {@link ConfigStatusProvider}.
     *
     * @param configStatusCallback the configuration status callback to be set
     */
    void setConfigStatusCallback(ConfigStatusCallback configStatusCallback);

}
