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
package org.eclipse.smarthome.core.thing.link;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.thing.UID;

/**
 * {@link AbstractLinkRegistry} is an abstract class for link based registries,
 * which handle {@link AbstractLink}s.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <L> Concrete type of the abstract link
 */
public abstract class AbstractLinkRegistry<L extends AbstractLink, P extends Provider<L>>
        extends AbstractRegistry<L, String, P> {

    private final ReentrantReadWriteLock toLinkLock = new ReentrantReadWriteLock();
    private final Map<String, Set<L>> itemNameToLink = new HashMap<>();
    private final Map<UID, Set<L>> linkedUidToLink = new HashMap<>();

    protected AbstractLinkRegistry(final Class<P> providerClazz) {
        super(providerClazz);
    }

    @Override
    protected void onAddElement(final L element) {
        toLinkAdded(element);
    }

    @Override
    protected void onRemoveElement(final L element) {
        toLinkRemoved(element);
    }

    @Override
    protected void onUpdateElement(final L oldElement, final L element) {
        toLinkRemoved(oldElement);
        toLinkAdded(element);
    }

    private void toLinkAdded(final L element) {
        final String itemName = element.getItemName();
        final UID linkedUid = element.getLinkedUID();

        toLinkLock.writeLock().lock();
        try {
            Set<L> set;

            set = itemNameToLink.get(itemName);
            if (set == null) {
                set = new HashSet<>();
                itemNameToLink.put(itemName, set);
            }
            set.add(element);

            set = linkedUidToLink.get(linkedUid);
            if (set == null) {
                set = new HashSet<>();
                linkedUidToLink.put(linkedUid, set);
            }
            set.add(element);
        } finally {
            toLinkLock.writeLock().unlock();
        }
    }

    private void toLinkRemoved(final L element) {
        final String itemName = element.getItemName();
        final UID linkedUid = element.getLinkedUID();

        toLinkLock.writeLock().lock();
        try {
            Set<L> set;

            set = itemNameToLink.get(itemName);
            if (set != null) {
                set.remove(element);
                if (set.isEmpty()) {
                    itemNameToLink.remove(itemName);
                }
            }

            set = linkedUidToLink.get(linkedUid);
            if (set != null) {
                set.remove(element);
                if (set.isEmpty()) {
                    linkedUidToLink.remove(linkedUid);
                }
            }
        } finally {
            toLinkLock.writeLock().unlock();
        }
    }

    /**
     * Returns if an item for a given item name is linked to a channel or thing for a
     * given UID.
     *
     * @param itemName item name
     * @param uid UID
     * @return true if linked, false otherwise
     */
    public boolean isLinked(String itemName, UID uid) {
        toLinkLock.readLock().lock();
        try {
            final Set<L> forItemName = itemNameToLink.get(itemName);
            final Set<L> forLinkedUID = linkedUidToLink.get(uid);
            if (forItemName == null || forLinkedUID == null) {
                return false;
            } else {
                return forItemName.parallelStream().anyMatch(forLinkedUID::contains);
            }
        } finally {
            toLinkLock.readLock().unlock();
        }
    }

    /**
     * Returns if a link for the given item name exists.
     *
     * @param itemName item name
     * @return true if a link exists, otherwise false
     */
    public boolean isLinked(final String itemName) {
        toLinkLock.readLock().lock();
        try {
            return itemNameToLink.get(itemName) != null; // if present the set is not empty by definition
        } finally {
            toLinkLock.readLock().unlock();
        }
    }

    /**
     * Returns if a link for the given UID exists.
     *
     * @param uid UID
     * @return true if a link exists, otherwise false
     */
    public boolean isLinked(final UID uid) {
        toLinkLock.readLock().lock();
        try {
            return linkedUidToLink.get(uid) != null; // if present the set is not empty by definition
        } finally {
            toLinkLock.readLock().unlock();
        }
    }

    /**
     * Returns the item names, which are bound to the given UID.
     *
     * @param uid UID
     * @return a non-null collection of item names that are linked to the given UID.
     */
    public Set<String> getLinkedItemNames(UID uid) {
        final Set<String> linkedItems = new LinkedHashSet<>();
        toLinkLock.readLock().lock();
        try {
            final Set<L> forLinkedUID = linkedUidToLink.get(uid);
            if (forLinkedUID != null) {
                forLinkedUID.forEach(link -> linkedItems.add(link.getItemName()));
            }
        } finally {
            toLinkLock.readLock().unlock();
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
        final Set<L> links = new LinkedHashSet<>();
        toLinkLock.readLock().lock();
        try {
            final Set<L> forLinkedUID = linkedUidToLink.get(uid);
            if (forLinkedUID != null) {
                links.addAll(forLinkedUID);
            }
        } finally {
            toLinkLock.readLock().unlock();
        }
        return links;
    }

    /**
     * Returns all links for a given item name.
     *
     * @param itemName the name of the item
     * @return a set of links for the given item name
     */
    public Set<L> getLinks(final String itemName) {
        final Set<L> links = new LinkedHashSet<>();
        toLinkLock.readLock().lock();
        try {
            final Set<L> forLinkedUID = itemNameToLink.get(itemName);
            if (forLinkedUID != null) {
                links.addAll(forLinkedUID);
            }
        } finally {
            toLinkLock.readLock().unlock();
        }
        return links;
    }

}
