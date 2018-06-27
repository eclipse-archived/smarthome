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
package org.eclipse.smarthome.binding.digitalstrom.internal.discovery;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.handler.BridgeHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.CircuitHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.DeviceHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.SceneHandler;
import org.eclipse.smarthome.binding.digitalstrom.handler.ZoneTemperatureControlHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.TemperatureControlSensorTransmitter;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.providers.DsDeviceThingTypeProvider;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link DiscoveryServiceManager} manages the different scene and device discovery services and informs them about
 * new added or removed scenes and devices.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DiscoveryServiceManager
        implements SceneStatusListener, DeviceStatusListener, TemperatureControlStatusListener {

    private final HashMap<String, AbstractDiscoveryService> discoveryServices;
    private final Map<String, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final String bridgeUID;

    /**
     * Creates a new {@link DiscoveryServiceManager} and generates automatically all {@link SceneDiscoveryService}s and
     * {@link DeviceDiscoveryService}s for all supported {@link ThingType}s of the {@link DeviceHandler} and
     * {@link SceneHandler}.
     *
     * @param bridgeHandler (must not be null)
     */
    public DiscoveryServiceManager(BridgeHandler bridgeHandler) {
        bridgeUID = bridgeHandler.getThing().getUID().getAsString();
        discoveryServices = new HashMap<String, AbstractDiscoveryService>(SceneHandler.SUPPORTED_THING_TYPES.size()
                + DeviceHandler.SUPPORTED_THING_TYPES.size() + CircuitHandler.SUPPORTED_THING_TYPES.size()
                + ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.size());
        for (ThingTypeUID type : SceneHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new SceneDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : DeviceHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new DeviceDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : CircuitHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new DeviceDiscoveryService(bridgeHandler, type));
        }
        for (ThingTypeUID type : ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES) {
            discoveryServices.put(type.getId(), new ZoneTemperatureControlDiscoveryService(bridgeHandler, type));
        }
        bridgeHandler.registerSceneStatusListener(this);
        bridgeHandler.registerDeviceStatusListener(this);
        bridgeHandler.registerTemperatureControlStatusListener(this);
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
                if (service instanceof ZoneTemperatureControlDiscoveryService) {
                    ZoneTemperatureControlDiscoveryService devDisServ = (ZoneTemperatureControlDiscoveryService) service;
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
                if (service instanceof ZoneTemperatureControlDiscoveryService) {
                    this.discoveryServiceRegs.put(
                            bridgeUID + ((ZoneTemperatureControlDiscoveryService) service).getID(),
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
    public void onDeviceRemoved(GeneralDeviceInformation device) {
        if (device instanceof Device) {
            String id = ((Device) device).getHWinfo().substring(0, 2);
            if (((Device) device).isSensorDevice()) {
                id = ((Device) device).getHWinfo().replace("-", "");
            }
            if (discoveryServices.get(id) != null) {
                ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceRemoved(device);
            }
        }
        if (device instanceof Circuit) {
            if (discoveryServices.get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString()) != null) {
                ((DeviceDiscoveryService) discoveryServices
                        .get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString())).onDeviceRemoved(device);
            }
        }
    }

    @Override
    public void onDeviceAdded(GeneralDeviceInformation device) {
        if (device instanceof Device) {
            String id = ((Device) device).getHWinfo().substring(0, 2);
            if (((Device) device).isSensorDevice()) {
                id = ((Device) device).getHWinfo();
            }
            if (discoveryServices.get(id) != null) {
                ((DeviceDiscoveryService) discoveryServices.get(id)).onDeviceAdded(device);
            }
        }
        if (device instanceof Circuit) {
            if (discoveryServices.get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString()) != null) {
                ((DeviceDiscoveryService) discoveryServices
                        .get(DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString())).onDeviceAdded(device);
            }
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

    @Override
    public void configChanged(TemperatureControlStatus tempControlStatus) {
        // currently only this thing-type exists
        if (discoveryServices.get(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL.toString()) != null) {
            ((ZoneTemperatureControlDiscoveryService) discoveryServices
                    .get(DigitalSTROMBindingConstants.THING_TYPE_ZONE_TEMERATURE_CONTROL.toString()))
                            .configChanged(tempControlStatus);
        }
    }

    @Override
    public void registerTemperatureSensorTransmitter(
            TemperatureControlSensorTransmitter temperatureSensorTransreciver) {
        // nothing to do
    }

    @Override
    public Integer getTemperationControlStatusListenrID() {
        return TemperatureControlStatusListener.DISCOVERY;
    }

    @Override
    public void onTargetTemperatureChanged(Float newValue) {
        // nothing to do

    }

    @Override
    public void onControlValueChanged(Integer newValue) {
        // nothing to do

    }
}
