/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link ConfigStatusSource} represents a source which would like to propagate its new configuration status. It is
 * used as input for {@link ConfigStatusCallback#configUpdated(ConfigStatusSource)}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public abstract class ConfigStatusSource {

    /** The id of the entity whose new configuration status is to be propagated. */
    public final String entityId;

    /**
     * Creates a new config status source object.
     *
     * @param entityId the id of the entity whose new configuration status is to be propagated
     */
    public ConfigStatusSource(@NonNull String entityId) {
        super();
        this.entityId = entityId;
    }

    /**
     * @return the topic over which the new configuration status is to be propagated
     */
    public abstract String getTopic();

}
