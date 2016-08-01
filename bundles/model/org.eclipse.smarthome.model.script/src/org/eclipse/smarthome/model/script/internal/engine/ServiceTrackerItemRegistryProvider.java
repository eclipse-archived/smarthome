/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.eclipse.smarthome.model.script.engine.IItemRegistryProvider;

import com.google.inject.Singleton;

/**
 * This class allows guice-enabled classes to have access to the item registry
 * without going through OSGi declarative services.
 * Though it is very handy, this should be rather seen as a workaround - I am not
 * yet clear on how best to combine guice injection and OSGi DS.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Singleton
public class ServiceTrackerItemRegistryProvider implements IItemRegistryProvider {
    @Override
    public ItemRegistry get() {
        ItemRegistry itemRegistry = ScriptServiceUtil.getItemRegistry();
        return itemRegistry;
    }
}