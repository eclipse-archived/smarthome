/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;

public class ThingBuilder extends GenericThingBuilder<ThingBuilder> {

    private ThingBuilder(ThingImpl thing) {
        super(thing);
    }

    public static ThingBuilder create(ThingTypeUID thingTypeUID, String thingId) {
        ThingImpl thing = new ThingImpl(thingTypeUID, thingId);
        return new ThingBuilder(thing);
    }

    public static ThingBuilder create(ThingUID thingUID) {
        ThingImpl thing = new ThingImpl(thingUID);
        return new ThingBuilder(thing);
    }

}
