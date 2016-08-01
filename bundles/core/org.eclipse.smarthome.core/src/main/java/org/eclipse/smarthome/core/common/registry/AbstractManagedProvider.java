/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

import java.util.Collection;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * {@link AbstractManagedProvider} is an abstract implementation for the {@link ManagedProvider} interface and can be
 * used as base class for {@link ManagedProvider} implementations. It uses the {@link StorageService} to persist the
 * elements.
 *
 * <p>
 * It provides the possibility to transform the element into another java class, that can be persisted. This is needed,
 * if the original element class is not directly persistable. If the element type can be persisted directly the
 * {@link DefaultAbstractManagedProvider} can be used as base class.
 * </p>
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the element
 * @param <K>
 *            type of the element key
 * @param <PE>
 *            type of the persistable element
 */
public abstract class AbstractManagedProvider<E, K, PE> extends AbstractProvider<E> implements ManagedProvider<E, K> {

    private Storage<PE> storage;
    protected final Logger logger = LoggerFactory.getLogger(AbstractManagedProvider.class);

    @Override
    public void add(E element) {

        if (element == null) {
            throw new IllegalArgumentException("Cannot add null element");
        }

        String keyAsString = getKeyAsString(element);
        if (storage.get(keyAsString) != null) {
            throw new IllegalArgumentException("Cannot add element, because an element with same UID (" + keyAsString
                    + ") already exists.");
        }

        storage.put(keyAsString, toPersistableElement(element));
        notifyListenersAboutAddedElement(element);
        logger.debug("Added new element {} to {}.", keyAsString, this.getClass().getSimpleName());
    }

    @Override
    public Collection<E> getAll() {
        final Function<String, E> toElementList = new Function<String, E>() {
            @Override
            public E apply(String elementKey) {
                PE persistableElement = storage.get(elementKey);
                if (persistableElement != null) {
                    return toElement(elementKey, persistableElement);
                } else {
                    return null;
                }
            }
        };

        Collection<String> keys = storage.getKeys();
        Collection<E> elements = Collections2.filter(Collections2.transform(keys, toElementList), Predicates.notNull());

        return ImmutableList.copyOf(elements);
    }

    @Override
    public E get(K key) {

        if (key == null) {
            throw new IllegalArgumentException("Cannot get null element");
        }

        String keyAsString = keyToString(key);

        PE persistableElement = storage.get(keyAsString);
        if (persistableElement != null) {
            return toElement(keyAsString, persistableElement);
        } else {
            return null;
        }
    }

    @Override
    public E remove(K key) {

        if (key == null) {
            throw new IllegalArgumentException("Cannot remove null element");
        }

        String keyAsString = keyToString(key);
        PE persistableElement = storage.remove(keyAsString);
        if (persistableElement != null) {
            E element = toElement(keyAsString, persistableElement);
            if (element != null) {
                notifyListenersAboutRemovedElement(element);
                logger.debug("Removed element {} from {}.", keyAsString, this.getClass().getSimpleName());
                return element;
            }
        }

        return null;
    }

    @Override
    public E update(E element) {

        if (element == null) {
            throw new IllegalArgumentException("Cannot update null element");
        }

        String key = getKeyAsString(element);
        if (storage.get(key) != null) {
            PE persistableElement = storage.put(key, toPersistableElement(element));
            E oldElement = toElement(key, persistableElement);
            notifyListenersAboutUpdatedElement(oldElement, element);
            logger.debug("Updated element {} in {}.", key, this.getClass().getSimpleName());
            return oldElement;
        } else {
            logger.warn("Could not update element with key {} in {}, because it does not exists.", key, this.getClass()
                    .getSimpleName());
        }

        return null;
    }

    private String getKeyAsString(E element) {
        return keyToString(getKey(element));
    }

    /**
     * Returns the key for a given element
     *
     * @param element
     *            element
     * @return key (must not be null)
     */
    protected abstract K getKey(E element);

    /**
     * Returns the name of storage, that is used to persist the elements.
     *
     * @return name of the storage
     */
    protected abstract String getStorageName();

    /**
     * Transforms the key into a string representation.
     *
     * @param key
     *            key
     * @return string representation of the key
     */
    protected abstract String keyToString(K key);

    protected void setStorageService(StorageService storageService) {
        this.storage = storageService.getStorage(getStorageName(), this.getClass().getClassLoader());
    }

    /**
     * Converts the persistable element into the original element.
     *
     * @param key key
     * @param persistableElement
     *            persistable element
     * @return original element
     */
    protected abstract E toElement(String key, PE persistableElement);

    /**
     * Converts the original element into an element that can be persisted.
     *
     * @param element
     *            original element
     * @return persistable element
     */
    protected abstract PE toPersistableElement(E element);

    protected void unsetStorageService(StorageService storageService) {
        this.storage = null;
    }

}
