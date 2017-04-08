/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.model.script.actions.ThingAction;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

public class ThingActionService implements ActionService {

    private static ThingRegistry thingRegistry;

    @Override
    public String getActionClassName() {
        return ThingAction.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return ThingAction.class;
    }

    public void activate() {
    }

    public void deactivate() {
        // deallocate Resources here that are no longer needed and
        // should be reset when activating this binding again
    }

    public void setThingRegistry(ThingRegistry thingRegistry) {
        ThingActionService.thingRegistry = thingRegistry;
    }

    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        ThingActionService.thingRegistry = null;
    }

    public static ThingStatusInfo getThingStatusInfo(String thingUid) {
        ThingUID uid = new ThingUID(thingUid);
        Thing thing = thingRegistry.get(uid);

        if (thing != null) {
            return thing.getStatusInfo();
        } else {
            return null;
        }
    }

}
