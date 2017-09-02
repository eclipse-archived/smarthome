/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface ModelRepositoryChangeListener {

    /**
     * Performs dispatch of all binding configs and
     * fires all {@link ItemsChangeListener}s if {@code modelName} ends with "items".
     */
    public void modelChanged(String modelName, EventType type);
}
