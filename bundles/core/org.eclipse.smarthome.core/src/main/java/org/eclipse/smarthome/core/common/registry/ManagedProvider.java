/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link ManagedProvider} is a specific {@link Provider} that enables to
 * add, remove and update elements at runtime.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the element
 * @param <K>
 *            type of the element key
 */
public interface ManagedProvider<E extends Identifiable<K>, K> extends Provider<E> {

    /**
     * Adds an element.
     *
     * @param element
     *            element to be added
     */
    void add(@NonNull E element);

    /**
     * Removes an element and returns the removed element.
     *
     * @param key
     *            key of the element that should be removed
     * @return element that was removed, or null if no element with the given
     *         key exists
     */
    E remove(@NonNull K key);

    /**
     * Updates an element.
     *
     * @param element
     *            element to be updated
     * @return returns the old element or null if no element with the same key
     *         exists
     */
    E update(@NonNull E element);

    /**
     * Returns an element for the given key or null if no element for the given
     * key exists.
     *
     * @param key
     *            key
     * @return returns element or null, if no element for the given key exists
     */
    E get(K key);

}