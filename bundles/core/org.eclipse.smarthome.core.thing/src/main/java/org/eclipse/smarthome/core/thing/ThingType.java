/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link ThingType} describes a concrete type of a {@link Thing}.
 * <p>
 * This description is used as template definition for the creation of the
 * according concrete {@link Thing} object.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 * @author Dennis Nobel - Initial Contribution
 */
public class ThingType extends AbstractDescriptionType {


    private final List<ChannelDefinition> channelDefinitions;
    private final String manufacturer;
    private final List<String> supportedBridgeTypeUIDs;

    /**
     * @see ThingType#ThingType(String, List, String, String, String,
     *      List, String)
     */
    public ThingType(String bindingId, String thingTypeId, String label, String description,
            String manufacturer)
            throws IllegalArgumentException {
        this(new ThingTypeUID(bindingId, thingTypeId), null, label, description, manufacturer, null, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param uid
     *            the unique identifier which identifies this Thing type within
     *            the overall system (must neither be null, nor empty)
     * 
     * @param supportedBridgeTypeUIDs
     *            the unique identifiers to the bridges this Thing type supports
     *            (could be null or empty)
     * 
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     * 
     * @param description the human readable description for the according type
     *     (must neither be null nor empty)
     * 
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     * 
     * @param description the human readable description for the according type
     *     (must neither be null nor empty)
     * 
     * @param channelDefinitions
     *            the channels this Thing type provides (could be null or empty)
     * 
     * @param configDescriptionURI
     *            the link to the concrete ConfigDescription (could be null)
     * 
     * @throws IllegalArgumentException
     *             if the UID is null or empty, or the the meta information is
     *             null
     */
    public ThingType(ThingTypeUID uid, List<String> supportedBridgeTypeUIDs,
            String label, String description, String manufacturer,
            List<ChannelDefinition> channelDefinitions, String configDescriptionURI)
            throws IllegalArgumentException {

        super(uid, label, description, configDescriptionURI);

        this.manufacturer = manufacturer;

        if (supportedBridgeTypeUIDs != null) {
            this.supportedBridgeTypeUIDs = Collections.unmodifiableList(supportedBridgeTypeUIDs);
        } else {
            this.supportedBridgeTypeUIDs = Collections.unmodifiableList(new ArrayList<String>(0));
        }

        if (channelDefinitions != null) {
            this.channelDefinitions = Collections.unmodifiableList(channelDefinitions);
        } else {
            this.channelDefinitions = Collections
                    .unmodifiableList(new ArrayList<ChannelDefinition>(0));
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThingType other = (ThingType) obj;

        return this.getUID().equals(other.getUID());
    }

    /**
     * Returns the binding id
     * 
     * @return binding id (could not be null)
     */
    public String getBindingId() {
        return this.getUID().getBindingId();
    }

    /**
     * Returns the channels this {@link ThingType} provides.
     * <p>
     * The returned list is immutable.
     * 
     * @return the channels this Thing type provides (could not be null)
     */
    public List<ChannelDefinition> getChannelDefinitions() {
        return this.channelDefinitions;
    }

    /**
     * Returns the id.
     * 
     * @return id (could not be null)
     */
    public ThingTypeUID getUID() {
        return (ThingTypeUID) super.getUID();
    }

    /**
     * Returns the human readable name of the manufacturer.
     * 
     * @return the human readable name of the manufacturer (could be null or
     *         empty)
     */
    public String getManufacturer() {
        return this.manufacturer;
    }

    /**
     * Returns the unique identifiers to the bridges this {@link ThingType}
     * supports.
     * <p>
     * The returned list is immutable.
     * 
     * @return the unique identifiers to the bridges this Thing type supports
     *         (could not be null)
     */
    public List<String> getSupportedBridgeTypeUIDs() {
        return this.supportedBridgeTypeUIDs;
    }

    @Override
    public int hashCode() {
       return getUID().hashCode();
    }

    @Override
    public String toString() {
        return getUID().toString();
    }
}
