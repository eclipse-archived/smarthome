/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.provider.util;

import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class is responsible for custom serialization and deserialization of the {@link ModuleType}s. It is necessary
 * for the persistence of the {@link ModuleType}s. Implements {@link Externalizable}.
 * 
 * @author Ana Dimova - Initial Contribution
 * @param <T> is one of {@link TriggerType}, {@link CompositeTriggerType}, {@link ConditionType},
 *            {@link CompositeConditionType}, {@link ActionType} or {@link CompositeActionType}.
 * 
 */
public class PersistableModuleType {

    /**
     * This constructor is used for deserialization of the {@link ModuleType}s.
     * 
     */
    public PersistableModuleType() {
    }

}
