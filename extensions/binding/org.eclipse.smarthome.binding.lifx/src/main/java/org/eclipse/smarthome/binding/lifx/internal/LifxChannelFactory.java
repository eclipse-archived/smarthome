/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link LifxChannelFactory} creates dynamic LIFX channels.
 *
 * @author Wouter Born - Add i18n support
 */
public interface LifxChannelFactory {

    Channel createColorZoneChannel(ThingUID thingUID, int index);

    Channel createTemperatureZoneChannel(ThingUID thingUID, int index);

}
