/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link ChannelGroupType} contains a list of {@link ChannelDefinition}s and further
 * meta information such as label and description, which are generally used by user interfaces.
 * <p>
 * This type can be used for Things which offers multiple functionalities which belong all together.
 *
 * @author Dennis Nobel - Initial Contribution
 * @author Michael Grammling - Initial Contribution
 */
public class ChannelGroupType extends AbstractDescriptionType {

    private final boolean advanced;
    private final List<ChannelDefinition> channelDefinitions;

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param uid the unique identifier which identifies this channel group type within the
     *            overall system (must neither be null, nor empty)
     *
     * @param advanced true if this channel group type contains advanced features, otherwise false
     *
     * @param label the human readable label for the according type
     *            (must neither be null nor empty)
     *
     * @param description the human readable description for the according type
     *            (could be null or empty)
     *
     * @param channelDefinitions the channel definitions this channel group forms
     *            (could be null or empty)
     *
     * @throws IllegalArgumentException if the UID is null, or the label is null or empty
     */
    public ChannelGroupType(ChannelGroupTypeUID uid, boolean advanced, String label, String description,
            List<ChannelDefinition> channelDefinitions) throws IllegalArgumentException {

        super(uid, label, description);

        this.advanced = advanced;

        if (channelDefinitions != null) {
            this.channelDefinitions = Collections.unmodifiableList(channelDefinitions);
        } else {
            this.channelDefinitions = Collections.unmodifiableList(new ArrayList<ChannelDefinition>(0));
        }
    }

    /**
     * Returns {@code true} if this {@link ChannelGroupType} contains advanced functionalities
     * which should be typically not shown in the basic view of user interfaces,
     * otherwise {@code false}.
     * 
     * @return true if this channel group contains advanced functionalities, otherwise false
     */
    public boolean isAdvanced() {
        return advanced;
    }

    /**
     * Returns the channel definitions this {@link ChannelGroupType} provides.
     * <p>
     * The returned list is immutable.
     *
     * @return the channels this Thing type provides (not null, could be empty)
     */
    public List<ChannelDefinition> getChannelDefinitions() {
        return channelDefinitions;
    }

    @Override
    public ChannelGroupTypeUID getUID() {
        return (ChannelGroupTypeUID) super.getUID();
    }

    @Override
    public String toString() {
        return super.getUID().toString();
    }

}
