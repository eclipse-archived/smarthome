/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * {@link ManagedThingProvider} is an OSGi service, that allows to add or remove
 * things at runtime by calling {@link ManagedThingProvider#addThing(Thing)} or
 * {@link ManagedThingProvider#removeThing(Thing)}. An added thing is
 * automatically exposed to the {@link ThingRegistry}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Dennis Nobel - Integrated Storage
 * @author Michael Grammling - Added dynamic configuration update
 */
@Component(immediate = true, service = { ThingProvider.class, ManagedThingProvider.class })
public class ManagedThingProvider extends DefaultAbstractManagedProvider<Thing, ThingUID> implements ThingProvider {

    @Override
    protected String getStorageName() {
        return Thing.class.getName();
    }

    @Override
    protected String keyToString(ThingUID key) {
        return key.toString();
    }

    @Reference(policy = ReferencePolicy.DYNAMIC)
    @Override
    protected void setStorageService(StorageService storageService) {
        super.setStorageService(storageService);
    }

    @Override
    protected void unsetStorageService(StorageService storageService) {
        super.unsetStorageService(storageService);
    }

}
