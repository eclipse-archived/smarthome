/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ntp.discovery;

import static org.eclipse.smarthome.binding.ntp.NtpBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 *
 * The {@link NtpDiscovery} is used to add a ntp Thing for the local time in the discovery inbox
 * *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class NtpDiscovery extends AbstractDiscoveryService {

    public NtpDiscovery() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        scheduler.schedule(() -> {
            discoverNtp();
        }, 1, TimeUnit.SECONDS);

    }

    @Override
    protected void startScan() {
        discoverNtp();
    }

    /**
     * Add a ntp Thing for the local time in the discovery inbox
     */
    private void discoverNtp() {
        Map<String, Object> properties = new HashMap<>(4);
        properties.put(PROPERTY_TIMEZONE, TimeZone.getDefault().getID());
        ThingUID uid = new ThingUID(THING_TYPE_NTP, "local");
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel("Local Time").build();
            thingDiscovered(result);
        }

    }

}
