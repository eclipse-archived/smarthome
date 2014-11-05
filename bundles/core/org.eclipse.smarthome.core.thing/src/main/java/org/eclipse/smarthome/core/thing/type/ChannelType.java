/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Channel;


/**
 * The {@link ChannelType} describes a concrete type of a {@link Channel}.
 * <p>
 * This description is used as template definition for the creation
 * of the according concrete {@link Channel} object.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class ChannelType extends AbstractDescriptionType {

    private String itemType;
    private Set<String> tags;


    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uid the unique identifier which identifies this Channel type within
     *     the overall system (must neither be null, nor empty)
     *
     * @param itemType the item type of this Channel type, e.g. {@code ColorItem}
     *     (must neither be null nor empty)
     *
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     *
     * @param description the human readable description for the according type
     *     (could be null or empty)
     *
     * @param tags all tags of this {@link ChannelType}, e.g. {@code Alarm}
     *     (could be null or empty)
     *
     * @param configDescriptionURI the link to the concrete ConfigDescription (could be null)
     *
     * @throws IllegalArgumentException if the UID or the item type is null or empty,
     *     or the the meta information is null
     */
    public ChannelType(ChannelTypeUID uid, String itemType, String label, String description,
            Set<String> tags, URI configDescriptionURI) throws IllegalArgumentException {

        super(uid, label, description, configDescriptionURI);

        if ((itemType == null) || (itemType.isEmpty())) {
            throw new IllegalArgumentException("The item type must neither be null nor empty!");
        }

        this.itemType = itemType;

        if (tags != null) {
            this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
        } else {
            this.tags = Collections.unmodifiableSet(new HashSet<String>(0));
        }
    }

    @Override
    public ChannelTypeUID getUID() {
        return (ChannelTypeUID) super.getUID();
    }

    /**
     * Returns the item type of this {@link ChannelType}, e.g. {@code ColorItem}.
     * 
     * @return the item type of this Channel type, e.g. {@code ColorItem} (neither null nor empty)
     */
    public String getItemType() {
        return this.itemType;
    }

    /**
     * Returns all tags of this {@link ChannelType}, e.g. {@code Alarm}.
     * 
     * @return all tags of this Channel type, e.g. {@code Alarm} (not null, could be empty)
     */
    public Set<String> getTags() {
        return this.tags;
    }

    @Override
    public String toString() {
        return super.getUID().toString();
    }

}
