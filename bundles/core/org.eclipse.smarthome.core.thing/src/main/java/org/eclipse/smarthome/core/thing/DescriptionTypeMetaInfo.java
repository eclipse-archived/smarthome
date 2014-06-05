/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;


/**
 * The {@link DescriptionTypeMetaInfo} contains meta information about a
 * {@link org.eclipse.smarthome.core.thing.ThingType},
 * {@link BridgeType} or a {@link ChannelType}. This class contains human
 * readable text information about the according types.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class DescriptionTypeMetaInfo {

    private String label;
    private String description;


    /**
     * Creates a new instance of this class with the specified parameters.
     * 
     * @param label the human readable label for the according type
     *     (must neither be null nor empty)
     * 
     * @param description the human readable description for the according type
     *     (must neither be null nor empty)
     * 
     * @throws IllegalArgumentException if any of the parameters is null or empty
     */
    public DescriptionTypeMetaInfo(String label, String description)
            throws IllegalArgumentException {

        if ((label == null) || (label.isEmpty())) {
            throw new IllegalArgumentException("The label must neither be null nor empty!");
        }

        if ((description == null) || (description.isEmpty())) {
            throw new IllegalArgumentException("The description must neither be null nor empty!");
        }

        this.label = label;
        this.description = description;
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
     * @return the human readable description for the according type (neither null, nor empty)
     */
    public String getDescription() {
        return this.description;
    }

}
