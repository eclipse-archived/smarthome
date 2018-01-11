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
package org.eclipse.smarthome.config.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface marking the service with service.pid as one that can be instantiated multiple times with different
 * configurations
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public interface MultipleInstanceServiceInfo {

    /**
     * Get the human readable label of the service
     *
     * @return label of the service
     */
    String getLabel();

    /**
     * Obtain the service category
     *
     * @return Category this service is in
     */
    String getCategory();

    /**
     * Get the service.pid of the service that can be created multiple times with different configurations
     *
     * @return service.pid of the service
     */
    String getServicePID();

    /**
     * Get the description URI of this service
     *
     * @return the config description URI of this service
     */
    String getConfigDescriptionUri();

}
