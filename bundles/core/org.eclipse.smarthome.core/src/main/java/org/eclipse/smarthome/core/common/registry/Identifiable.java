/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.registry;

/**
 * Interface for classes that instances provide an identifier.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public interface Identifiable<T> {

    /**
     * Get the unique identifier.
     *
     * @return the unique identifier
     */
    T getUID();
}
