/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ThingTypeUID} represents a unique identifier for thing types.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Jochen Hiller - Bugfix 455434: added default constructor
 */
@NonNullByDefault
public class ThingTypeUID extends UID {

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ThingTypeUID() {
        super();
    }

    public ThingTypeUID(String uid) {
        super(uid);
    }

    public ThingTypeUID(String bindingId, String thingTypeId) {
        super(bindingId, thingTypeId);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 2;
    }

    public String getId() {
        return getSegment(1);
    }
}
