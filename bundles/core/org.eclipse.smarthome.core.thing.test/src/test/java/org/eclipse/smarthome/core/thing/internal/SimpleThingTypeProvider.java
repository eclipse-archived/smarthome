/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;

public class SimpleThingTypeProvider implements ThingTypeProvider {
    private final Collection<ThingType> thingTypes;

    SimpleThingTypeProvider(final Collection<ThingType> thingTypes) {
        this.thingTypes = thingTypes;
    }

    @Override
    public Collection<ThingType> getThingTypes(final Locale locale) {
        return this.thingTypes;
    }

    @Override
    public ThingType getThingType(final ThingTypeUID thingTypeUID, final Locale locale) {
        for (final ThingType thingType : thingTypes) {
            if (thingType.getUID().equals(thingTypeUID)) {
                return thingType;
            }
        }
        return null;
    }
}
