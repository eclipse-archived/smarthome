/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.internal;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.emf.codegen.ecore.templates.edit.ItemProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.AbstractProvider;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataProvider;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Component;

/**
 * This class serves as a provider for all metadata that is found within item files.
 * It is filled with content by the {@link GenericItemProvider}, which cannot itself implement the
 * {@link MetadataProvider} interface as it already implements {@link ItemProvider}, which would lead to duplicate
 * methods.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
@Component(service = { MetadataProvider.class, GenericMetadataProvider.class })
public class GenericMetadataProvider extends AbstractProvider<Metadata> implements MetadataProvider {

    private final Map<MetadataKey, Metadata> metadata = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Adds metadata to this provider or overrides it if already available.
     *
     * @param bindingType
     * @param itemName
     * @param configuration
     */
    public void addMetadata(String bindingType, String itemName, String value,
            @Nullable Map<String, Object> configuration) {
        MetadataKey key = new MetadataKey(bindingType, itemName);
        Metadata md = new Metadata(key, value, configuration);
        Metadata previous = null;
        try {
            lock.writeLock().lock();
            previous = metadata.remove(key);
            metadata.put(key, md);
        } finally {
            lock.writeLock().unlock();
        }
        if (previous != null) {
            notifyListenersAboutUpdatedElement(previous, md);
        } else {
            notifyListenersAboutAddedElement(md);
        }
    }

    /**
     * Removes metadata from this provider
     *
     * @param bindingType
     * @param itemName
     */
    public void removeMetadata(String bindingType, String itemName) {
        MetadataKey key = new MetadataKey(bindingType, itemName);
        Metadata previous = null;
        try {
            lock.writeLock().lock();
            previous = metadata.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
        if (previous != null) {
            notifyListenersAboutRemovedElement(previous);
        }
    }

    /**
     * Removes all meta-data for a given item name
     *
     * @param itemName
     */
    public void removeMetadata(String itemName) {
        Set<MetadataKey> toBeRemoved;
        Set<Metadata> removed = new HashSet<>();
        try {
            lock.writeLock().lock();
            toBeRemoved = metadata.keySet().stream() //
                    .filter(key -> key.getItemName().equals(itemName)) //
                    .filter(key -> !key.getNamespace().startsWith(MetadataRegistry.INTERNAL_NAMESPACE_PREFIX)) //
                    .collect(toSet());
            toBeRemoved.forEach(key -> {
                removed.add(metadata.remove(key));
            });
        } finally {
            lock.writeLock().unlock();
        }
        for (Metadata m : removed) {
            notifyListenersAboutRemovedElement(m);
        }
    }

    @Override
    public Collection<Metadata> getAll() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableCollection(metadata.values());
        } finally {
            lock.readLock().unlock();
        }
    }

}
