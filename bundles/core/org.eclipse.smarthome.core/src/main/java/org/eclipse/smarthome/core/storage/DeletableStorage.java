/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage;

/**
 * A {@code Storage} that could be disposed.
 *
 * @author Markus Rathgeb - Initial Contribution and API
 */
public interface DeletableStorage<T> extends Storage<T> {

    /**
     * Delete the storage.
     *
     * <p>
     * This function could be called if the storage is not longer used (now and in future). The storage implementation
     * will clean up / remove the storage (e.g. file, database, etc.).
     * After this function has been called the storage must not be used anymore.
     */
    void delete();
}
