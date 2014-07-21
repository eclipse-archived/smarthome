/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		super(getArray(thingUID.getBindingId(), thingUID.getThingTypeId(), thingUID.getId(), id, thingUID.getBridgeIds()));
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
	
    private static String[] getArray(String bindingId, String thingTypeId, String thingId, String id, List<String> bridgeIds) {
    	if (bridgeIds == null || bridgeIds.size() == 0) {
    		return new String[] {
    	    		bindingId,thingTypeId,thingId,id
        	};
    	}
    	
    	String[] result = new String[4+bridgeIds.size()];
    	result[0] = bindingId;
    	result[1] = thingTypeId;
    	for (int i = 0; i < bridgeIds.size(); i++) {
			result[i+2] = bridgeIds.get(i);
		}
    	
    	result[result.length-2] = thingId;
    	result[result.length-1] = id;
    	return result;
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
     * Returns the bridge ids.
     * 
     * @return list of bridge ids
     */
    public List<String> getBridgeIds() {
    	List<String> bridgeIds = new ArrayList<>();
    	String[] segments = getSegments();
    	for (int i = 3; i < segments.length-1; i++) {
			bridgeIds.add(segments[i]);
		}
    	return bridgeIds;
    }
	
	/**
	 * Returns the id.
	 * 
	 * @return id
	 */
	public String getId() {
        String[] segments = getSegments();
		return segments[segments.length-1];
	}
	
	
	@Override
	protected int getMinimalNumberOfSegments() {
		return 4;
	}

	/**
     * Returns the thing UID
     * 
     * @return the thing UID
     */
    public ThingUID getThingUID() {
        return new ThingUID(Arrays.copyOfRange(getSegments(), 0, getSegments().length - 1));
    }

}
