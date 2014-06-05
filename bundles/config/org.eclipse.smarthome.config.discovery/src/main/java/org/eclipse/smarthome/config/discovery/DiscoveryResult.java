/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;


/**
 * The {@link DiscoveryResult} is a container for one result of a discovery process.
 * The discovery process can lead to <i>0..N</i> {@link DiscoveryResult} objects
 * which are fired as an event to registered {@link DiscoveryListener}s.
 *
 * @author Michael Grammling - Initial Contribution.
 * @author Oliver Libutzki - Introduction of ThingUID
 *
 * @see DiscoveryService
 * @see DiscoveryListener
 */
public final class DiscoveryResult {

	private ThingUID thingUID;
	
    private Map<String, Object> properties;

    private DiscoveryResultFlag flag;

    private String label;

    private ThingUID bridgeUID;

    private ThingTypeUID thingTypeUID;

    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param thingType
     *            the Thing type to be set (must neither be null nor empty). A
     *            {@code Thing} type could be a product number which identifies
     *            the same type of {@link Thing}s. It's usually <i>not</i> a
     *            serial number.
     * 
     * @param thingId
     *            the Thing id to be set (must not be null). If a {@code Thing}
     *            disappears and is discovered again, the same {@code Thing} UID
     *            must be created. A typical {@code Thing} UID could be the
     *            serial number. It's usually <i>not</i> a product number.
     * 
     * @throws IllegalArgumentException
     *             if the Thing type or the Thing ID is null or empty
     */
    public DiscoveryResult(ThingTypeUID thingTypeUID, ThingUID thingUID)
            throws IllegalArgumentException {
        if (thingTypeUID == null) {
            throw new IllegalArgumentException("The thing type must not be null!");
        }
        if (thingUID == null) {
            throw new IllegalArgumentException("The thing UID must not be null!");
        }
        this.thingTypeUID = thingTypeUID;
        this.thingUID = thingUID;
        
        setProperties(null);
        setFlag(null);
        setLabel(null);
    }

    /**
     * Returns the {@code ThingUID} of this result object.
     * <p>
     * A {@code ThingUID} must be a unique identifier of a concrete
     * {@code Thing} which <i>must not</i> consist of data which could change
     * (e.g. configuration data such as an IP address). If a {@code Thing}
     * disappears and is discovered again, the same {@code Thing} ID must be
     * created. A typical {@code Thing} ID could be the serial number. It's
     * usually <i>not</i> a product number.
     * 
     * @return the Thing ID of this result object (not null, not empty)
     */
    public ThingUID getThingUID() {
		return thingUID;
	}

    /**
     * Returns the {@code ThingTypeUID} of this result object.
     * <p>
     * A {@code Thing} type could be a product number which identifies the same
     * type of {@link Thing}s. It's usually <i>not</i> a serial number.
     * 
     * @return the Thing type of this result object (not null, not empty)
     */
    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    /**
     * Returns the binding ID of this result object.
     * <p>
     * The binding ID is extracted from the {@code Thing} ID.
     *
     * @return the binding ID of this result object (not null, not empty)
     */
    public String getBindingId() {
    	ThingUID thingId = this.thingUID;
    	if (thingId != null) {
    		return thingId.getBindingId();
    	}
        return null;
    }

    /**
     * Sets the properties of this result object.<br>
     * The properties contain information which become part of a {@code Thing}.
     * <p>
     * If the specified properties are {@code null}, an empty {@link Map} is created.
     *
     * @param properties the properties to be set (could be null or empty)
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = (properties != null) ? properties : new HashMap<String, Object>();
    }

    /**
     * Returns the properties of this result object.<br>
     * The properties contain information which become part of a {@code Thing}.
     *
     * @return the properties of this result object (not null, could be empty)
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Sets the flag of this result object.<br>
     * The flag signals e.g. if the result is {@link DiscoveryResultFlag#NEW}
     * or has been marked as {@link DiscoveryResultFlag#IGNORED}. In the latter
     * case the result object should be regarded as known by the system so that
     * further processing should be skipped.
     * <p>
     * If the specified flag is {@code null}, {@link DiscoveryResultFlag.NEW}
     * is set by default.
     *
     * @param flag the flag of this result object to be set (could be null)
     */
    public void setFlag(DiscoveryResultFlag flag) {
        this.flag = (flag == null) ? DiscoveryResultFlag.NEW : flag;
    }

    /**
     * Returns the flag of this result object.<br>
     * The flag signals e.g. if the result is {@link DiscoveryResultFlag#NEW}
     * or has been marked as {@link DiscoveryResultFlag#IGNORED}. In the latter
     * case the result object should be regarded as known by the system so that
     * further processing should be skipped.
     *
     * @return the flag of this result object (not null)
     */
    public DiscoveryResultFlag getFlag() {
        return this.flag;
    }

    /**
     * Sets a human readable label for this result object.
     * <p>
     * If the specified label is {@code null}, an empty text is set.
     *
     * @param label the human readable label to set (could be null or empty)
     */
    public void setLabel(String label) {
        this.label = (label == null) ? "" : label;
    }

    /**
     * Returns the human readable label for this result object.
     *
     * @return the human readable label for this result object (not null, could be empty)
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the bridge id of the {@link DiscoveryResult}.
     * 
     * @param bridgeUID
     *            bridge id
     */
    public void setBridgeUID(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
    }

    /**
     * Returns the bridge id of the {@link DiscoveryResult}.
     * 
     * @return bridge unique id
     */
    public ThingUID getBridgeUID() {
        return bridgeUID;
    }

    /**
     * Merges the content of the specified source {@link DiscoveryResult} into
     * this object.
     * <p>
     * <i>Hint:</i> The {@link DiscoveryResultFlag} of this object keeps its
     * state.
     * <p>
     * This method returns silently if the specified source
     * {@link DiscoveryResult} is {@code null} or its {@code Thing} type or ID
     * does not fit to this object.
     * 
     * @param sourceResult
     *            the discovery result which is used as source for the merge
     */
    public void synchronize(DiscoveryResult sourceResult) {
        if ((sourceResult != null)
                && (sourceResult.getThingUID().equals(this.thingUID))) {
            this.properties = sourceResult.getProperties();
            this.label = sourceResult.getLabel();
        }
    }

    @Override
    public String toString() {
        return "DiscoveryResult [thingUID=" + thingUID
                + ", properties=" + properties
                + ", flag=" + flag + ", label=" + label + "]";
    }

}
