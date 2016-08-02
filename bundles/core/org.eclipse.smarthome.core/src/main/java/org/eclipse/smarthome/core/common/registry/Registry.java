/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

import java.util.Collection;

/**
 * The {@link Registry} interface represents a registry for elements of the type
 * E. The concrete subinterfaces are registered as OSGi services.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the elements in the registry
 */
public interface Registry<E, K> {

    /**
     * Adds a {@link RegistryChangeListener} to the registry.
     *
     * @param listener
     *            registry change listener
     */
    void addRegistryChangeListener(RegistryChangeListener<E> listener);

    /**
     * Returns a collection of all elements in the registry.
     *
     * @return collection of all elements in the registry
     */
    Collection<E> getAll();

    /**
     * This method retrieves a single element from the registry.
     *
     * @param key
     *            key of the element
     * @return element or null if no element was found
     */
    public E get(K key);

    /**
     * Removes a {@link RegistryChangeListener} from the registry.
     *
     * @param listener
     *            registry change listener
     */
    void removeRegistryChangeListener(RegistryChangeListener<E> listener);

    /**
     * Adds the given element to the according {@link ManagedProvider}.
     *
     * @param element
     *            element to be added (must not be null)
     * @return the added element or newly created object of the same type
     * @throws IllegalStateException
     *             if no ManagedProvider is available
     */
    public E add(E element);

    /**
     * Updates the given element at the according {@link ManagedProvider}.
     *
     * @param element
     *            element to be updated (must not be null)
     * @return returns the old element or null if no element with the same key
     *         exists
     * @throws IllegalStateException
     *             if no ManagedProvider is available
     */
    public E update(E element);

    /**
     * Removes the given element from the according {@link ManagedProvider}.
     *
     * @param key
     *            key of the element (must not be null)
     * @return element that was removed, or null if no element with the given
     *         key exists
     * @throws IllegalStateException
     *             if no ManagedProvider is available
     */
    public E remove(K key);
}