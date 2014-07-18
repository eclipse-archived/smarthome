/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.ArrayList;
import java.util.List;



/**
 * {@link ThingUID} represents a unique identifier for things.
 *  
 * @author Dennis Nobel - Initial contribution
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
     * @param thingType
     *            the thing type
     * @param id
     *            the id
     */
    public ThingUID(ThingTypeUID thingTypeUID, String id, String... bridgeIds) {
    	super(getArray(thingTypeUID.getBindingId(), thingTypeUID.getId(), id, bridgeIds));
    }
    
    private static String[] getArray(String bindingId, String thingTypeId, String id, String... bridgeIds) {
    	if (bridgeIds == null || bridgeIds.length == 0) {
    		return new String[] {
    	    		bindingId,thingTypeId,id
        	};
    	}
    	
    	String[] result = new String[3+bridgeIds.length];
    	result[0] = bindingId;
    	result[1] = thingTypeId;
    	for (int i = 0; i < bridgeIds.length; i++) {
			result[i+2] = bridgeIds[i];
		}
    	result[result.length-1] = id;
    	return result;
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
	 * Instantiates a new thing UID.
	 * 
     * @param segments
     *            segments (must not be null)
	 */
	public ThingUID(String... segments) {
	    super(segments);
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
     * Returns the bridge ids.
     * 
     * @return list of bridge ids
     */
    public List<String> getBridgeIds() {
    	List<String> bridgeIds = new ArrayList<>();
    	String[] segments = getSegments();
    	for (int i = 2; i < segments.length-1; i++) {
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
        return 3;
    }

}
