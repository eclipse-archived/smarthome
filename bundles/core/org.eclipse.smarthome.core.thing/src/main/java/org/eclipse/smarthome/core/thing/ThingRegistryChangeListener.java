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
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;

/**
 * {@link ThingRegistryChangeListener} can be implemented to listen for things
 * beeing added or removed. The listener must be added and removed via
 * {@link ThingRegistry#addThingRegistryChangeListener(ThingRegistryChangeListener)} and
 * {@link ThingRegistry#removeThingRegistryChangeListener(ThingRegistryChangeListener)}.
 *
 * @author Dennis Nobel - Initial Contribution
 * @author Michael Grammling - Added dynamic configuration update
 *
 * @see ThingRegistry
 */
public interface ThingRegistryChangeListener extends RegistryChangeListener<Thing> {

}
