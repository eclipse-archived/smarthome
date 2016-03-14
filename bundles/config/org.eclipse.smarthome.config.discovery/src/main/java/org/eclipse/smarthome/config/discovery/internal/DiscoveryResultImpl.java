/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

public class DiscoveryResultImpl implements DiscoveryResult {

    private ThingUID bridgeUID;
    private ThingUID thingUID;
    private ThingTypeUID thingTypeUID;

    private Map<String, Object> properties;
    private String representationProperty;
    private DiscoveryResultFlag flag;
    private String label;
    private long timestamp;
    private long timeToLive = TTL_UNLIMITED;

    /**
     * Package protected default constructor to allow reflective instantiation.
     */
    DiscoveryResultImpl() {
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param thingUID
     *            the Thing UID to be set (must not be null). If a {@code Thing} disappears and is discovered again, the
     *            same {@code Thing} ID
     *            must be created. A typical {@code Thing} ID could be the
     *            serial number. It's usually <i>not</i> a product name.
     * @param properties the properties to be set (could be null or empty)
     * @param representationProperty the representationProperty to be set (could be null or empty)
     * @param label the human readable label to set (could be null or empty)
     * @param bridgeUID the unique bridge ID to be set
     * @param timeToLive time to live in seconds
     *
     * @throws IllegalArgumentException
     *             if the Thing type UID or the Thing UID is null
     * @deprecated use {@link #DiscoveryResultImpl(ThingUID, ThingTypeUID, ThingUID, Map, String, String, long)}
     *             instead.
     */
    @Deprecated
    public DiscoveryResultImpl(ThingUID thingUID, ThingUID bridgeUID, Map<String, Object> properties,
            String representationProperty, String label, long timeToLive) throws IllegalArgumentException {
        this(thingUID.getThingTypeUID(), thingUID, bridgeUID, properties, representationProperty, label, timeToLive);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param thingTypeUID the {@link ThingTypeUID}
     * @param thingUID the Thing UID to be set (must not be null). If a {@code Thing} disappears and is discovered
     *            again, the same {@code Thing} ID must be created. A typical {@code Thing} ID could be the serial
     *            number. It's usually <i>not</i> a product name.
     * @param properties the properties to be set (could be null or empty)
     * @param representationProperty the representationProperty to be set (could be null or empty)
     * @param label the human readable label to set (could be null or empty)
     * @param bridgeUID the unique bridge ID to be set
     * @param timeToLive time to live in seconds
     *
     * @throws IllegalArgumentException
     *             if the Thing type UID or the Thing UID is null
     */
    public DiscoveryResultImpl(ThingTypeUID thingTypeUID, ThingUID thingUID, ThingUID bridgeUID,
            Map<String, Object> properties, String representationProperty, String label, long timeToLive)
                    throws IllegalArgumentException {
        if (thingUID == null) {
            throw new IllegalArgumentException("The thing UID must not be null!");
        }
        if (timeToLive < 1 && timeToLive != TTL_UNLIMITED) {
            throw new IllegalArgumentException("The ttl must not be 0 or negative!");
        }

        this.thingUID = thingUID;
        this.thingTypeUID = thingTypeUID;
        this.bridgeUID = bridgeUID;
        this.properties = Collections
                .unmodifiableMap((properties != null) ? new HashMap<>(properties) : new HashMap<String, Object>());
        this.representationProperty = representationProperty;
        this.label = label == null ? "" : label;

        this.timestamp = new Date().getTime();
        this.timeToLive = timeToLive;

        this.flag = DiscoveryResultFlag.NEW;
    }

    /**
     * Returns the unique {@code Thing} ID of this result object.
     * <p>
     * A {@link ThingUID} must be a unique identifier of a concrete {@code Thing} which <i>must not</i> consist of data
     * which could change (e.g. configuration data such as an IP address). If a {@code Thing} disappears and is
     * discovered again, the same {@code Thing} ID must be created. A typical {@code Thing} ID could be the serial
     * number. It's usually <i>not</i> a product number.
     *
     * @return the Thing ID of this result object (not null, not empty)
     */
    @Override
    public ThingUID getThingUID() {
        return thingUID;
    }

    /**
     * Returns the unique {@code Thing} type ID of this result object.
     * <p>
     * A {@code Thing} type ID could be a product number which identifies the same type of {@link Thing}s. It's usually
     * <i>not</i> a serial number.
     *
     * @return the unique Thing type of this result object (not null, not empty)
     */
    @Override
    public ThingTypeUID getThingTypeUID() {
        if (this.thingTypeUID != null) {
            return this.thingTypeUID;
        } else {
            // fallback for discovery result which were created before the thingTypeUID field was added
            return this.thingUID.getThingTypeUID();
        }
    }

    /**
     * Returns the binding ID of this result object.
     * <p>
     * The binding ID is extracted from the unique {@code Thing} ID.
     *
     * @return the binding ID of this result object (not null, not empty)
     */
    @Override
    public String getBindingId() {
        ThingUID thingId = this.thingUID;
        if (thingId != null) {
            return thingId.getBindingId();
        }
        return null;
    }

    /**
     * Returns the properties of this result object.<br>
     * The properties contain information which become part of a {@code Thing}.
     * <p>
     * <b>Hint:</b> The returned properties are immutable.
     *
     * @return the properties of this result object (not null, could be empty)
     */
    @Override
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Returns the representation property of this result object.
     * <p>
     * The representation property represents an unique human and/or machine readable identifier of the thing that was
     * discovered. Its actual value can be retrieved from the {@link DiscoveryResult#getProperties()} map. Such unique
     * identifiers are among others the <code>ipAddress</code>, the <code>macAddress</code> or the
     * <code>serialNumber</code> of the discovered thing.
     *
     * @return the representation property of this result object (could be null)
     */
    @Override
    public String getRepresentationProperty() {
        return this.representationProperty;
    }

    /**
     * Returns the flag of this result object.<br>
     * The flag signals e.g. if the result is {@link DiscoveryResultFlag#NEW} or has been marked as
     * {@link DiscoveryResultFlag#IGNORED}. In the latter
     * case the result object should be regarded as known by the system so that
     * further processing should be skipped.
     *
     * @return the flag of this result object (not null)
     */
    @Override
    public DiscoveryResultFlag getFlag() {
        return this.flag;
    }

    /**
     * Returns the human readable label for this result object.
     *
     * @return the human readable label for this result object (not null, could be empty)
     */
    @Override
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the unique bridge ID of the {@link DiscoveryResult}.
     *
     * @return the unique bridge ID (could be null)
     */
    @Override
    public ThingUID getBridgeUID() {
        return bridgeUID;
    }

    /**
     * Merges the content of the specified source {@link DiscoveryResult} into this object.
     * <p>
     * <i>Hint:</i> The {@link DiscoveryResultFlag} of this object keeps its state.
     * <p>
     * This method returns silently if the specified source {@link DiscoveryResult} is {@code null} or its {@code Thing}
     * type or ID does not fit to this object.
     *
     * @param sourceResult the discovery result which is used as source for the merge
     */
    public void synchronize(DiscoveryResult sourceResult) {
        if ((sourceResult != null) && (sourceResult.getThingUID().equals(this.thingUID))) {

            this.properties = sourceResult.getProperties();
            this.representationProperty = sourceResult.getRepresentationProperty();
            this.label = sourceResult.getLabel();
            this.timestamp = new Date().getTime();
            this.timeToLive = sourceResult.getTimeToLive();
        }
    }

    /**
     * Sets the flag of this result object.<br>
     * The flag signals e.g. if the result is {@link DiscoveryResultFlag#NEW} or has been marked as
     * {@link DiscoveryResultFlag#IGNORED}. In the latter
     * case the result object should be regarded as known by the system so that
     * further processing should be skipped.
     * <p>
     * If the specified flag is {@code null}, {@link DiscoveryResultFlag.NEW} is set by default.
     *
     * @param flag the flag of this result object to be set (could be null)
     */
    public void setFlag(DiscoveryResultFlag flag) {
        this.flag = (flag == null) ? DiscoveryResultFlag.NEW : flag;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((thingUID == null) ? 0 : thingUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscoveryResultImpl other = (DiscoveryResultImpl) obj;
        if (thingUID == null) {
            if (other.thingUID != null)
                return false;
        } else if (!thingUID.equals(other.thingUID))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DiscoveryResult [thingUID=" + thingUID + ", properties=" + properties + ", flag=" + flag + ", label="
                + label + ", bridgeUID=" + bridgeUID + ", ttl=" + timeToLive + ", timestamp=" + timestamp + "]";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getTimeToLive() {
        return timeToLive;
    }
}
