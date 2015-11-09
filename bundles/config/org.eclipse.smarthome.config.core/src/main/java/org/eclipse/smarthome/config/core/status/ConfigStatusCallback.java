/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

/**
 * The {@link ConfigStatusCallback} interface is a callback interface to propagate a new configuration status for an
 * entity.
 *
 * @author Thomas Höfer - Initial contribution
 */
public interface ConfigStatusCallback {

    /**
     * Based on the given {@link ConfigStatusSource} this operation propagates a new configuration status for an entity
     * after its configuration has been updated.
     *
     * @param configStatusSource the source of the configuration status
     */
    void configUpdated(ConfigStatusSource configStatusSource);

}
