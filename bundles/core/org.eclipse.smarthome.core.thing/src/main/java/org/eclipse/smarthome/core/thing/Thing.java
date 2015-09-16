/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;

/**
 * A {@link Thing} is a representation of a connected part (e.g. physical device
 * or cloud service) from the real world. It contains a list of {@link Channel} s, which can be bound to {@link Item}s.
 * A {@link Thing} might be connected
 * through a {@link Bridge}.
 *
 * @author Dennis Nobel - Initial contribution and API
 * @author Thomas Höfer - Added thing and thing type properties
 */
public interface Thing {

    /** the key for the vendor property */
    public static final String PROPERTY_VENDOR = "vendor";

    /** the key for the model ID property */
    public static final String PROPERTY_MODEL_ID = "modelId";

    /** the key for the serial number property */
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";

    /** the key for the hardware version property */
    public static final String PROPERTY_HARDWARE_VERSION = "hardwareVersion";

    /** the key for the firmware version property */
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";

    /**
     * Gets the channels.
     *
     * @return the channels
     */
    List<Channel> getChannels();

    /**
     * Gets the channel for the given id or null if no channel with the id
     * exists.
     *
     * @param channelId
     *            channel ID
     *
     * @return the channel for the given id or null if no channel with the id
     *         exists
     */
    Channel getChannel(String channelId);

    /**
     * Gets the status of a thing.
     * In order to get all status information (status, status detail and status description)
     * please use {@link Thing#getStatusInfo()}.
     *
     * @return the status
     */
    ThingStatus getStatus();

    /**
     * Gets the status info of a thing.
     * The status info consists of the status itself, the status detail and a status description.
     *
     * @return the status info
     */
    ThingStatusInfo getStatusInfo();

    /**
     * Sets the status info.
     *
     * @param status
     *            the new status info
     */
    void setStatusInfo(ThingStatusInfo status);

    /**
     * Sets the handler.
     *
     * @param thingHandler
     *            the new handler
     */
    void setHandler(ThingHandler thingHandler);

    /**
     * Gets the handler.
     *
     * @return the handler (can be null)
     */
    ThingHandler getHandler();

    /**
     * Gets the bridge UID.
     *
     * @return the bridge UID (can be null)
     */
    ThingUID getBridgeUID();

    /**
     * Sets the bridge.
     *
     * @param bridge
     *            the new bridge
     */
    void setBridgeUID(ThingUID bridgeUID);

    /**
     * Gets the configuration.
     *
     * @return the configuration (not null)
     */
    Configuration getConfiguration();

    /**
     * Gets the uid.
     *
     * @return the uid
     */
    ThingUID getUID();

    /**
     * Gets the thing type UID.
     *
     * @return the thing type UID
     */
    ThingTypeUID getThingTypeUID();

    /**
     * Returns the group item, which is linked to the thing or null if no item is
     * linked.
     *
     * @deprecated Will be removed soon, because it is dynamic data which does not belong to the thing. Use
     *             {@link ItemThingLinkRegistry} instead.
     *
     * @return group item , which is linked to the thing or null
     */
    @Deprecated
    GroupItem getLinkedItem();

    /**
     * Returns whether the thing is linked to an item.
     *
     * @deprecated Will be removed soon, because it is dynamic data which does not belong to the thing. Use
     *             {@link ItemThingLinkRegistry} instead.
     *
     * @return true if thing is linked, false otherwise.
     */
    @Deprecated
    public boolean isLinked();

    /**
     * Returns an immutable copy of the {@link Thing} properties.
     *
     * @return an immutable copy of the {@link Thing} properties (not null)
     */
    Map<String, String> getProperties();

    /**
     * Sets the property value for the property identified by the given name. If the value to be set is null then the
     * property will be removed.
     *
     * @param name the name of the property to be set (must not be null or empty)
     *
     * @param value the value of the property (if null then the property with the given name is removed)
     *
     * @return the previous value associated with the name, or null if there was no mapping for the name
     */
    String setProperty(String name, String value);
}
