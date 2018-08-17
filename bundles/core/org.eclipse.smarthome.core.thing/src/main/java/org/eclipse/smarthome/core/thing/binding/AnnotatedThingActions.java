/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.binding;

/**
 *
 * Marker interface for a service that provides automation actions via annotated methods
 *
 * @author Stefan Triller - initial contribution
 *
 */
public interface AnnotatedThingActions {

    public static final String ACTION_THING_UID = "esh.thingActionID";

    /**
     * Sets the ThingHandler on which the actions (methods) should be called
     *
     * @param handler the {@link ThingHandler}
     */
    void setThingHandler(ThingHandler handler);

    /**
     * Gets the ThingHandler on which the actions (methods) should be called
     *
     * @return the {@link ThingHandler}
     */
    ThingHandler getThingHandler();

}
