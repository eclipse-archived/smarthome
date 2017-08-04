/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.DeviceStatusManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SceneReadingJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceConsumptionSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.DeviceOutputValueSensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneConfigReadingJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.impl.SceneOutputValueReadingJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdateImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.ApartmentSceneEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceStatusManagerImpl} is the implementation of the the {@link DeviceStatusManager}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceStatusManagerImpl implements DeviceStatusManager {

    private Logger logger = LoggerFactory.getLogger(DeviceStatusManagerImpl.class);

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private ScheduledFuture<?> pollingScheduler = null;

    private ConnectionManager connMan;
    private StructureManager strucMan;
    private SceneManager sceneMan;
    private DsAPI digitalSTROMClient;
    private Config config;

    private SensorJobExecutor sensorJobExecutor = null;
    private SceneReadingJobExecutor sceneJobExecutor = null;

    private List<String> meters = null;
    private List<TrashDevice> trashDevices = new LinkedList<TrashDevice>();

    private long lastBinCheck = 0;
    private ManagerStates state = ManagerStates.STOPPED;

    private int tempConsumption = 0;
    private int totalPowerConsumption = 0;
    private int tempEnergyMeter = 0;
    private int totalEnergyMeter = 0;

    private DeviceStatusListener deviceDiscovery = null;
    private TotalPowerConsumptionListener totalPowerConsumptionListener = null;
    private ManagerStatusListener statusListener = null;

    public DeviceStatusManagerImpl(Config config) {
        init(new ConnectionManagerImpl(config), null, null, null);
    }

    public DeviceStatusManagerImpl(String hostAddress, String user, String password, String appToken) {
        init(new ConnectionManagerImpl(hostAddress, user, password, false), null, null, null);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan) {
        init(connMan, strucMan, sceneMan, null);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan,
            ManagerStatusListener statusListener) {
        init(connMan, strucMan, sceneMan, statusListener);
    }

    public DeviceStatusManagerImpl(ConnectionManager connMan) {
        init(connMan, null, null, null);
    }

    private void init(ConnectionManager connMan, StructureManager strucMan, SceneManager sceneMan,
            ManagerStatusListener statusListener) {
        this.connMan = connMan;
        this.digitalSTROMClient = connMan.getDigitalSTROMAPI();
        this.config = connMan.getConfig();
        if (strucMan != null) {
            this.strucMan = strucMan;
        } else {
            this.strucMan = new StructureManagerImpl();
        }
        if (sceneMan != null) {
            this.sceneMan = sceneMan;
        } else {
            this.sceneMan = new SceneManagerImpl(connMan, strucMan, statusListener);
        }
        this.statusListener = statusListener;
    }

    private class PollingRunnable implements Runnable {
        private boolean devicesLoaded = false;
        private long nextSensorUpdate = 0;

        @Override
        public void run() {
            if (connMan.checkConnection()) {
                if (!getManagerState().equals(ManagerStates.RUNNING)) {
                    logger.debug("Thread started");
                    if (devicesLoaded) {
                        stateChanged(ManagerStates.RUNNING);
                    } else {
                        stateChanged(ManagerStates.INITIALIZING);
                    }
                }
                HashMap<DSID, Device> tempDeviceMap;
                if (strucMan.getDeviceMap() != null) {
                    tempDeviceMap = (HashMap<DSID, Device>) strucMan.getDeviceMap();
                } else {
                    tempDeviceMap = new HashMap<DSID, Device>();
                }

                List<Device> currentDeviceList = digitalSTROMClient.getApartmentDevices(connMan.getSessionToken(),
                        false);

                // update the current total power consumption
                if (totalPowerConsumptionListener != null && nextSensorUpdate <= System.currentTimeMillis()) {
                    meters = digitalSTROMClient.getMeterList(connMan.getSessionToken());
                    totalPowerConsumptionListener.onTotalPowerConsumptionChanged(getTotalPowerConsumption());
                    totalPowerConsumptionListener.onEnergyMeterValueChanged(getTotalEnergyMeterValue());
                    nextSensorUpdate = System.currentTimeMillis() + config.getTotalPowerUpdateInterval();
                }

                while (!currentDeviceList.isEmpty()) {
                    Device currentDevice = currentDeviceList.remove(0);
                    DSID currentDeviceDSID = currentDevice.getDSID();
                    Device eshDevice = tempDeviceMap.remove(currentDeviceDSID);

                    if (eshDevice != null) {
                        checkDeviceConfig(currentDevice, eshDevice);

                        if (eshDevice.isPresent()) {
                            // check device state updates
                            while (!eshDevice.isDeviceUpToDate()) {
                                DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                                if (deviceStateUpdate != null) {
                                    switch (deviceStateUpdate.getType()) {
                                        case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                                        case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                                            filterCommand(deviceStateUpdate, eshDevice);
                                            break;
                                        case DeviceStateUpdate.UPDATE_SCENE_CONFIG:
                                        case DeviceStateUpdate.UPDATE_SCENE_OUTPUT:
                                            updateSceneData(eshDevice, deviceStateUpdate);
                                            break;
                                        case DeviceStateUpdate.UPDATE_OUTPUT_VALUE:
                                            readOutputValue(eshDevice);
                                            break;
                                        default:
                                            sendComandsToDSS(eshDevice, deviceStateUpdate);
                                    }
                                }
                            }
                        }

                    } else {
                        logger.debug("Found new device!");
                        if (trashDevices.isEmpty()) {
                            currentDevice.setConfig(config);
                            strucMan.addDeviceToStructure(currentDevice);
                            logger.debug("trashDevices are empty, add Device with dSID {} to the deviceMap!",
                                    currentDevice.getDSID());
                        } else {
                            logger.debug("Search device in trashDevices.");
                            TrashDevice foundTrashDevice = null;
                            for (TrashDevice trashDevice : trashDevices) {
                                if (trashDevice != null) {
                                    if (trashDevice.getDevice().equals(currentDevice)) {
                                        foundTrashDevice = trashDevice;
                                        logger.debug(
                                                "Found device in trashDevices, add TrashDevice with dSID {} to the StructureManager!",
                                                currentDeviceDSID);
                                    }
                                }
                            }
                            if (foundTrashDevice != null) {
                                trashDevices.remove(foundTrashDevice);
                                strucMan.addDeviceToStructure(foundTrashDevice.getDevice());
                            } else {
                                strucMan.addDeviceToStructure(currentDevice);
                                logger.debug(
                                        "Can't find device in trashDevices, add Device with dSID: {} to the StructureManager!",
                                        currentDeviceDSID);
                            }
                        }
                        if (deviceDiscovery != null) {
                            if (currentDevice.isDeviceWithOutput()) {
                                deviceDiscovery.onDeviceAdded(currentDevice);
                                logger.debug("inform DeviceStatusListener: {} about removed device with dSID {}",
                                        DeviceStatusListener.DEVICE_DISCOVERY, currentDevice.getDSID().getValue());
                            }
                        } else {
                            logger.debug(
                                    "The device discovery is not registrated, can't inform device discovery about found device.");
                        }
                    }
                }

                if (!devicesLoaded && strucMan.getDeviceMap() != null) {
                    if (!strucMan.getDeviceMap().values().isEmpty()) {
                        logger.debug("Devices loaded");
                        devicesLoaded = true;
                        stateChanged(ManagerStates.RUNNING);
                    }
                }

                if (!sceneMan.scenesGenerated()
                        && !sceneMan.getManagerState().equals(ManagerStates.GENERATING_SCENES)) {
                    logger.debug("{}", sceneMan.getManagerState());
                    sceneMan.generateScenes();
                }

                for (Device device : tempDeviceMap.values()) {
                    logger.debug("Found removed devices.");

                    trashDevices.add(new TrashDevice(device));
                    DeviceStatusListener listener = device.unregisterDeviceStateListener();
                    if (listener != null) {
                        listener.onDeviceRemoved(null);
                    }
                    strucMan.deleteDevice(device);
                    logger.debug("Add device with dSID {} to trashDevices", device.getDSID().getValue());

                    if (deviceDiscovery != null) {
                        deviceDiscovery.onDeviceRemoved(device);
                        logger.debug("inform DeviceStatusListener: {} about removed device with dSID {}",
                                DeviceStatusListener.DEVICE_DISCOVERY, device.getDSID().getValue());
                    } else {
                        logger.debug(
                                "The device-Discovery is not registrated, can't inform device discovery about removed device.");
                    }
                }

                if (!trashDevices.isEmpty() && (lastBinCheck + config.getBinCheckTime() < System.currentTimeMillis())) {
                    for (TrashDevice trashDevice : trashDevices) {
                        if (trashDevice.isTimeToDelete(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))) {
                            logger.debug("Found trashDevice that have to delete!");
                            trashDevices.remove(trashDevice);
                            logger.debug("Delete trashDevice: {}", trashDevice.getDevice().getDSID().getValue());
                        }
                    }
                    lastBinCheck = System.currentTimeMillis();
                }
            }
        }

        private void filterCommand(DeviceStateUpdate deviceStateUpdate, Device device) {
            String stateUpdateType = deviceStateUpdate.getType();
            short newAngle = 0;
            if (stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                    || stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)) {
                newAngle = device.getAnglePosition();
            }
            DeviceStateUpdate nextDeviceStateUpdate = device.getNextDeviceUpdateState();
            while (nextDeviceStateUpdate != null && nextDeviceStateUpdate.getType().equals(stateUpdateType)) {
                switch (stateUpdateType) {
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                        deviceStateUpdate = nextDeviceStateUpdate;
                        nextDeviceStateUpdate = device.getNextDeviceUpdateState();
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                        if (deviceStateUpdate.getValue() == 1) {
                            newAngle = (short) (newAngle + DeviceConstants.ANGLE_STEP_SLAT);
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                        if (deviceStateUpdate.getValue() == 1) {
                            newAngle = (short) (newAngle - DeviceConstants.ANGLE_STEP_SLAT);
                        }
                        break;
                }
            }
            if (stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                    || stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)) {
                if (newAngle > device.getMaxSlatAngle()) {
                    newAngle = (short) device.getMaxSlatAngle();
                }
                if (newAngle < device.getMinSlatAngle()) {
                    newAngle = (short) device.getMinSlatAngle();
                }
                if (!(stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE)
                        && checkAngleIsMinMax(device) == 1)
                        || !(stateUpdateType.equals(DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE)
                                && checkAngleIsMinMax(device) == 0)) {
                    deviceStateUpdate = new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, newAngle);
                }
            }
            sendComandsToDSS(device, deviceStateUpdate);
            if (nextDeviceStateUpdate != null) {
                if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                        || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                    updateSceneData(device, deviceStateUpdate);
                } else {
                    sendComandsToDSS(device, deviceStateUpdate);
                }
            }
        }

    };

    @Override
    public ManagerTypes getManagerType() {
        return ManagerTypes.DEVICE_STATUS_MANAGER;
    }

    @Override
    public ManagerStates getManagerState() {
        return state;
    }

    private synchronized void stateChanged(ManagerStates state) {
        if (statusListener != null) {
            this.state = state;
            statusListener.onStatusChanged(ManagerTypes.DEVICE_STATUS_MANAGER, state);
        }
    }

    @Override
    public synchronized void start() {
        logger.debug("start pollingScheduler");
        if (pollingScheduler == null || pollingScheduler.isCancelled()) {
            pollingScheduler = scheduler.scheduleWithFixedDelay(new PollingRunnable(), 0, config.getPollingFrequency(),
                    TimeUnit.MILLISECONDS);
            sceneMan.start();
        }
        if (sceneJobExecutor != null) {
            this.sceneJobExecutor.startExecutor();
        }

        if (sensorJobExecutor != null) {
            this.sensorJobExecutor.startExecutor();
        }
    }

    @Override
    public synchronized void stop() {
        logger.debug("stop pollingScheduler");
        if (sceneMan != null) {
            sceneMan.stop();
        }
        if (pollingScheduler != null && !pollingScheduler.isCancelled()) {
            pollingScheduler.cancel(true);
            pollingScheduler = null;
            stateChanged(ManagerStates.STOPPED);
        }
        if (sceneJobExecutor != null) {
            this.sceneJobExecutor.shutdown();
        }
        if (sensorJobExecutor != null) {
            this.sensorJobExecutor.shutdown();
        }
    }

    /**
     * The {@link TrashDevice} saves not present {@link Device}'s, but at this point not deleted from the
     * digitalSTROM-System, temporary to get back the configuration of the {@link Device}'s faster.
     *
     * @author Michael Ochel - Initial contribution
     * @author Matthias Siegele - Initial contribution
     */
    private class TrashDevice {
        private Device device;
        private int timeStamp;

        /**
         * Creates a new {@link TrashDevice}.
         *
         * @param device
         */
        public TrashDevice(Device device) {
            this.device = device;
            this.timeStamp = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Returns the saved {@link Device}.
         *
         * @return device
         */
        public Device getDevice() {
            return device;
        }

        /**
         * Returns true if the time for the {@link TrashDevice} is over and it can be deleted.
         *
         * @param dayOfYear
         * @return true = time to delete | false = not time to delete
         */
        public boolean isTimeToDelete(int dayOfYear) {
            return this.timeStamp + config.getTrashDeviceDeleteTime() <= dayOfYear;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof TrashDevice
                    ? this.device.getDSID().equals(((TrashDevice) object).getDevice().getDSID()) : false;
        }
    }

    private void checkDeviceConfig(Device newDevice, Device internalDevice) {
        if (newDevice == null || internalDevice == null) {
            return;
        }
        // check device availability has changed and inform the deviceStatusListener about the change.
        // NOTE:
        // The device is not availability for the digitalSTROM-Server, it has not been deleted and are therefore set to
        // OFFLINE.
        // To delete an alternate algorithm is responsible.
        if (newDevice.isPresent() != internalDevice.isPresent()) {
            internalDevice.setIsPresent(newDevice.isPresent());
        }
        if (newDevice.getMeterDSID() != null && !newDevice.getMeterDSID().equals(internalDevice.getMeterDSID())) {
            internalDevice.setMeterDSID(newDevice.getMeterDSID().getValue());
        }
        if (newDevice.getFunctionalColorGroup() != null
                && !newDevice.getFunctionalColorGroup().equals(internalDevice.getFunctionalColorGroup())) {
            internalDevice.setFunctionalColorGroup(newDevice.getFunctionalColorGroup());
        }
        if (newDevice.getName() != null && !newDevice.getName().equals(internalDevice.getName())) {
            internalDevice.setName(newDevice.getName());
        }
        if (newDevice.getOutputMode() != null && !newDevice.getOutputMode().equals(internalDevice.getOutputMode())) {
            internalDevice.setOutputMode(newDevice.getOutputMode());
        }
        strucMan.updateDevice(newDevice);
    }

    private long lastSceneCall = 0;
    private long sleepTime = 0;

    @Override
    public synchronized void sendSceneComandsToDSS(InternalScene scene, boolean call_undo) {
        if (scene != null) {
            if (lastSceneCall + 1000 > System.currentTimeMillis()) {
                sleepTime = System.currentTimeMillis() - lastSceneCall;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred", e);
                }
            }
            if (this.connMan.checkConnection()) {
                lastSceneCall = System.currentTimeMillis();
                boolean requestSuccsessfull = false;
                if (scene.getZoneID() == 0) {
                    if (call_undo) {
                        logger.debug("{} {} {}", scene.getGroupID(), scene.getSceneID(),
                                ApartmentSceneEnum.getApartmentScene(scene.getSceneID()));
                        requestSuccsessfull = this.digitalSTROMClient.callApartmentScene(connMan.getSessionToken(),
                                scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()),
                                false);
                    } else {
                        requestSuccsessfull = this.digitalSTROMClient.undoApartmentScene(connMan.getSessionToken(),
                                scene.getGroupID(), null, ApartmentSceneEnum.getApartmentScene(scene.getSceneID()));
                    }
                } else {
                    if (call_undo) {
                        requestSuccsessfull = this.digitalSTROMClient.callZoneScene(connMan.getSessionToken(),
                                scene.getZoneID(), null, scene.getGroupID(), null,
                                SceneEnum.getScene(scene.getSceneID()), false);
                    } else {
                        requestSuccsessfull = this.digitalSTROMClient.undoZoneScene(connMan.getSessionToken(),
                                scene.getZoneID(), null, scene.getGroupID(), null,
                                SceneEnum.getScene(scene.getSceneID()));
                    }
                }

                logger.debug("Was the scene call succsessful?: {}", requestSuccsessfull);
                if (requestSuccsessfull) {
                    this.sceneMan.addEcho(scene.getID());
                    if (call_undo) {
                        scene.activateScene();
                    } else {
                        scene.deactivateScene();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void sendStopComandsToDSS(final Device device) {
        scheduler.execute(new Runnable() {

            @Override
            public void run() {
                if (connMan.checkConnection()) {
                }
                if (digitalSTROMClient.callDeviceScene(connMan.getSessionToken(), device.getDSID(), null,
                        SceneEnum.STOP, true)) {
                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.STOP.getSceneNumber());
                    readOutputValue(device);
                }
            }
        });
    }

    private void readOutputValue(Device device) {
        short outputIndex = DeviceConstants.DEVICE_SENSOR_OUTPUT;
        if (device.isShade()) {
            outputIndex = DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT;
        }

        int outputValue = this.digitalSTROMClient.getDeviceOutputValue(connMan.getSessionToken(), device.getDSID(),
                null, outputIndex);
        if (outputValue != -1) {
            if (!device.isShade()) {
                device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_BRIGHTNESS, outputValue));
            } else {
                device.updateInternalDeviceState(
                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLATPOSITION, outputValue));
                if (device.isBlind()) {
                    outputValue = this.digitalSTROMClient.getDeviceOutputValue(connMan.getSessionToken(),
                            device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT);
                    device.updateInternalDeviceState(
                            new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_SLAT_ANGLE, outputValue));
                }
            }
        }
    }

    public synchronized void updateDevice(Device eshDevice) {
        logger.debug("Check device updates");
        // check device state updates
        while (!eshDevice.isDeviceUpToDate()) {
            DeviceStateUpdate deviceStateUpdate = eshDevice.getNextDeviceUpdateState();
            if (deviceStateUpdate != null) {
                if (deviceStateUpdate.getType() != DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                    if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                            || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                        updateSceneData(eshDevice, deviceStateUpdate);
                    } else {
                        sendComandsToDSS(eshDevice, deviceStateUpdate);
                    }
                } else {
                    DeviceStateUpdate nextDeviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                    while (nextDeviceStateUpdate != null
                            && nextDeviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_BRIGHTNESS) {
                        deviceStateUpdate = nextDeviceStateUpdate;
                        nextDeviceStateUpdate = eshDevice.getNextDeviceUpdateState();
                    }
                    sendComandsToDSS(eshDevice, deviceStateUpdate);
                    if (nextDeviceStateUpdate != null) {
                        if (deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_CONFIG
                                || deviceStateUpdate.getType() == DeviceStateUpdate.UPDATE_SCENE_OUTPUT) {
                            updateSceneData(eshDevice, deviceStateUpdate);
                        } else {
                            sendComandsToDSS(eshDevice, deviceStateUpdate);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the output value of a {@link Device} and return 0, if the output value or slat position is min and 1, if
     * the output value or slat position is max, otherwise it returns -1.
     *
     * @param device
     * @return 0 = output value is min, 1 device value is min, otherwise -1
     */
    private short checkIsAllreadyMinMax(Device device) {
        if (device.isShade()) {
            if (device.getSlatPosition() == device.getMaxSlatPosition()) {
                if (device.isBlind()) {
                    if (device.getAnglePosition() == device.getMaxSlatAngle()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return 1;
            }
            if (device.getSlatPosition() == device.getMinSlatPosition()) {
                if (device.isBlind()) {
                    if (device.getAnglePosition() == device.getMinSlatAngle()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
        } else {
            if (device.getOutputValue() == device.getMaxOutputValue()) {
                return 1;
            }
            if (device.getOutputValue() == device.getMinOutputValue() || device.getOutputValue() <= 0) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * Checks the angle value of a {@link Device} and return 0, if the angle value is min and 1, if the angle value is
     * max, otherwise it returns -1.
     *
     * @param device
     * @return 0 = angle value is min, 1 angle value is min, otherwise -1
     */
    private short checkAngleIsMinMax(Device device) {
        if (device.getAnglePosition() == device.getMaxSlatAngle()) {
            return 1;
        }
        if (device.getAnglePosition() == device.getMinSlatAngle()) {
            return 1;
        }
        return -1;
    }

    @Override
    public synchronized void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (connMan.checkConnection()) {
            boolean requestSuccsessful = false;
            boolean commandHaveNoEffect = false;
            if (deviceStateUpdate != null) {
                switch (deviceStateUpdate.getType()) {
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS_DECREASE:
                    case DeviceStateUpdate.UPDATE_SLAT_DECREASE:
                        if (checkIsAllreadyMinMax(device) != 0) {
                            requestSuccsessful = digitalSTROMClient.decreaseValue(connMan.getSessionToken(),
                                    device.getDSID());
                            if (requestSuccsessful) {
                                sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.DECREMENT.getSceneNumber());
                            }
                        } else {
                            commandHaveNoEffect = true;
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS_INCREASE:
                    case DeviceStateUpdate.UPDATE_SLAT_INCREASE:
                        if (checkIsAllreadyMinMax(device) != 1) {
                            requestSuccsessful = digitalSTROMClient.increaseValue(connMan.getSessionToken(),
                                    device.getDSID());
                            if (requestSuccsessful) {
                                sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.INCREMENT.getSceneNumber());
                            }
                        } else {
                            commandHaveNoEffect = true;
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_BRIGHTNESS:
                        if (device.getOutputValue() != deviceStateUpdate.getValue()) {
                            requestSuccsessful = digitalSTROMClient.setDeviceValue(connMan.getSessionToken(),
                                    device.getDSID(), null, deviceStateUpdate.getValue());
                        } else {
                            commandHaveNoEffect = true;
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_OPEN_CLOSE:
                    case DeviceStateUpdate.UPDATE_ON_OFF:
                        if (deviceStateUpdate.getValue() > 0) {
                            if (checkIsAllreadyMinMax(device) != 1) {
                                requestSuccsessful = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                        device.getDSID(), null);
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MAXIMUM.getSceneNumber());
                                }
                            } else {
                                commandHaveNoEffect = true;
                            }
                        } else {
                            if (checkIsAllreadyMinMax(device) != 0) {
                                requestSuccsessful = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                        device.getDSID(), null);
                                if (requestSuccsessful) {
                                    sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MINIMUM.getSceneNumber());
                                }
                                if (sensorJobExecutor != null) {
                                    sensorJobExecutor.removeSensorJobs(device);
                                }
                            } else {
                                commandHaveNoEffect = true;
                            }
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_SLATPOSITION:
                        if (device.getSlatPosition() != deviceStateUpdate.getValue()) {
                            requestSuccsessful = digitalSTROMClient.setDeviceOutputValue(connMan.getSessionToken(),
                                    device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_POSITION_OUTPUT,
                                    deviceStateUpdate.getValue());
                        } else {
                            commandHaveNoEffect = true;
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_STOP:
                        this.sendStopComandsToDSS(device);
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_MOVE:
                        if (deviceStateUpdate.getValue() > 0) {
                            requestSuccsessful = digitalSTROMClient.turnDeviceOn(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSuccsessful) {
                                sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MAXIMUM.getSceneNumber());
                            }
                        } else {
                            requestSuccsessful = digitalSTROMClient.turnDeviceOff(connMan.getSessionToken(),
                                    device.getDSID(), null);
                            if (requestSuccsessful) {
                                sceneMan.addEcho(device.getDSID().getValue(), SceneEnum.MINIMUM.getSceneNumber());
                            }
                            if (sensorJobExecutor != null) {
                                sensorJobExecutor.removeSensorJobs(device);
                            }
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_CALL_SCENE:
                        if (SceneEnum.getScene((short) deviceStateUpdate.getValue()) != null) {
                            requestSuccsessful = digitalSTROMClient.callDeviceScene(connMan.getSessionToken(),
                                    device.getDSID(), null, SceneEnum.getScene((short) deviceStateUpdate.getValue()),
                                    true);
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_UNDO_SCENE:
                        if (SceneEnum.getScene((short) deviceStateUpdate.getValue()) != null) {
                            requestSuccsessful = digitalSTROMClient.undoDeviceScene(connMan.getSessionToken(),
                                    device.getDSID(), SceneEnum.getScene((short) deviceStateUpdate.getValue()));
                        }
                        break;
                    case DeviceStateUpdate.UPDATE_ACTIVE_POWER:
                        if (deviceStateUpdate.getValue() == 0) {
                            logger.debug("Device need active power SensorData update");
                            updateSensorData(new DeviceConsumptionSensorJob(device, SensorEnum.ACTIVE_POWER),
                                    device.getActivePowerRefreshPriority());
                            return;
                        } else {
                            int consumption = this.digitalSTROMClient.getDeviceSensorValue(connMan.getSessionToken(),
                                    device.getDSID(), null, SensorEnum.ACTIVE_POWER);
                            if (consumption >= 0) {
                                device.updateInternalDeviceState(
                                        new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_ACTIVE_POWER, consumption));
                                requestSuccsessful = true;
                            }
                        }
                    case DeviceStateUpdate.UPDATE_OUTPUT_CURRENT:
                        if (deviceStateUpdate.getValue() == 0) {
                            logger.debug("Device need output current SensorData update");
                            updateSensorData(new DeviceConsumptionSensorJob(device, SensorEnum.OUTPUT_CURRENT),
                                    device.getOutputCurrentRefreshPriority());
                            return;
                        } else {
                            int consumption = this.digitalSTROMClient.getDeviceSensorValue(connMan.getSessionToken(),
                                    device.getDSID(), null, SensorEnum.OUTPUT_CURRENT);
                            if (consumption >= 0) {
                                device.updateInternalDeviceState(new DeviceStateUpdateImpl(
                                        DeviceStateUpdate.UPDATE_OUTPUT_CURRENT, consumption));
                                requestSuccsessful = true;
                            }
                        }
                    case DeviceStateUpdate.UPDATE_ELECTRIC_METER:
                        if (deviceStateUpdate.getValue() == 0) {
                            logger.debug("Device need electric meter SensorData update");
                            updateSensorData(new DeviceConsumptionSensorJob(device, SensorEnum.ELECTRIC_METER),
                                    device.getElectricMeterRefreshPriority());
                            return;
                        } else {
                            int consumption = this.digitalSTROMClient.getDeviceSensorValue(connMan.getSessionToken(),
                                    device.getDSID(), null, SensorEnum.ELECTRIC_METER);
                            if (consumption >= 0) {
                                device.updateInternalDeviceState(new DeviceStateUpdateImpl(
                                        DeviceStateUpdate.UPDATE_ELECTRIC_METER, consumption));
                                requestSuccsessful = true;
                            }
                        }
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_DECREASE:
                        // By UPDATE_SLAT_ANGLE_DECREASE, UPDATE_SLAT_ANGLE_INCREASE with value unequal 1 which will
                        // handle in the pollingRunnable and UPDATE_OPEN_CLOSE_ANGLE the value will be set without
                        // checking, because it was triggered by setting the slat position.
                        requestSuccsessful = true;
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE_INCREASE:
                        requestSuccsessful = true;
                        break;
                    case DeviceStateUpdate.UPDATE_OPEN_CLOSE_ANGLE:
                        requestSuccsessful = true;
                        break;
                    case DeviceStateUpdate.UPDATE_SLAT_ANGLE:
                        if (device.getAnglePosition() != deviceStateUpdate.getValue()) {
                            requestSuccsessful = digitalSTROMClient.setDeviceOutputValue(connMan.getSessionToken(),
                                    device.getDSID(), null, DeviceConstants.DEVICE_SENSOR_SLAT_ANGLE_OUTPUT,
                                    deviceStateUpdate.getValue());
                        } else {
                            commandHaveNoEffect = true;
                        }
                        break;
                    case DeviceStateUpdate.REFRESH_OUTPUT:
                        readOutputValue(device);
                        logger.debug("Inizalize output value reading for device with dSID {}.",
                                device.getDSID().getValue());
                        return;
                    default:
                        return;
                }
                if (commandHaveNoEffect) {
                    logger.debug("Command {} for device with dSID {} not send to dSS, because it has no effect!",
                            deviceStateUpdate.getType(), device.getDSID().getValue());
                    return;
                }
                if (requestSuccsessful) {
                    logger.debug("Send {} command to dSS and updateInternalDeviceState for device with dSID {}.",
                            deviceStateUpdate.getType(), device.getDSID().getValue());
                    device.updateInternalDeviceState(deviceStateUpdate);
                } else {
                    logger.debug("Can't send {} command for device with dSID {} to dSS!", deviceStateUpdate.getType(),
                            device.getDSID().getValue());
                }
            }
        }
    }

    @Override
    public void updateSensorData(SensorJob sensorJob, String priority) {
        if (sensorJobExecutor == null) {
            sensorJobExecutor = new SensorJobExecutor(connMan);
            this.sensorJobExecutor.startExecutor();
        }
        if (sensorJob != null && priority != null) {
            if (priority.contains(Config.REFRESH_PRIORITY_HIGH)) {
                sensorJobExecutor.addHighPriorityJob(sensorJob);
            } else if (priority.contains(Config.REFRESH_PRIORITY_MEDIUM)) {
                sensorJobExecutor.addMediumPriorityJob(sensorJob);
            } else if (priority.contains(Config.REFRESH_PRIORITY_LOW)) {
                sensorJobExecutor.addLowPriorityJob(sensorJob);
            } else {
                logger.debug("Sensor data update priority do {}  not exist! Please check the input!", priority);
                return;
            }
            logger.debug("Add new sensorJob {} with priority: {} to sensorJobExecuter", sensorJob.toString(), priority);
        }
    }

    @Override
    public void updateSceneData(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (sceneJobExecutor == null) {
            sceneJobExecutor = new SceneReadingJobExecutor(connMan);
            this.sceneJobExecutor.startExecutor();
        }

        if (deviceStateUpdate != null) {
            if (deviceStateUpdate.getValue() < 1000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecutor.addHighPriorityJob(
                            new SceneOutputValueReadingJob(device, (short) deviceStateUpdate.getValue()));
                    DeviceOutputValueSensorJob devOutJob = new DeviceOutputValueSensorJob(device);
                    devOutJob.setInitalisationTime(0);
                    updateSensorData(devOutJob, Config.REFRESH_PRIORITY_HIGH);
                } else {
                    sceneJobExecutor.addHighPriorityJob(
                            new SceneConfigReadingJob(device, (short) deviceStateUpdate.getValue()));
                }
            } else if (deviceStateUpdate.getValue() < 2000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecutor.addMediumPriorityJob(
                            new SceneOutputValueReadingJob(device, (short) (deviceStateUpdate.getValue() - 1000)));
                } else {
                    sceneJobExecutor.addMediumPriorityJob(
                            new SceneConfigReadingJob(device, (short) (deviceStateUpdate.getValue() - 1000)));
                }
            } else if (deviceStateUpdate.getValue() >= 2000 && deviceStateUpdate.getValue() < 3000) {
                if (deviceStateUpdate.getType().equals(DeviceStateUpdate.UPDATE_SCENE_OUTPUT)) {
                    sceneJobExecutor.addLowPriorityJob(
                            new SceneOutputValueReadingJob(device, (short) (deviceStateUpdate.getValue() - 2000)));
                } else {
                    sceneJobExecutor.addLowPriorityJob(
                            new SceneConfigReadingJob(device, (short) (deviceStateUpdate.getValue() - 2000)));
                }
            } else {
                logger.debug("Device state update value {} is out of range. Please check the input!",
                        deviceStateUpdate.getValue());
                return;
            }
            logger.debug("Add new sceneReadingJob with priority: {} to SceneReadingJobExecuter",
                    new Integer(deviceStateUpdate.getValue()).toString().charAt(0));
        }
    }

    @Override
    public void registerDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getDeviceStatusListenerID();
            logger.debug("register DeviceListener with id: {}", id);
            if (id.equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                this.deviceDiscovery = deviceListener;
                for (Device device : strucMan.getDeviceMap().values()) {
                    deviceDiscovery.onDeviceAdded(device);
                }
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getDeviceStatusListenerID());
                if (intDevice != null) {
                    intDevice.registerDeviceStateListener(deviceListener);
                } else {
                    deviceListener.onDeviceRemoved(null);
                }
            }
        }
    }

    @Override
    public void unregisterDeviceListener(DeviceStatusListener deviceListener) {
        if (deviceListener != null) {
            String id = deviceListener.getDeviceStatusListenerID();
            if (id.equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                this.deviceDiscovery = null;
            } else {
                Device intDevice = strucMan.getDeviceByDSID(deviceListener.getDeviceStatusListenerID());
                if (intDevice != null) {
                    intDevice.unregisterDeviceStateListener();
                }
            }
        }
    }

    @Override
    public void removeDevice(String dSID) {
        Device intDevice = strucMan.getDeviceByDSID(dSID);
        if (intDevice != null) {
            strucMan.deleteDevice(intDevice);
            trashDevices.add(new TrashDevice(intDevice));
        }
    }

    @Override
    public void registerTotalPowerConsumptionListener(TotalPowerConsumptionListener totalPowerConsumptionListener) {
        this.totalPowerConsumptionListener = totalPowerConsumptionListener;
    }

    @Override
    public void unregisterTotalPowerConsumptionListener() {
        this.totalPowerConsumptionListener = null;
    }

    @Override
    public void registerSceneListener(SceneStatusListener sceneListener) {
        this.sceneMan.registerSceneListener(sceneListener);
    }

    @Override
    public void unregisterSceneListener(SceneStatusListener sceneListener) {
        this.sceneMan.unregisterSceneListener(sceneListener);
    }

    @Override
    public void registerStatusListener(ManagerStatusListener statusListener) {
        this.statusListener = statusListener;
        this.sceneMan.registerStatusListener(statusListener);
    }

    @Override
    public void unregisterStatusListener() {
        this.statusListener = null;
        this.sceneMan.unregisterStatusListener();
    }

    @Override
    public void registerConnectionListener(ConnectionListener connectionListener) {
        this.connMan.registerConnectionListener(connectionListener);
    }

    @Override
    public void unregisterConnectionListener() {
        this.connMan.unregisterConnectionListener();
    }

    @Override
    public int getTotalPowerConsumption() {
        List<CachedMeteringValue> cachedConsumptionMeteringValues = digitalSTROMClient
                .getLatest(connMan.getSessionToken(), MeteringTypeEnum.consumption, meters, MeteringUnitsEnum.W);
        if (cachedConsumptionMeteringValues != null) {
            tempConsumption = 0;
            for (CachedMeteringValue value : cachedConsumptionMeteringValues) {
                tempConsumption += value.getValue();
            }
            if (tempConsumption != totalPowerConsumption) {
                totalPowerConsumption = tempConsumption;
            }
        }
        return totalPowerConsumption;
    }

    @Override
    public int getTotalEnergyMeterValue() {
        List<CachedMeteringValue> cachedEnergyMeteringValues = digitalSTROMClient.getLatest(connMan.getSessionToken(),
                MeteringTypeEnum.energy, meters, MeteringUnitsEnum.Wh);
        if (cachedEnergyMeteringValues != null) {
            tempEnergyMeter = 0;
            for (CachedMeteringValue value : cachedEnergyMeteringValues) {
                tempEnergyMeter += value.getValue();
            }
            if (tempEnergyMeter != totalEnergyMeter) {
                totalEnergyMeter = tempEnergyMeter;
            }
        }
        return totalEnergyMeter;
    }
}
