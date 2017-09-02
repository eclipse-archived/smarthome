/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;

/**
 * The {@link ThingTypeProvider} is responsible for providing thing types.
 *
 * @author Dennis Nobel
 *
 */
public interface ThingTypeProvider {

    /**
     * Provides a collection of thing types
     *
     * @param locale
     *            locale (can be null)
     *
     * @return the thing types provided by the {@link ThingTypeProvider}
     */
    Collection<ThingType> getThingTypes(Locale locale);

    /**
     * Provides a thing type for the given UID or null if no type for the
     * given UID exists.
     *
     * @param locale
     *            locale (can be null)
     * @return thing type for the given UID or null if no type for the given
     *         UID exists
     */
    @Nullable
    ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale);

}
