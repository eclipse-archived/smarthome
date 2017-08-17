/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * {@link ManagedItemThingLinkProvider} is responsible for managed {@link ItemThingLink}s at runtime.
 *
 * @author Dennis Nobel - Initial contribution
 *
 */
@Component(immediate = true, service = { ItemThingLinkProvider.class, ManagedItemThingLinkProvider.class })
public class ManagedItemThingLinkProvider extends DefaultAbstractManagedProvider<ItemThingLink, String>
        implements ItemThingLinkProvider {

    @Override
    protected String getStorageName() {
        return ItemThingLink.class.getName();
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Reference
    @Override
    protected void setStorageService(StorageService storageService) {
        super.setStorageService(storageService);
    }

    @Override
    protected void unsetStorageService(StorageService storageService) {
        super.unsetStorageService(storageService);
    }
}
