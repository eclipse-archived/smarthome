/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.internal.BridgeImpl;

public class GenericBridgeBuilder<T extends GenericBridgeBuilder<T>> extends GenericThingBuilder<T> {

    protected GenericBridgeBuilder(BridgeImpl bridge) {
        super(bridge);
    }

    @Override
    public Bridge build() {
        return (Bridge) super.build();
    }

}
