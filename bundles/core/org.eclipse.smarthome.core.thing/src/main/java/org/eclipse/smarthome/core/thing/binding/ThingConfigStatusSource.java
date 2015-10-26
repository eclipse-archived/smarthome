/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.config.core.status.ConfigStatusSource;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * An implementation of {@link ConfigStatusSource} for the {@link Thing} entity.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ThingConfigStatusSource extends ConfigStatusSource {

    private static final String TOPIC = "smarthome/things/{thingUID}/config/status";

    /**
     * Creates a new {@link ThingConfigStatusSource} for the given thing UID.
     *
     * @param thingUID the UID of the thing
     */
    public ThingConfigStatusSource(String thingUID) {
        super(thingUID);
    }

    @Override
    public String getTopic() {
        return TOPIC.replace("{thingUID}", this.entityId);
    }
}
