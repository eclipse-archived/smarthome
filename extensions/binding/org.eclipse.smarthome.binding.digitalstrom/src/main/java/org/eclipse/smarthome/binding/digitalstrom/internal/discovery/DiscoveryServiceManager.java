/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.SceneHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link DiscoveryServiceManager} manages the different scene and device discovery services and informs them about
 * new added or removed scenes and devices.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DiscoveryServiceManager implements SceneStatusListener, DeviceStatusListener {

    private HashMap<String, AbstractDiscoveryService> discoveryServices = null;
    private Map<String, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private String bridgeUID;

    /**
     * Creates a new {@link DiscoveryServiceManager} and generates automatically all {@link SceneDiscoveryService}s and
     * {@link DeviceDiscoveryService}s for all supported {@link ThingType}s of the {@link DeviceHandler} and
     * {@link SceneHandler}.
     *
     * @param bridgeHandler (must not be null)
     */
    public DiscoveryServiceManager(BridgeHandler bridgeHandler) {
        bridgeUID = bridgeHandler.getThing().getUID().getAsString();
        discoveryServices = new HashMap<String, AbstractDiscoveryService>(
                SceneHandler.SUPPORTED_THING_TYPES.size() + DeviceHandler.SUPPORTED_THING_TYPES.size());
        for (ThingTypeUID type : SceneHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new SceneDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : DeviceHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new DeviceDiscoveryService(bridgeHandler, type));
        }
        bridgeHandler.registerSceneStatusListener(this);
        bridgeHandler.registerDeviceStatusListener(this);
    }

    /**
     * Deactivates all {@link SceneDiscoveryService}s and {@link DeviceDiscoveryService}s of this
     * {@link DiscoveryServiceManager} and unregisters them from the given {@link BundleContext}.
     *
     * @param bundleContext (must not be null)
     */
    public void unregisterDiscoveryServices(BundleContext bundleContext) {
        if (discoveryServices != null) {
            for (AbstractDiscoveryService service : discoveryServices.values()) {
                if (service instanceof SceneDiscoveryService) {
                    SceneDiscoveryService sceneDisServ = (SceneDiscoveryService) service;
                    ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID + sceneDisServ.getID());
                    sceneDisServ.deactivate();
                    serviceReg.unregister();
                    discoveryServiceRegs.remove(bridgeUID + sceneDisServ.getID());
                }
                if (service instanceof DeviceDiscoveryService) {
                    DeviceDiscoveryService devDisServ = (DeviceDiscoveryService) service;
                    ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeUID + devDisServ.getID());
                    devDisServ.deactivate();
                    serviceReg.unregister();
                    discoveryServiceRegs.remove(bridgeUID + devDisServ.getID());
                }
            }
        }
    }

    /**
     * Registers all {@link SceneDiscoveryService}s and {@link DeviceDiscoveryService}s of this
     * {@link DiscoveryServiceManager} to the given {@link BundleContext}.
     *
     * @param bundleContext (must not be null)
     */
    public void registerDiscoveryServices(BundleContext bundleContext) {
        if (discoveryServices != null) {
            for (AbstractDiscoveryService service : discoveryServices.values()) {
                if (service instanceof SceneDiscoveryService) {
                    this.discoveryServiceRegs.put(bridgeUID + ((SceneDiscoveryService) service).getID(),
                            bundleContext.registerService(DiscoveryService.class.getName(), service,
                                    new Hashtable<String, Object>()));
                }
                if (service instanceof DeviceDiscoveryService) {
                    this.discoveryServiceRegs.put(bridgeUID + ((DeviceDiscoveryService) service).getID(),
                            bundleContext.registerService(DiscoveryService.class.getName(), service,
                                    new Hashtable<String, Object>()));
                }
            }
        }
    }

    @Override
    public String getSceneStatusListenerID() {
        return SceneStatusListener.SCENE_DISCOVERY;
    }

    @Override
    public void onSceneStateChanged(boolean flag) {
        // nothing to do
    }

    @Override
    public void onSceneRemoved(InternalScene scene) {
        if (discoveryServices.get(scene.getSceneType()) != null) {
            ((SceneDiscoveryService) discoveryServices.get(scene.getSceneType())).onSceneRemoved(scene);
        }
    }

    @Override
    public void onSceneAdded(InternalScene scene) {
        if (discoveryServices.get(scene.getSceneType()) != null) {
            ((SceneDiscoveryService) discoveryServices.get(scene.getSceneType())).onSceneAdded(scene);
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        // nothing to do
    }

    @Override
    public void onDeviceRemoved(Device device) {
        String id = device.getHWinfo().substring(0, 2);
        if (discoveryServices.get(id) != null) {
            ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceRemoved(device);
        }
    }

    @Override
    public void onDeviceAdded(Device device) {
        String id = device.getHWinfo().substring(0, 2);
        if (discoveryServices.get(id) != null) {
            ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceAdded(device);
        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whatConfig) {
        // nothing to do
    }

    @Override
    public void onSceneConfigAdded(short sceneId) {
        // nothing to do
    }

    @Override
    public String getDeviceStatusListenerID() {
        return DeviceStatusListener.DEVICE_DISCOVERY;
    }
}
