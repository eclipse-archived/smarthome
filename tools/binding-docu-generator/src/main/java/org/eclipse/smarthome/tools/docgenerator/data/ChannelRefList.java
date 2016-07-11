/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.ChannelRef;

public class ChannelRefList extends ModelList {
    /**
     * @return Returns a new {@link ChannelRef} object.
     */
    @Override
    public ChannelRef getNewModel() {
        return new ChannelRef();
    }
}
