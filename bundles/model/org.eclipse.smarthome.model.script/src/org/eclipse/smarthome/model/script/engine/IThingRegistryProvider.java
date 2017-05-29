/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.engine;

import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.model.script.internal.engine.ServiceTrackerThingRegistryProvider;

import com.google.inject.ImplementedBy;
import com.google.inject.Provider;

@ImplementedBy(ServiceTrackerThingRegistryProvider.class)
public interface IThingRegistryProvider extends Provider<ThingRegistry> {

}
