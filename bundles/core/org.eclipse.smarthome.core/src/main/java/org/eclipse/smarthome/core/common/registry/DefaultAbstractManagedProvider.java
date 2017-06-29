/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

/**
 * {@link DefaultAbstractManagedProvider} is a specific {@link AbstractManagedProvider} implementation, where the stored
 * element is
 * the same as the element of the provider. So no transformation is needed.
 * Therefore only two generic parameters are needed instead of three.
 *
 * @author Dennis Nobel - Initial contribution
 *
 * @param <E>
 *            type of the element
 * @param <K>
 *            type of the element key
 */
public abstract class DefaultAbstractManagedProvider<E extends Identifiable<K>, K>
        extends AbstractManagedProvider<E, K, E> {

    @Override
    protected E toElement(String key, E element) {
        return element;
    }

    @Override
    protected E toPersistableElement(E element) {
        return element;
    }

}
