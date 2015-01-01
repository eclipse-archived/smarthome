/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;


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
    private final List<String> supportedBridgeTypeUIDs;


    /**
     * @see ThingType#ThingType(ThingTypeUID, List, String, String, List, URI)
     */
    public ThingType(String bindingId, String thingTypeId, String label)
            throws IllegalArgumentException {

        this(new ThingTypeUID(bindingId, thingTypeId), null, label, null, null, null);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param uid the unique identifier which identifies this Thing type within the overall system
     *     (must neither be null, nor empty)
     * 
     * @param supportedBridgeTypeUIDs the unique identifiers of the bridges this Thing type supports
     *     (could be null or empty)
     * 
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     * 
     * @param description the human readable description for the according type
     *     (could be null or empty)
     * 
     * @param channelDefinitions the channels this Thing type provides (could be null or empty)
     * 
     * @param configDescriptionURI the link to the concrete ConfigDescription (could be null)
     * 
     * @throws IllegalArgumentException
     *     if the UID is null or empty, or the the meta information is null
     */
    public ThingType(ThingTypeUID uid, List<String> supportedBridgeTypeUIDs,
            String label, String description, List<ChannelDefinition> channelDefinitions,
            URI configDescriptionURI)
            throws IllegalArgumentException {

        super(uid, label, description, configDescriptionURI);

        if (supportedBridgeTypeUIDs != null) {
            this.supportedBridgeTypeUIDs = Collections.unmodifiableList(supportedBridgeTypeUIDs);
        } else {
            this.supportedBridgeTypeUIDs = Collections.unmodifiableList(new ArrayList<String>(0));
        }

        if (channelDefinitions != null) {
            this.channelDefinitions = Collections.unmodifiableList(channelDefinitions);
        } else {
            this.channelDefinitions = Collections.unmodifiableList(
                    new ArrayList<ChannelDefinition>(0));
        }
    }

    /**
     * Returns the unique identifier which identifies this Thing type within the overall system.
     * 
     * @return the unique identifier which identifies this Thing type within the overall system
     *     (not null)
     */
    public ThingTypeUID getUID() {
        return (ThingTypeUID) super.getUID();
    }

    /**
     * Returns the binding ID this Thing type belongs to.
     * 
     * @return the binding ID this Thing type belongs to (not null)
     */
    public String getBindingId() {
        return this.getUID().getBindingId();
    }

    /**
     * Returns the unique identifiers of the bridges this {@link ThingType} supports.
     * <p>
     * The returned list is immutable.
     * 
     * @return the unique identifiers of the bridges this Thing type supports
     *     (not null, could be empty)
     */
    public List<String> getSupportedBridgeTypeUIDs() {
        return this.supportedBridgeTypeUIDs;
    }

    /**
     * Returns the channels this {@link ThingType} provides.
     * <p>
     * The returned list is immutable.
     * 
     * @return the channels this Thing type provides (not null, could be empty)
     */
    public List<ChannelDefinition> getChannelDefinitions() {
        return this.channelDefinitions;
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

    @Override
    public int hashCode() {
       return getUID().hashCode();
    }

    @Override
    public String toString() {
        return getUID().toString();
    }

}
