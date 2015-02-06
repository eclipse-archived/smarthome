/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link DiscoveryResultBuilder} helps creating a {@link DiscoveryResult} through the builder pattern.
 *
 * @author Kai Kreuzer - Initial API
 *
 * @see DiscoveryResult
 */
public class DiscoveryResultBuilder {

    final private ThingUID thingUID;

    private ThingUID bridgeUID;
    private Map<String, Object> properties = new HashMap<>();
    private String label;

    private DiscoveryResultBuilder(ThingUID thingUID) {
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
     * Adds properties to the desired result
     * 
     * @param properties of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * Adds a property to the desired result
     * 
     * @param property of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * Sets the bridgeUID of the desired result
     * 
     * @param bridgeUID of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withBridge(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
        return this;
    }

    /**
     * Sets the label of the desired result
     * 
     * @param label of the desired result
     * @return the updated builder
     */
    public DiscoveryResultBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Builds a result with the settings of this builder.
     *
     * @return the desired result
     */
    public DiscoveryResult build() {
        return new DiscoveryResultImpl(thingUID, bridgeUID, properties, label);
    }

}
