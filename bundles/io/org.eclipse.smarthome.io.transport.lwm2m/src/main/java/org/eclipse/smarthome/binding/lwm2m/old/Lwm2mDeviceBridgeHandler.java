/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lwm2m.old;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.CommunicationException;

import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUpdateHandler;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressCallback;
import org.eclipse.smarthome.core.thing.binding.firmware.ProgressStep;
import org.eclipse.smarthome.core.types.State;
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
public class Lwm2mDeviceBridgeHandler implements RegistrationListener, FirmwareUpdateHandler {
    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(Lwm2mDeviceBridgeHandler.class);
    private final LeshanOpenhab leshan;
    public Registration client;
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
    private Thing thing;

    public Lwm2mDeviceBridgeHandler(Bridge bridge, LeshanOpenhab leshan, Registration client) {

        this.leshan = leshan;
        this.client = client;
    }

    private void updateStatus(ThingStatus offline, ThingStatusDetail communicationError, String localizedMessage) {
    }

    private void updateStatus(ThingStatus initializing) {
    }

    private void updateState(String id2, State newState) {
    }

    private ThingBuilder editThing() {
        return null;
    }

    private void updateThing(Thing build) {
    }

    private boolean extractData(LwM2mPath path, Map<String, String> properties) {
        try {
            LwM2mObjectInstance values;
            try {
                values = leshan.requestValues(client, path);
            } catch (CommunicationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return false;
            }
            Map<Integer, LwM2mResource> resources = values.getResources();
            for (Entry<Integer, LwM2mResource> entry : resources.entrySet()) {
                extractDataValue(path.getObjectId(), entry.getKey(), entry.getValue(), properties);
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

    public void dispose() {
        leshan.stopClientObserve(this);
    }

    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);
        firmwareUpdateState = FIRMWARE_UPDATE_NOT_SUPPORTED;
        leshan.startClientObserve(this);
        LwM2mPath[] objectLinks = leshan.getObjectLinks(client);

        Map<String, String> properties = editProperties();

        for (LwM2mPath objectPath : objectLinks) {
            switch (objectPath.getObjectId()) {
                case 0: // Security
                case 1: // Server
                case 2: // Access Control
                case 4: // Connectivity Monitoring
                case 7: // Connectivity Statistics
                    break;
                case 3: // Device
                case 5: // Firmware
                case 6: // Location^
                    if (!extractData(objectPath, properties)) {
                        return;
                    }
                    break;
                default:

            }
        }

        updateProperties(properties);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateProperties(Map<String, String> properties) {
        // TODO Auto-generated method stub

    }

    private Map<String, String> editProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registered(Registration client) {
    }

    @Override
    public void updated(RegistrationUpdate update, Registration updatedRegistration,
            Registration previousRegistration) {
        if (update.equals(client)) {
            this.client = updatedRegistration;
            for (Thing objectThing : getBridge().getThings()) {
                ThingHandler handler = objectThing.getHandler();
                if (handler == null) {
                    continue;
                }
                ((Lwm2mObjectHandler) handler).updateClient(updatedRegistration);
            }
            dispose();
            initialize();
        }
    }

    private Bridge getBridge() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unregistered(Registration registration, Collection<Observation> observations, boolean expired) {
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

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }

    @Override
    public Thing getThing() {
        // TODO Auto-generated method stub
        return null;
    }

}
