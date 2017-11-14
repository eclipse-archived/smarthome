/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link DiscoveryResultBuilder} helps creating a {@link DiscoveryResult} through the builder pattern.
 *
 * @author Kai Kreuzer - Initial API
 * @author Andre Fuechsel - added support for time to live
 * @author Thomas Höfer - Added representation
 *
 * @see DiscoveryResult
 */
public class DiscoveryResultBuilder {

    final private ThingUID thingUID;

    private ThingUID bridgeUID;
    private Map<String, Object> properties = new HashMap<>();
    private String representationProperty;
    private String label;
    private long ttl = DiscoveryResult.TTL_UNLIMITED;
    private ThingTypeUID thingTypeUID;

    private DiscoveryResultBuilder(ThingUID thingUID) {
        this.thingTypeUID = thingUID.getThingTypeUID();
        this.thingUID = thingUID;
    };

    /**
     * Creates a new builder for a given thing UID.
     *
     * @param thingUID the thing UID for which the builder should be created-
     *
     * @return a new instance of a {@link DiscoveryResultBuilder}
     */
    public static DiscoveryResultBuilder create(ThingUID thingUID) {
        return new DiscoveryResultBuilder(thingUID);
    }

    /**
     * Explicitly sets the thing type.
     *
     * @param thingTypeUID the {@link ThingTypeUID}
     * @return the updated builder
     */
    public DiscoveryResultBuilder withThingType(ThingTypeUID thingTypeUID) {
        this.thingTypeUID = thingTypeUID;
        return this;
    }

    /**
     * Adds properties to the desired result.
     *
     * @param properties of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * Adds a property to the desired result.
     *
     * @param property of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * Sets the representation Property of the desired result.
     *
     * @param representationProperty the representation property of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withRepresentationProperty(String representationProperty) {
        this.representationProperty = representationProperty;
        return this;
    }

    /**
     * Sets the bridgeUID of the desired result.
     *
     * @param bridgeUID of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withBridge(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
        return this;
    }

    /**
     * Sets the label of the desired result.
     *
     * @param label of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Sets the time to live for the result in seconds.
     *
     * @param ttl time to live in seconds
     * @return the updated builder
     */
    public DiscoveryResultBuilder withTTL(long ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * Builds a result with the settings of this builder.
     *
     * @return the desired result
     */
    public DiscoveryResult build() {
        return new DiscoveryResultImpl(thingTypeUID, thingUID, bridgeUID, properties, representationProperty, label,
                ttl);
    }

}
