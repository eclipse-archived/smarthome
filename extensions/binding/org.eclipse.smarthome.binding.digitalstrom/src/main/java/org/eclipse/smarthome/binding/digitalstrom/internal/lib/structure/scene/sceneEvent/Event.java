/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.sceneEvent;

import java.util.List;

/**
 * The {@link Event} represents a digitalSTROM-Event.
 *
 * @author Alexander Betker
 */
public interface Event {

    /**
     * Returns a list of the {@link EventItem}s of this Event.
     *
     * @return List of {@link EventItem}s
     */
    public List<EventItem> getEventItems();
}
