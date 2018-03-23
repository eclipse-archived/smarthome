/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ChannelUID} represents a unique identifier for channels.
 *
 * @author Oliver Libutzki - Initital contribution
 * @author Jochen Hiller - Bugfix 455434: added default constructor
 * @author Dennis Nobel - Added channel group id
 * @author Kai Kreuzer - Changed creation of channels to not require a thing type
 */
@NonNullByDefault
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
     * @param thingUID the unique identifier of the thing the channel belongs to
     * @param id the channel's id
     */
    public ChannelUID(ThingUID thingUID, String id) {
        super(getArray(thingUID, null, id));
    }

    @Deprecated
    public ChannelUID(ThingTypeUID thingTypeUID, ThingUID thingUID, String id) {
        super(getArray(thingUID, null, id));
    }

    /**
     * @param thingUID the unique identifier of the thing the channel belongs to
     * @param groupId the channel's group id
     * @param id the channel's id
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

    private static List<String> getArray(ThingUID thingUID, @Nullable String groupId, String id) {
        List<String> ret = new ArrayList<>(thingUID.getAllSegments());
        ret.add(getChannelId(groupId, id));
        return ret;
    }

    private static String getChannelId(@Nullable String groupId, String id) {
        return groupId != null ? groupId + CHANNEL_GROUP_SEPERATOR + id : id;
    }

    /**
     * Returns the id.
     *
     * @return id
     */
    public String getId() {
        List<String> segments = getAllSegments();
        return segments.get(segments.size() - 1);
    }

    /**
     * Returns the id without the group id.
     *
     * @return id id without group id
     */
    public String getIdWithoutGroup() {
        if (!isInGroup()) {
            return getId();
        } else {
            return getId().split(CHANNEL_GROUP_SEPERATOR)[1];
        }
    }

    public boolean isInGroup() {
        return getId().contains(CHANNEL_GROUP_SEPERATOR);
    }

    /**
     * Returns the group id.
     *
     * @return group id or null if channel is not in a group
     */
    public @Nullable String getGroupId() {
        return isInGroup() ? getId().split(CHANNEL_GROUP_SEPERATOR)[0] : null;
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
        List<@NonNull String> allSegments = getAllSegments();
        return new ThingUID(allSegments.subList(0, allSegments.size() - 1).toArray(new String[allSegments.size() - 1]));
    }

}
