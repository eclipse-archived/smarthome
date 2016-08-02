/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
 * @author Jochen Hiller - Bugfix 455434: added default constructor
 */
public class ThingUID extends UID {

    private static final String NO_THING_TYPE = "";

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ThingUID() {
        super();
    }

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
     * @param bridgeUID
     *            the bridge UID through which the thing is accessed
     * @param id
     *            the id of the thing
     */
    public ThingUID(ThingTypeUID thingTypeUID, ThingUID bridgeUID, String id) {
        super(getArray(thingTypeUID.getBindingId(), thingTypeUID.getId(), id, bridgeUID.getBridgeIds(),
                bridgeUID.getId()));
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

    /**
     * Instantiates a new thing UID.
     *
     * @param bindingId
     *            the binding id
     * @param id
     *            the id
     */
    public ThingUID(String bindingId, String id) {
        super(bindingId, NO_THING_TYPE, id);
    }

    /**
     * Instantiates a new thing UID.
     *
     * @param bindingId
     *            the binding id
     * @param bridgeUID
     *            the bridge UID through which the thing is accessed
     * @param id
     *            the id
     */
    public ThingUID(String bindingId, ThingUID bridgeUID, String id) {
        super(getArray(bindingId, NO_THING_TYPE, id, bridgeUID.getBridgeIds(), bridgeUID.getId()));
    }

    private static String[] getArray(String bindingId, String thingTypeId, String id, String... bridgeIds) {
        if (bridgeIds == null || bridgeIds.length == 0) {
            return new String[] { bindingId, thingTypeId, id };
        }

        String[] result = new String[3 + bridgeIds.length];
        result[0] = bindingId;
        result[1] = thingTypeId;
        for (int i = 0; i < bridgeIds.length; i++) {
            result[i + 2] = bridgeIds[i];
        }
        result[result.length - 1] = id;
        return result;
    }

    private static String[] getArray(String bindingId, String thingTypeId, String id, List<String> bridgeIds,
            String bridgeId) {
        List<String> allBridgeIds = new ArrayList<>(bridgeIds);
        allBridgeIds.add(bridgeId);
        return getArray(bindingId, thingTypeId, id, allBridgeIds.toArray(new String[0]));
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
     * @deprecated use {@link Thing#getThingTypeUID()} instead.
     */
    @Deprecated
    public String getThingTypeId() {
        String thingType = getSegment(1);
        if (NO_THING_TYPE.equals(thingType)) {
            return null;
        } else {
            return thingType;
        }
    }

    /**
     * Returns the thing type uid.
     *
     * @return thing type uid
     * @deprecated use {@link Thing#getThingTypeUID()} instead.
     */
    @Deprecated
    public ThingTypeUID getThingTypeUID() {
        String thingType = getSegment(1);
        if (NO_THING_TYPE.equals(thingType)) {
            return null;
        } else {
            return new ThingTypeUID(getSegment(0), getSegment(1));
        }
    }

    /**
     * Returns the bridge ids.
     *
     * @return list of bridge ids
     */
    public List<String> getBridgeIds() {
        List<String> bridgeIds = new ArrayList<>();
        String[] segments = getSegments();
        for (int i = 2; i < segments.length - 1; i++) {
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
        return segments[segments.length - 1];
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 3;
    }

}
