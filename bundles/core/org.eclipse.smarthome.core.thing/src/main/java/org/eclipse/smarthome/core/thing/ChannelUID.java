/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

/**
 * {@link ChannelUID} represents a unique identifier for channels.
 * 
 * @author Oliver Libutzki - Initital contribution
 */
public class ChannelUID extends UID {

    public ChannelUID(String channelUid) {
        super(channelUid);
    }

    /**
     * @param thingUID
     *            the unique identifier of the thing the channel belongs to
     * @param id
     *            the channel's id
     */
	public ChannelUID(ThingUID thingUID, String id) {
		this(thingUID.getBindingId(), thingUID.getThingTypeId(), thingUID.getId(), id);
	}
	

	/**
	 * @param thingTypeUID the unique id of the thing's thingType
	 * @param thingId the id of the thing the channel belongs to
	 * @param id the channel's id
	 */
	public ChannelUID(ThingTypeUID thingTypeUID, String thingId, String id) {
		this(thingTypeUID.getBindingId(), thingTypeUID.getId(), thingId, id);
	}
	
	/**
	 * @param bindingId the binding id of the thingType
	 * @param thingTypeId the thing type id of the thing's thingType
	 * @param thingId the id of the thing the channel belongs to
	 * @param id the channel's id
	 */
	public ChannelUID(String bindingId, String thingTypeId, String thingId, String id) {
		super(bindingId, thingTypeId, thingId, id);
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
     * Returns the thing id.
     * 
     * @return thing id
     */
	public String getThingId() {
        return getSegment(2);
	}
	
	/**
	 * Returns the id.
	 * 
	 * @return id
	 */
	public String getId() {
		return getSegment(3);
	}
	
	
	@Override
	protected int getNumberOfSegments() {
		return 4;
	}

}
