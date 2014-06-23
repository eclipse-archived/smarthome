/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding.builder;

import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;

import com.google.common.collect.Lists;

public class GenericThingBuilder<T extends GenericThingBuilder<T>> {

    private ThingImpl thing;

    protected GenericThingBuilder(ThingImpl thing) {
        this.thing = thing;
    }

    public T withChannels(Channel... channels) {
        this.thing.setChannels(Lists.newArrayList(channels));
        return self();
    }

    public T withChannels(List<Channel> channels) {
        this.thing.setChannels(channels);
        return self();
    }

    public T withConfiguration(Configuration thingConfiguration) {
        this.thing.setConfiguration(thingConfiguration);
        return self();
    }

    public T withBridge(Bridge bridge) {
        if (bridge != null) {
            this.thing.setBridge(bridge);
        }
        return self();
    }

    public Thing build() {
        return this.thing;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

}
