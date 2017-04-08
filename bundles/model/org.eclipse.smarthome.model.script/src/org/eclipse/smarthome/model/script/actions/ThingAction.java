/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.actions;

import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.model.script.internal.engine.action.ThingActionService;

/**
 * This class provides static methods that can be used in automation rules for
 * getting thing's status info.
 *
 * @author Maoliang Huang
 *
 */
public class ThingAction {
    /**
     * Get the thing's status info
     *
     * @param thingUid The uid of the thing
     * @return <code>ThingStatusInfo</code>
     */
    public static ThingStatusInfo getThingStatusInfo(String thingUid) {
        return ThingActionService.getThingStatusInfo(thingUid);
    }
}