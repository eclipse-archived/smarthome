/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

/**
 * {@link ProviderChangeListener} can be added to {@link Provider} services, to
 * listen for changes. The {@link AbstractRegistry} implements a {@link ProviderChangeListener} and subscribes itself to
 * every added {@link Provider}.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the element from the provider
 */
public interface ProviderChangeListener<E> {

    /**
     * Notifies the listener that a single element has been added.
     *
     * @param provider
     *            element provider
     * @param element
     *            the element that has been added
     */
    void added(Provider<E> provider, E element);

    /**
     * Notifies the listener that a single element has been removed.
     *
     * @param provider
     *            element provider
     * @param element
     *            the element that has been removed
     */
    void removed(Provider<E> provider, E element);

    /**
     * Notifies the listener that a single element has been updated.
     *
     * @param provider
     *            element provider
     * @param element
     *            the element that has been update
     */
    void updated(Provider<E> provider, E oldelement, E element);

}
