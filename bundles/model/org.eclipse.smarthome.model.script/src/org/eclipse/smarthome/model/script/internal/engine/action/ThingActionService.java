/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.model.script.actions.ThingAction;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
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

    @Reference
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
