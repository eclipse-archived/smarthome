/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * {@link ItemThingLinkRegistry} tracks all {@link ItemThingLinkProvider}s and
 * aggregates all {@link ItemThingLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemThingLinkRegistry extends AbstractLinkRegistry<ItemThingLink> {

    /**
     * Returns the list of linked thing UIDs, which are linked to the given item name.
     *
     * @param itemName
     *            item name
     * @return list of linked thing UIDs or an empty list of no thing is linked to the item
     */
    public Set<ThingUID> getLinkedThings(String itemName) {
        Set<ThingUID> linkedThings = new LinkedHashSet<>();
        for (ItemThingLink link : getAll()) {
            if (link.getItemName().equals(itemName)) {
                linkedThings.add(link.getUID());
            }
        }
        return linkedThings;
    }
}
