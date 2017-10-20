/**
* Copyright (c) 2015, 2017 by Bosch Software Innovations and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/
package org.eclipse.smarthome.automation.parser.gson.internal;

import java.lang.reflect.Type;

import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.TriggerType;

import com.google.gson.InstanceCreator;

/**
 * This class creates {@link TriggerType} instances.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class TriggerInstanceCreator implements InstanceCreator<CompositeTriggerType> {

    @Override
    public CompositeTriggerType createInstance(Type type) {
        return new CompositeTriggerType(null, null, null, null);
    }
}
