/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.smarthome.core.common.registry.Identifiable;

/**
 * A listener to be informed before entities are added respectively after they are removed.
 *
 * @author Simon Kaufmann - initial contribution and API.
 */
public interface RegistryHook<E extends Identifiable<?>> {

    /**
     * Notifies the listener that a single element is going to be added by another provider.
     *
     * @param element the element to be added
     */
    void beforeAdding(E element);

    /**
     * Notifies the listener that a single element was removed by another provider.
     *
     * @param element the element that was removed
     */
    void afterRemoving(E element);

}
