/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
