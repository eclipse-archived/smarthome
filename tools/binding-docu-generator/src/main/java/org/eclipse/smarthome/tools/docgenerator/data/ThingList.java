/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Thing;

public class ThingList extends ModelList {
    /**
     * Returns a new {@link Thing} object.
     *
     * @return
     */
    @Override
    public Thing getNewModel() {
        return new Thing();
    }
}
