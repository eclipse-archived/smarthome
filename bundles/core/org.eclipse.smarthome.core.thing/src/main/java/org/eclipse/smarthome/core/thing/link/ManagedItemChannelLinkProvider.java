/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 *
 * {@link ManagedItemChannelLinkProvider} is responsible for managed {@link ItemChannelLink}s at runtime.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
public class ManagedItemChannelLinkProvider extends DefaultAbstractManagedProvider<ItemChannelLink, String> implements
        ItemChannelLinkProvider {

    @Override
    protected String getStorageName() {
        return ItemChannelLink.class.getName();
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected String getKey(ItemChannelLink element) {
        return element.getID();
    }

    public void removeLinksForThing(ThingUID thingUID) {
        Collection<ItemChannelLink> itemChannelLinks = getAll();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            if (itemChannelLink.getUID().getThingUID().equals(thingUID)) {
                this.remove(itemChannelLink.getID());
            }
        }
    }

}
