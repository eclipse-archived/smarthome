/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lwm2mleshan.handler;

import java.util.Map;
import java.util.Map.Entry;

import javax.naming.CommunicationException;

import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lwm2mleshan.internal.CreateThings;
import org.openhab.binding.lwm2mleshan.internal.LeshanOpenhab;
import org.openhab.binding.lwm2mleshan.internal.ObjectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Lwm2mDeviceBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Channels will have some properties:
 * - "unit": for the lwm2m color type this indicates the color space ("RGB", "HSB")
 *
 * TODO:
 * - Object 3: (device), resource 11, 12: Error codes and reset error codes -> Display to the user
 * - Object 3: (device), resource 6-9: Battery level, power source -> Display to the user
 * - Object 3: (device), resource 13-15: Time, Timezone -> Set on first connect, if available
 * - Object 4: Connectivity state -> Display to the user
 * - Object 5: Firmware updates
 * - Object 2: Access restrictions (wait for ESH https://github.com/eclipse/smarthome/issues/579)
 *
 * @author David Graeff - Initial contribution
 */
public class Lwm2mDeviceBridgeHandler extends BaseBridgeHandler
        implements ClientRegistryListener, FirmwareUpdateHandler {
    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(Lwm2mDeviceBridgeHandler.class);
    private final LeshanOpenhab leshan;
    public Client client;
    // Location of this lwm2m device, only available if Object 6 is supported on the device
    private String latitude;
    private String longitude;
    private String altitude;
    // Firmware status
    private static int FIRMWARE_UPDATE_NOT_SUPPORTED = 0;
    private static int FIRMWARE_STATE_IDLE = 1;
    private static int FIRMWARE_STATE_IN_PROGRESS = 2;
    private static int FIRMWARE_STATE_READY_TO_BOOT_NEW_FIRMWARE = 3;
    private int firmwareUpdateState;

    public Lwm2mDeviceBridgeHandler(Bridge bridge, LeshanOpenhab leshan, Client client) {
        super(bridge);
        this.leshan = leshan;
        this.client = client;
    }

    // Avoid dispose+initialize because of a configuration change on the bridge
    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    // Avoid dispose+initialize because of a configuration change on the bridge
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        updateConfiguration(configuration);
    }

    private boolean extractData(ObjectInstance objectInstance, Map<String, String> properties) {
        try {
            LwM2mObjectInstance values;
            try {
                values = leshan.requestValues(objectInstance);
            } catch (CommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return false;
            }
            Map<Integer, LwM2mResource> resources = values.getResources();
            for (Entry<Integer, LwM2mResource> entry : resources.entrySet()) {
                extractDataValue(objectInstance.getObjectID(), entry.getKey(), entry.getValue(), properties);
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void extractDataValue(int objectID, int resourceID, LwM2mResource value, Map<String, String> properties) {
        switch (objectID) {
            case 3: // device
                switch (resourceID) {
                    case 0: // manufacturer
                        properties.put("Manufacturer", (String) value.getValue());
                        break;
                    case 1: // model number
                        properties.put("Model", (String) value.getValue());
                        updateThing(editThing().withLabel((String) value.getValue()).build());
                        break;
                    case 2: // serial number
                        properties.put("SerialNo", (String) value.getValue());
                        break;
                    case 3: // firmware version
                        properties.put("FirmwareVersion", (String) value.getValue());
                        break;
                }
                break;
            case 5: // firmware update
                switch (resourceID) {
                    case 3: // firmware state
                        this.firmwareUpdateState = (int) value.getValue();
                        break;
                }
                break;
            case 6: // location
                switch (resourceID) {
                    case 0: // lat in Deg, example: "-43.5723"
                        this.latitude = (String) value.getValue();
                        break;
                    case 1: // long in Deg
                        this.longitude = (String) value.getValue();
                        updateThing(editThing().withLocation(latitude + "," + longitude).build());
                        break;
                    case 2: // alt in m
                        this.altitude = (String) value.getValue();
                        updateThing(editThing().withLocation(latitude + "," + longitude + "," + altitude).build());
                        break;
                }
                break;
        }
    }

    @Override
    public void dispose() {
        leshan.stopClientObserve(this);
        super.dispose();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);
        firmwareUpdateState = FIRMWARE_UPDATE_NOT_SUPPORTED;
        leshan.startClientObserve(this);
        ObjectInstance[] objectLinks = leshan.getObjectLinks(client);

        Map<String, String> properties = editProperties();

        for (ObjectInstance objectInstance : objectLinks) {
            switch (objectInstance.getObjectID()) {
                case 0: // Security
                case 1: // Server
                case 2: // Access Control
                case 4: // Connectivity Monitoring
                case 7: // Connectivity Statistics
                    break;
                case 3: // Device
                case 5: // Firmware
                case 6: // Location
                    if (!extractData(objectInstance, properties)) {
                        return;
                    }
                    break;
                default:
                    CreateThings.createThing(thingRegistry, objectInstance.getObjectID(),
                            objectInstance.getInstanceID());
            }
        }

        updateProperties(properties);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void registered(Client client) {
    }

    @Override
    public void updated(ClientUpdate update, Client clientUpdated) {
        if (clientUpdated.equals(client)) {
            this.client = clientUpdated;
            for (Thing objectThing : getBridge().getThings()) {
                ThingHandler handler = objectThing.getHandler();
                if (handler == null) {
                    continue;
                }
                ((Lwm2mObjectHandler) handler).updateClient(clientUpdated);
            }
            dispose();
            initialize();
        }
    }

    @Override
    public void unregistered(Client client) {
        if (client.equals(this.client)) {
            // Client not available -> set state to offline and remove client listener.
            // We should be reinitialized by the discovery service if the client reconnects.
            updateStatus(ThingStatus.OFFLINE);
            dispose();
        }
    }

    @Override
    public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
        progressCallback.defineSequence(ProgressStep.TRANSFERRING, ProgressStep.UPDATING, ProgressStep.REBOOTING);
        // TODO implement update. Wait for https://github.com/eclipse/smarthome/pull/1424
        // leshan.sendFirmware(client, firmware);
    }

    @Override
    public boolean isUpdateExecutable() {
        return thing.getStatus() == ThingStatus.ONLINE && firmwareUpdateState == FIRMWARE_STATE_IDLE;
    }
}
