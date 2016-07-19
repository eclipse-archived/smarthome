/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;

/**
 *
 * {@link ManagedItemThingLinkProvider} is responsible for managed {@link ItemThingLink}s at runtime.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ManagedItemThingLinkProvider extends DefaultAbstractManagedProvider<ItemThingLink, String> implements
        ItemThingLinkProvider {

    @Override
    protected String getKey(ItemThingLink element) {
        return element.getID();
    }

    @Override
    protected String getStorageName() {
        return ItemThingLink.class.getName();
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

}
