/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
