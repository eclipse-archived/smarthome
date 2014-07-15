/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.net.URI;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.core.thing.UID;


/**
 * The {@link AbstractDescriptionType} class is the base class for a {@link ThingType},
 * a {@link BridgeType} or a {@link ChannelType}. This class contains only properties
 * and methods accessing them.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public abstract class AbstractDescriptionType {

    private UID uid;
    private String label;
    private String description;
    private URI configDescriptionURI;


    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param uid the unique identifier which identifies the according type within
     *     the overall system (must neither be null, nor empty)
     * 
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     * 
     * @param description the human readable description for the according type
     *     (could be null or empty)
     * 
     * @param configDescriptionURI the link to a concrete ConfigDescription (could be null)
     * 
     * @throws IllegalArgumentException if the UID is null or empty,
     *     or the the meta information is null
     */
    public AbstractDescriptionType(UID uid, String label, String description,
            URI configDescriptionURI) throws IllegalArgumentException {

        if (uid == null) {
            throw new IllegalArgumentException("The UID must not be null");
        }

        if ((label == null) || (label.isEmpty())) {
            throw new IllegalArgumentException("The label must neither be null nor empty!");
        }

        this.uid = uid;
        this.label = label;
        this.description = description;
        this.configDescriptionURI = configDescriptionURI;
    }

    /**
     * Returns the unique identifier which identifies the according type within the overall system.
     * 
     * @return the unique identifier which identifies the according type within
     *     the overall system (neither null, nor empty)
     */
    public UID getUID() {
        return this.uid;
    }

    /**
     * Returns the human readable label for the according type.
     * 
     * @return the human readable label for the according type (neither null, nor empty)
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the human readable description for the according type.
     * 
     * @return the human readable description for the according type (could be null or empty)
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns {@code true} if a link to a concrete {@link ConfigDescription} exists,
     * otherwise {@code false}. 
     * 
     * @return true if a link to a concrete ConfigDescription exists, otherwise false
     */
    public boolean hasConfigDescriptionURI() {
        return (this.configDescriptionURI != null);
    }

    /**
     * Returns the link to a concrete {@link ConfigDescription}.
     * 
     * @return the link to a concrete ConfigDescription (could be null)
     */
    public URI getConfigDescriptionURI() {
        return this.configDescriptionURI;
    }

}
