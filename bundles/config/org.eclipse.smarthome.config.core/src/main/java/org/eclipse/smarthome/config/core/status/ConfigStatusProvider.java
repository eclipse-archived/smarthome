/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import java.util.Collection;
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
     * Returns the configuration status in form of a collection of {@link ConfigStatusMessage}s for the
     * {@link Configuration} of the entity that is supported by this {@link ConfigStatusProvider}.
     *
     * @return the requested configuration status (not null)
     */
    Collection<ConfigStatusMessage> getConfigStatus();

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
