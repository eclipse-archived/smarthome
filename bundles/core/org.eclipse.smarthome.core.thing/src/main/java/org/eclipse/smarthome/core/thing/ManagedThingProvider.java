/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.common.registry.DefaultAbstractManagedProvider;

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
public class ManagedThingProvider extends DefaultAbstractManagedProvider<Thing, ThingUID> implements ThingProvider {

    @Override
    protected ThingUID getKey(Thing thing) {
        return thing.getUID();
    }

    @Override
    protected String getStorageName() {
        return Thing.class.getName();
    }

    @Override
    protected String keyToString(ThingUID key) {
        return key.toString();
    }

}
