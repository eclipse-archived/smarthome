/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;



/**
 * {@link ThingUID} represents a unique identifier for things.
 */
public class ThingUID extends UID {

    /**
     * Instantiates a new thing UID.
     * 
     * @param thingType
     *            the thing type
     * @param id
     *            the id
     */
    public ThingUID(ThingTypeUID thingTypeUID, String id) {
        super(thingTypeUID.getBindingId(), thingTypeUID.getId(), id);
    }

    /**
     * Instantiates a new thing UID.
     * 
     * @param bindingId
     *            the binding id
     * @param thingTypeId
     *            the thing type id
     * @param id
     *            the id
     */
	public ThingUID(String bindingId, String thingTypeId, String id) {
        super(bindingId, thingTypeId, id);
	}

    /**
     * Instantiates a new thing UID.
     * 
     * @param thingUID
     *            the thing UID
     */
	public ThingUID(String thingUID) {
        super(thingUID);
	}

    /**
     * Returns the thing type id.
     * 
     * @return thing type id
     */
    public String getThingTypeId() {
        return getSegment(1);
	}

    /**
     * Returns the id.
     * 
     * @return id
     */
	public String getId() {
        return getSegment(2);
	}

    @Override
    protected int getNumberOfSegments() {
        return 3;
    }

}
