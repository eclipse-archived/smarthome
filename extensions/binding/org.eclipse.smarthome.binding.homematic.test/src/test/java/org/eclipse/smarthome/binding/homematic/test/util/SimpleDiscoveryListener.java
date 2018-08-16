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
package org.eclipse.smarthome.binding.homematic.test.util;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * This {@link DiscoveryListener} implementation simply records all discovered
 * results, so that they can be checked after the discovery.
 * 
 * @author Florian Stolte - Initial Contribution
 *
 */
public class SimpleDiscoveryListener implements DiscoveryListener {

    public Queue<DiscoveryResult> discoveredResults = new ConcurrentLinkedQueue<DiscoveryResult>();

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        discoveredResults.add(result);
    }

    @Override
    public @Nullable Collection<@NonNull ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return null;
    }
}
