/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.ManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;

/**
 * {@link ManagedMetadataProvider} is an OSGi service interface that allows to add or remove
 * metadata for items at runtime. Persistence of added metadata is handled by
 * a {@link StorageService}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface ManagedMetadataProvider extends ManagedProvider<Metadata, MetadataKey>, MetadataProvider {

    /**
     * Removes all metadata of a given item
     *
     * @param itemname the name of the item for which the metadata is to be removed.
     */
    void removeItemMetadata(@NonNull String name);

}
