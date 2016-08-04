/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link ThingTypeMigrationService} describes a service to change the thing type
 * of a given {@link Thing}.
 * 
 * @author Andre Fuechsel - initial contribution
 */
public interface ThingTypeMigrationService {

    /**
     * Changes the type of a given {@link Thing}.
     * 
     * @param thing {@link Thing} whose type should be changed
     * @param thingTypeUID new {@link ThingTypeUID}
     * @param configuration new configuration
     * 
     * @throws RuntimeException, if the new thing type is not registered in the registry
     */
    void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration);

}
