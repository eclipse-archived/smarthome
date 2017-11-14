/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.sceneEvent;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.EventPropertyEnum;

/**
 * The {@link EventItem} represents a event item of an digitalSTROM-Event.
 *
 * @author Alexander Betker
 */
public interface EventItem {

    /**
     * Returns the name of this {@link EventItem}.
     *
     * @return name of this {@link EventItem}
     */
    public String getName();

    /**
     * Returns {@link HashMap} with the properties of this {@link EventItem}.
     * The key is a {@link EventPropertyEnum} and represents the property name
     * and the value is the property value.
     *
     * @return the properties of this {@link EventItem}
     */
    public Map<EventPropertyEnum, String> getProperties();
}
