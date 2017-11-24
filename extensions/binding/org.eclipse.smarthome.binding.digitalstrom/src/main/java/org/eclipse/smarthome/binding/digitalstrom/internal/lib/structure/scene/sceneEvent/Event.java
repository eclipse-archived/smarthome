/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
