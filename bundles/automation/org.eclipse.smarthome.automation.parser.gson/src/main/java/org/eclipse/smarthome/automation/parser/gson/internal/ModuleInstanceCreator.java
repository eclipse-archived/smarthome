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
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.lang.reflect.Type;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.ModuleBuilder;

import com.google.gson.InstanceCreator;

/**
 * This class creates {@link Module} instances.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ModuleInstanceCreator implements InstanceCreator<Module> {

    @Override
    public Module createInstance(Type type) {
        if (type.getTypeName().equals(Trigger.class.getName())) {
            return ModuleBuilder.createTrigger().build();
        } else if (type.getTypeName().equals(Condition.class.getName())) {
            return ModuleBuilder.createCondition().build();
        } else if (type.getTypeName().equals(Action.class.getName())) {
            return ModuleBuilder.createAction().build();
        } else {
            throw new IllegalArgumentException("Cannot instantiate type " + type.getTypeName());
        }
    }
}
