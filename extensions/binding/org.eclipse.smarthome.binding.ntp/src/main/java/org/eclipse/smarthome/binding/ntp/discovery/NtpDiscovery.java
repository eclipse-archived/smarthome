/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ntp.discovery;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;

import static org.eclipse.smarthome.binding.ntp.NtpBindingConstants.*;

/**
 * 
 * The {@link NtpDiscovery} is used to add a ntp Thing for the local time in the discovery inbox
 *  * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class NtpDiscovery extends AbstractDiscoveryService {

	private final static String DEFAULT_NTP_SERVER = "0.pool.ntp.org";
	private final static int DEFAULT_REFRESH_INTERVAL = 60;
	private final static int DEFAULT_NTP_INTERVAL = 30;

	public NtpDiscovery() throws IllegalArgumentException {
		super(SUPPORTED_THING_TYPES_UIDS, 10);
	}

	@Override
	protected void startScan() {
		Map<String, Object> properties = new HashMap<>(4);
		properties.put(PROPERTY_NTP_SERVER, DEFAULT_NTP_SERVER);
		properties.put(PROPERTY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
		properties.put(PROPERTY_REFRESH_NTP, DEFAULT_NTP_INTERVAL);
		properties.put(PROPERTY_TIMEZONE, TimeZone.getDefault().getID());
		properties.put(PROPERTY_LOCALE, Locale.getDefault());
		ThingUID uid = new ThingUID(THING_TYPE_NTP, "local");
		if (uid != null) {
			DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
					.withLabel("Local Time").build();
			thingDiscovered(result);
		}

	}

}
