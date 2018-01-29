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

import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.ConditionType;

import com.google.gson.InstanceCreator;

/**
 * This class creates {@link ConditionType} instances.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class ConditionInstanceCreator implements InstanceCreator<CompositeConditionType> {

    @Override
    public CompositeConditionType createInstance(Type type) {
        return new CompositeConditionType(null, null, null, null);
    }
}
