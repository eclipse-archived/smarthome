/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ntp.internal;

import static org.eclipse.smarthome.binding.ntp.NtpBindingConstants.*;

import org.eclipse.smarthome.binding.ntp.handler.NtpHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link NtpHandlerFactory} is responsible for creating things and thing
 * handlers.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class NtpHandlerFactory extends BaseThingHandlerFactory {

	@Override
	public boolean supportsThingType(ThingTypeUID thingTypeUID) {
		return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
	}

	@Override
	protected ThingHandler createHandler(Thing thing) {

		ThingTypeUID thingTypeUID = thing.getThingTypeUID();

		if (thingTypeUID.equals(THING_TYPE_NTP)) {
			return new NtpHandler(thing);
		}

		return null;
	}
}
