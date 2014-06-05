/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.config.core.ConfigDescription;


/**
 * The {@link AbstractDescriptionType} class is the base class for a
 * {@link org.eclipse.smarthome.core.thing.ThingType}, a
 * {@link BridgeType} or a {@link ChannelType}. This class contains only
 * properties and methods accessing them.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public abstract class AbstractDescriptionType {

    private UID uid;
    private DescriptionTypeMetaInfo metaInfo;
    private String configDescriptionURI;

    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param uid the unique identifier which identifies the according type within
     *     the overall system (must neither be null, nor empty)
     * 
     * @param metaInfo the meta information containing human readable text
     *     of the according type (must not be null)
     * 
     * @param configDescriptionURI the link to a concrete ConfigDescription (could be null)
     * 
     * @throws IllegalArgumentException if the UID is null or empty,
     *     or the the meta information is null
     */
    public AbstractDescriptionType(UID uid, DescriptionTypeMetaInfo metaInfo,
            String configDescriptionURI) throws IllegalArgumentException {

        if (uid == null) {
            throw new IllegalArgumentException("The UID must not be null");
        }

        if (metaInfo == null) {
            throw new IllegalArgumentException("The meta information must not be null!");
        }

        this.uid = uid;
        this.metaInfo = metaInfo;
        this.configDescriptionURI = configDescriptionURI;
    }

    /**
     * Returns the unique identifier which identifies the according type within
     * the overall system.
     * 
     * @return the unique identifier which identifies the according type within
     *     the overall system (neither null, nor empty)
     */
    public UID getUID() {
        return this.uid;
    }

    /**
     * Returns the meta information containing human readable text of the according type.
     * 
     * @return the meta information containing human readable text
     *     of the according type (not null)
     */
    public DescriptionTypeMetaInfo getMetaInfo() {
        return this.metaInfo;
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
    public String getConfigDescriptionURI() {
        return this.configDescriptionURI;
    }

}
