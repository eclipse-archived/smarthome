/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.AbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ManagedMetadataProvider} is an OSGi service, that allows to add or remove
 * metadata for items at runtime. Persistence of added metadata is handled by
 * a {@link StorageService}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(immediate = true, service = { MetadataProvider.class, ManagedMetadataProvider.class })
public class ManagedMetadataProvider extends AbstractManagedProvider<Metadata, MetadataKey, Metadata>
        implements MetadataProvider {

    private final Logger logger = LoggerFactory.getLogger(ManagedMetadataProvider.class);

    @Override
    protected String getStorageName() {
        return Metadata.class.getName();
    }

    @Override
    protected @NonNull String keyToString(@NonNull MetadataKey key) {
        return key.toString();
    }

    @Override
    protected Metadata toElement(@NonNull String key, @NonNull Metadata persistableElement) {
        return persistableElement;
    }

    @Override
    protected Metadata toPersistableElement(Metadata element) {
        return element;
    }

    @Override
    @Reference
    protected void setStorageService(StorageService storageService) {
        super.setStorageService(storageService);
    }

    @Override
    protected void unsetStorageService(StorageService storageService) {
        super.unsetStorageService(storageService);
    }

    /**
     * Removes all metadata of a given item
     *
     * @param itemname the name of the item for which the meta data is to be removed.
     */
    public void removeItemMetadata(@NonNull String name) {
        getAll().stream().filter(MetadataPredicates.ofItem(name)).forEach(md -> remove(md.getUID()));
    }

}
