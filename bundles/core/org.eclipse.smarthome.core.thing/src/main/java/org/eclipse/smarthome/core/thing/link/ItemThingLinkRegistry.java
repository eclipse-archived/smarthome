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
 * @deprecated This class has been added in order to provide backward compatability for smarthome application. Pls use
 *             the qivicon specific service.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Deprecated
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

    /**
     * Channels can not be updated, so this methods throws an {@link UnsupportedOperationException}.
     */
    @Override
    public ItemThingLink update(ItemThingLink element) {
        throw new UnsupportedOperationException("Channels can not be updated.");
    }

    @Override
    protected void notifyListenersAboutAddedElement(ItemThingLink element) {
        super.notifyListenersAboutAddedElement(element);
    }

    @Override
    protected void notifyListenersAboutRemovedElement(ItemThingLink element) {
        super.notifyListenersAboutRemovedElement(element);
    }

    @Override
    protected void notifyListenersAboutUpdatedElement(ItemThingLink oldElement, ItemThingLink element) {
        super.notifyListenersAboutUpdatedElement(oldElement, element);
        // it is not needed to send an event, because links can not be updated
    }

}
