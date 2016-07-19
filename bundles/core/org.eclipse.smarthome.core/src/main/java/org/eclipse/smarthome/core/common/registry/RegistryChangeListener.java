/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

/**
 * {@link RegistryChangeListener} can be added to {@link Registry} services, to
 * listen for changes.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the element in the registry
 */
public interface RegistryChangeListener<E> {

    /**
     * Notifies the listener that a single element has been added.
     *
     * @param element
     *            the element that has been added
     */
    void added(E element);

    /**
     * Notifies the listener that a single element has been removed.
     *
     * @param element
     *            the element that has been removed
     */
    void removed(E element);

    /**
     * Notifies the listener that a single element has been updated.
     *
     * @param element
     *            the element that has been update
     */
    void updated(E oldElement, E element);

}
