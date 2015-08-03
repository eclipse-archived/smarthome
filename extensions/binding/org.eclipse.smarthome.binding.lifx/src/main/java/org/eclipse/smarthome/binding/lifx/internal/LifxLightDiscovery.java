/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal;

import java.util.List;
import java.util.Map;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLight.LFXLightListener;
import lifx.java.android.network_context.LFXNetworkContext;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.lifx.LifxBindingConstants;
import org.eclipse.smarthome.binding.lifx.internal.LifxConnection.LifxLightTracker;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;

import com.google.common.collect.Sets;

/**
 * The {@link LifxLightDiscovery} provides support for auto-discovery of LIFX
 * lights.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class LifxLightDiscovery extends AbstractDiscoveryService implements LifxLightTracker, LFXLightListener {

    private LFXNetworkContext networkContext;

    public LifxLightDiscovery() throws IllegalArgumentException {
        super(Sets.newHashSet(LifxBindingConstants.THING_TYPE_LIGHT), 1);
    }

    @Override
    public void lightAdded(LFXLight light) {
        DiscoveryResult discoveryResult = createDiscoveryResult(light);
        thingDiscovered(discoveryResult);
        light.addLightListener(this);
    }

    @Override
    public void lightDidChangeColor(LFXLight light, LFXHSBKColor color) {
        // not needed
    }

    @Override
    public void lightDidChangeLabel(LFXLight light, String label) {
        // update discovery result in inbox
        DiscoveryResult discoveryResult = createDiscoveryResult(light);
        thingDiscovered(discoveryResult);
    }

    @Override
    public void lightDidChangePowerState(LFXLight light, LFXPowerState powerState) {
        // not needed
    }

    @Override
    public void lightRemoved(LFXLight light) {
        light.removeLightListener(this);
        thingRemoved(getUID(light));
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        networkContext = LifxConnection.getInstance().getNetworkContext();
        super.activate(configProperties);
    }

    @Override
    protected void deactivate() {
        networkContext = null;
        super.deactivate();
    }

    @Override
    protected void startBackgroundDiscovery() {
        LifxConnection.getInstance().addLightTracker(this);
    }

    @Override
    protected void startScan() {
        List<LFXLight> lights = networkContext.getAllLightsCollection().getLights();
        for (LFXLight light : lights) {
            DiscoveryResult discoveryResult = createDiscoveryResult(light);
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        LifxConnection.getInstance().removeLightTracker(this);
    }

    private DiscoveryResult createDiscoveryResult(LFXLight light) {
        ThingUID thingUID = getUID(light);

        String label = light.getLabel();

        if (StringUtils.isBlank(label))
            label = "LIFX";

        return DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperty(LifxBindingConstants.CONFIG_PROPERTY_DEVICE_ID, light.getDeviceID()).build();
    }

    private ThingUID getUID(LFXLight light) {
        ThingUID thingUID = new ThingUID(LifxBindingConstants.THING_TYPE_LIGHT, light.getDeviceID());
        return thingUID;
    }

}
