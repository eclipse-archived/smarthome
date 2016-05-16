/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.thing.UID;

/**
 * {@link AbstractLinkRegistry} is an abstract class for link based registries,
 * which handle {@link AbstractLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <L>
 *            Concrete type of the abstract link
 */
public abstract class AbstractLinkRegistry<L extends AbstractLink> extends AbstractRegistry<L, String> {

    /**
     * Returns if an item for a given item name is linked to a channel or thing for a
     * given UID.
     *
     * @param itemName
     *            item name
     * @param uid
     *            UID
     * @return true if linked, false otherwise
     */
    public boolean isLinked(String itemName, UID uid) {

        for (AbstractLink link : getAll()) {
            if (link.getUID().equals(uid) && link.getItemName().equals(itemName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the item name, which is bound to the given UID.
     *
     * @param uid
     *            UID
     * @return item name or null if no item is bound to the given UID
     */
    public Set<String> getLinkedItems(UID uid) {
        Set<String> linkedItems = new LinkedHashSet<>();
        for (AbstractLink link : getAll()) {
            if (link.getUID().equals(uid)) {
                linkedItems.add(link.getItemName());
            }
        }
        return linkedItems;
    }

    /**
     * Returns all links for a given UID.
     *
     * @param uid a channel UID
     * @return a set of links for the given UID
     */
    public Set<L> getLinks(UID uid) {
        Set<L> links = new LinkedHashSet<>();
        for (L link : getAll()) {
            if (link.getUID().equals(uid)) {
                links.add(link);
            }
        }
        return links;
    }

    @Override
    public L get(String key) {
        Collection<L> links = getAll();
        for (L link : links) {
            if (link.getID().equals(key)) {
                return link;
            }
        }
        return null;
    }
}
