/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.test.hue;

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Sets;

/**
 * {@link ThingHandlerFactory} that can be switched into <code>dumb</code> mode
 * 
 * In <code>dumb</code> mode, it behaves as if the XML configuration files have not been processed yet,
 * i.e. it returns <code>null</code> on {@link #createThing(ThingTypeUID, Configuration, ThingUID, ThingUID)}
 * 
 * @author Simon Kaufmann - Initial contribution and API
 */
public class DumbThingHandlerFactory extends BaseThingHandlerFactory {

    public static final String BINDING_ID = "dumb";

    public final static ThingTypeUID THING_TYPE_TEST = new ThingTypeUID(BINDING_ID, "DUMB");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TEST);

    private boolean dumb;

    public DumbThingHandlerFactory(ComponentContext componentContext, boolean dumb) {
        this.dumb = dumb;
        super.activate(componentContext);
    }

    public void setDumb(boolean dumb) {
        this.dumb = dumb;
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (dumb) {
            return null;
        } else {
            if (SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
                Thing ret = super.createThing(thingTypeUID, configuration, thingUID, null);
                // set a property so that the test case may detect that the thing got created here
                ret.setProperty("funky", "true");
                return ret;
            }
            throw new IllegalArgumentException(
                    "The thing type " + thingTypeUID + " is not supported by the hue binding.");
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
    }
}
