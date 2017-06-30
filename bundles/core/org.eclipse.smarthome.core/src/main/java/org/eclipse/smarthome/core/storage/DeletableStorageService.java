/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.storage;

/**
 * The {@link DeletableStorageService} extends the normal {@link StorageService} and provides instances of
 * {@link DeletableStorage}s.
 *
 * @author Markus Rathgeb - Initial Contribution and API
 */
public interface DeletableStorageService extends StorageService {

    @Override
    <T> DeletableStorage<T> getStorage(String name);

    @Override
    <T> DeletableStorage<T> getStorage(String name, ClassLoader classLoader);
}
