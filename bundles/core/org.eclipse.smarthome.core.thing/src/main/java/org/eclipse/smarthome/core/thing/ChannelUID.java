/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Arrays;

/**
 * {@link ChannelUID} represents a unique identifier for channels.
 *
 * @author Oliver Libutzki - Initital contribution
 * @author Jochen Hiller - Bugfix 455434: added default constructor
 * @author Dennis Nobel - Added channel group id
 * @author Kai Kreuzer - Changed creation of channels to not require a thing type
 */
public class ChannelUID extends UID {

    private static final String CHANNEL_GROUP_SEPERATOR = "#";

    /**
     * Default constructor in package scope only. Will allow to instantiate this
     * class by reflection. Not intended to be used for normal instantiation.
     */
    ChannelUID() {
        super();
    }

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
        super(getArray(thingUID, null, id));
    }

    @Deprecated
    public ChannelUID(ThingTypeUID thingTypeUID, ThingUID thingUID, String id) {
        super(getArray(thingUID, null, id));
    }

    /**
     * @param thingUID
     *            the unique identifier of the thing the channel belongs to
     * @param groupId the channel's group id
     * @param id
     *            the channel's id
     */
    public ChannelUID(ThingUID thingUID, String groupId, String id) {
        super(getArray(thingUID, groupId, id));
    }

    @Deprecated
    public ChannelUID(ThingTypeUID thingTypeUID, ThingUID thingUID, String groupId, String id) {
        super(getArray(thingUID, groupId, id));
    }

    /**
     * @param thingTypeUID the unique id of the thing's thingType
     * @param thingId the id of the thing the channel belongs to
     * @param id the channel's id
     */
    @Deprecated
    public ChannelUID(ThingTypeUID thingTypeUID, String thingId, String id) {
        this(thingTypeUID.getBindingId(), thingTypeUID.getId(), thingId, id);
    }

    /**
     * @param bindingId the binding id of the thingType
     * @param thingTypeId the thing type id of the thing's thingType
     * @param thingId the id of the thing the channel belongs to
     * @param id the channel's id
     */
    @Deprecated
    public ChannelUID(String bindingId, String thingTypeId, String thingId, String id) {
        super(bindingId, thingTypeId, thingId, id);
    }

    /**
     * @param bindingId the binding id of the thingType
     * @param thingTypeId the thing type id of the thing's thingType
     * @param thingId the id of the thing the channel belongs to
     * @param groupId the channel's group id
     * @param id the channel's id
     */
    @Deprecated
    public ChannelUID(String bindingId, String thingTypeId, String thingId, String groupId, String id) {
        super(bindingId, thingTypeId, thingId, getChannelId(groupId, id));
    }

    private static String[] getArray(ThingUID thingUID, String groupId, String id) {

        String[] result = new String[thingUID.getSegments().length + 1];
        for (int i = 0; i < thingUID.getSegments().length; i++) {
            result[i] = thingUID.getSegments()[i];
        }
        result[result.length - 1] = getChannelId(groupId, id);

        return result;
    }

    private static String getChannelId(String groupId, String id) {
        return groupId != null ? groupId + CHANNEL_GROUP_SEPERATOR + id : id;
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

    /**
     * Returns the id without the group id.
     *
     * @return id id without group id
     */
    public String getIdWithoutGroup() {
        String[] segments = getSegments();
        if (!isInGroup()) {
            return segments[segments.length - 1];
        } else {
            return segments[segments.length - 1].split(CHANNEL_GROUP_SEPERATOR)[1];
        }
    }

    public boolean isInGroup() {
        String[] segments = getSegments();
        return segments[segments.length - 1].contains(CHANNEL_GROUP_SEPERATOR);
    }

    /**
     * Returns the group id.
     *
     * @return group id or null if channel is not in a group
     */
    public String getGroupId() {
        String[] segments = getSegments();
        return isInGroup() ? segments[segments.length - 1].split(CHANNEL_GROUP_SEPERATOR)[0] : null;
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 4;
    }

    @Override
    protected void validateSegment(String segment, int index, int length) {
        if (index < length - 1) {
            super.validateSegment(segment, index, length);
        } else {
            if (!segment.matches("[A-Za-z0-9_#-]*")) {
                throw new IllegalArgumentException("UID segment '" + segment
                        + "' contains invalid characters. The last segment of the channel UID must match the pattern [A-Za-z0-9_-#]*.");
            }
        }
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
