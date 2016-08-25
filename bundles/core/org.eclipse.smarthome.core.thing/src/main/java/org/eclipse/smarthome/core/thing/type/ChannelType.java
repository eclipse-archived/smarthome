/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.EventDescription;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * The {@link ChannelType} describes a concrete type of a {@link Channel}.
 * <p>
 * This description is used as template definition for the creation of the according concrete {@link Channel} object.
 * <p>
 * <b>Hint:</b> This class is immutable.
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ChannelType extends AbstractDescriptionType {

    private final boolean advanced;
    private final String itemType;
    private final String triggerType;
    private final Set<String> tags;
    private final String category;
    private final StateDescription state;
    private final EventDescription event;
    private final URI configDescriptionURI;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uid the unique identifier which identifies this Channel type within
     *            the overall system (must neither be null, nor empty)
     *
     * @param advanced true if this channel type contains advanced features, otherwise false
     *
     * @param itemType the item type of this Channel type, e.g. {@code ColorItem} (must neither be null nor empty)
     *
     * @param label the human readable label for the according type
     *            (must neither be null nor empty)
     *
     * @param description the human readable description for the according type
     *            (could be null or empty)
     *
     * @param category the category of this Channel type, e.g. {@code TEMPERATURE} (could be null or empty)
     *
     * @param tags all tags of this {@link ChannelType}, e.g. {@code Alarm} (could be null or empty)
     *
     * @param state the restrictions of an item state which gives information how to interpret it
     *            (could be null)
     *
     * @param configDescriptionURI the link to the concrete ConfigDescription (could be null)
     *
     * @throws IllegalArgumentException if the UID or the item type is null or empty,
     *             or the the meta information is null
     */
    public ChannelType(ChannelTypeUID uid, boolean advanced, String itemType, String label, String description,
            String category, Set<String> tags, StateDescription state, URI configDescriptionURI) {
        this(uid, advanced, itemType, null, label, description, category, tags, state, null, configDescriptionURI);

        if ((itemType == null) || (itemType.isEmpty())) {
            throw new IllegalArgumentException("The item type must neither be null nor empty!");
        }
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uid the unique identifier which identifies this Channel type within
     *            the overall system (must neither be null, nor empty)
     *
     * @param advanced true if this channel type contains advanced features, otherwise false
     *
     * @param itemType the item type of this Channel type, e.g. {@code ColorItem}
     *
     * @param triggerType the trigger type of this Channel type
     *
     * @param label the human readable label for the according type
     *            (must neither be null nor empty)
     *
     * @param description the human readable description for the according type
     *            (could be null or empty)
     *
     * @param category the category of this Channel type, e.g. {@code TEMPERATURE} (could be null or empty)
     *
     * @param tags all tags of this {@link ChannelType}, e.g. {@code Alarm} (could be null or empty)
     *
     * @param state the restrictions of an item state which gives information how to interpret it
     *            (could be null)
     *
     * @param configDescriptionURI the link to the concrete ConfigDescription (could be null)
     *
     * @throws IllegalArgumentException if the UID or the item type is null or empty,
     *             or the the meta information is null
     */
    public ChannelType(ChannelTypeUID uid, boolean advanced, String itemType, String triggerType, String label,
            String description, String category, Set<String> tags, StateDescription state, EventDescription event,
            URI configDescriptionURI) throws IllegalArgumentException {

        super(uid, label, description);

        if ((itemType == null || itemType.isEmpty()) && (triggerType == null || triggerType.isEmpty())) {
            throw new IllegalArgumentException("Either the item type or the trigger type must be set!");
        }

        this.itemType = itemType;
        this.triggerType = triggerType;
        this.configDescriptionURI = configDescriptionURI;

        if (tags != null) {
            this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
        } else {
            this.tags = Collections.unmodifiableSet(new HashSet<String>(0));
        }

        this.advanced = advanced;
        this.category = category;
        this.state = state;
        this.event = event;
    }

    @Override
    public ChannelTypeUID getUID() {
        return (ChannelTypeUID) super.getUID();
    }

    /**
     * Returns the item type of this {@link ChannelType}, e.g. {@code ColorItem}.
     *
     * @return the item type of this Channel type, e.g. {@code ColorItem}. Can be null if the channel is a trigger channel.
     */
    public String getItemType() {
        return this.itemType;
    }

    /**
     * Returns the trigger type of this {@link ChannelType}, e.g. {@code IncreaseDecreaseType}.
     *
     * @return the trigger type of this Channel type, e.g. {@code IncreaseDecreaseType}. Can be null if the channel is not a trigger channel.
     */
    public String getTriggerType() {
        return triggerType;
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

    /**
     * Returns the link to a concrete {@link ConfigDescription}.
     *
     * @return the link to a concrete ConfigDescription (could be null)
     */
    public URI getConfigDescriptionURI() {
        return this.configDescriptionURI;
    }

    /**
     * Returns the restrictions of an item state which gives information how to interpret it.
     *
     * @return the restriction of an item state which gives information how to interpret it
     *         (could be null)
     */
    public StateDescription getState() {
        return state;
    }

    /**
     * Returns informations about the supported events.
     *
     * @return the event information
     *         (could be null)
     */
    public EventDescription getEvent() {
        return event;
    }

    /**
     * Returns {@code true} if this channel type contains advanced functionalities
     * which should be typically not shown in the basic view of user interfaces,
     * otherwise {@code false}.
     *
     * @return true if this channel type contains advanced functionalities, otherwise false
     */
    public boolean isAdvanced() {
        return advanced;
    }

    /**
     * Returns the category of this {@link ChannelType}, e.g. {@code TEMPERATURE}.
     *
     * @return the category of this Channel type, e.g. {@code TEMPERATURE} (could be null or empty)
     */
    public String getCategory() {
        return category;
    }

}
