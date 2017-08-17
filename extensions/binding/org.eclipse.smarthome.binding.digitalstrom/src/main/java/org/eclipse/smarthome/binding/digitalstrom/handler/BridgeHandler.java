/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import static org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
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
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.DeviceStatusManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.SceneManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.impl.StructureManagerImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHandler} is the handler for a digitalSTROM-Server and connects it to
 * the framework.<br>
 * All {@link DeviceHandler}s and {@link SceneHandler}s use the {@link BridgeHandler} to execute the actual
 * commands.
 * <p>
 * The {@link BridgeHandler} also:
 * <ul>
 * <li>manages the {@link DeviceStatusManager} (starts, stops, register {@link DeviceStatusListener},
 * register {@link SceneStatusListener} and so on)</li>
 * <li>creates and load the configurations in the {@link Config}.</li>
 * <li>implements {@link ManagerStatusListener} to manage the expiration of the Thing initializations</li>
 * <li>implements the {@link ConnectionListener} to manage the {@link ThingStatus} of this {@link BridgeHandler}</li>
 * <li>and implements the {@link TotalPowerConsumptionListener} to update his Channels.</li>
 * </ul>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class BridgeHandler extends BaseBridgeHandler
        implements ConnectionListener, TotalPowerConsumptionListener, ManagerStatusListener {

    private Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DSS_BRIDGE);

    /* DS-Manager */
    private ConnectionManager connMan;
    private StructureManager structMan;
    private SceneManager sceneMan;
    private DeviceStatusManager devStatMan;

    private List<SceneStatusListener> sceneListener;
    private List<DeviceStatusListener> devListener;
    private Config config = null;

    List<SceneStatusListener> unregisterSceneStatusListeners = null;
    private boolean generatingScenes = false;
    private int connectionTimeoutCounter = 0;

    private class Initializer implements Runnable {

        BridgeHandler bridge = null;
        Config config;

        public Initializer(BridgeHandler bridge, Config config) {
            this.bridge = bridge;
            this.config = config;
        }

        @Override
        public void run() {
            logger.info("Checking connection");
            if (connMan == null) {
                connMan = new ConnectionManagerImpl(config, bridge, true);
            } else {
                connMan.registerConnectionListener(bridge);
                connMan.configHasBeenUpdated();
            }

            logger.info("Initializing digitalSTROM Manager");
            if (structMan == null) {
                structMan = new StructureManagerImpl();
            }
            if (sceneMan == null) {
                sceneMan = new SceneManagerImpl(connMan, structMan);
            }
            if (devStatMan == null) {
                devStatMan = new DeviceStatusManagerImpl(connMan, structMan, sceneMan, bridge);
            } else {
                devStatMan.registerStatusListener(bridge);
            }
            structMan.generateZoneGroupNames(connMan);

            devStatMan.registerTotalPowerConsumptionListener(bridge);

            if (connMan.checkConnection()) {
                devStatMan.start();
            }

            boolean configChanged = false;
            Configuration configuration = bridge.getConfig();
            if (connMan.getApplicationToken() != null) {
                configuration.remove(USER_NAME);
                configuration.remove(PASSWORD);
                logger.debug("Application-Token is: {}", connMan.getApplicationToken());
                configuration.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                configChanged = true;
            }
            if (StringUtils.isNotBlank((String) configuration.get(DigitalSTROMBindingConstants.DS_NAME))
                    && connMan.checkConnection()) {
                String dSSname = connMan.getDigitalSTROMAPI().getInstallationName(connMan.getSessionToken());

                if (dSSname != null) {
                    configuration.put(DS_NAME, dSSname);
                }
            }
            if (configChanged) {
                updateConfiguration(configuration);
            }
            if (StringUtils.isBlank(getThing().getProperties().get(DigitalSTROMBindingConstants.SERVER_CERT))
                    && StringUtils.isNotBlank(config.getCert())) {
                updateProperty(DigitalSTROMBindingConstants.SERVER_CERT, config.getCert());
            }
        }
    };

    public BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing digitalSTROM-BridgeHandler");
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");
        // Start an extra thread to readout the configuration and check the connection, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        Config config = loadAndCheckConfig();

        if (config != null) {
            logger.debug("{}", config.toString());
            scheduler.execute(new Initializer(this, config));
        }
    }

    private boolean checkLoginConfig(Config config) {
        if ((StringUtils.isNotBlank(config.getUserName()) && StringUtils.isNotBlank(config.getPassword()))
                || StringUtils.isNotBlank(config.getAppToken())) {
            return true;
        }
        onConnectionStateChange(CONNECTION_LOST, NO_USER_PASSWORD);
        return false;
    }

    private Config loadAndCheckConfig() {
        Configuration thingConfig = super.getConfig();
        Config config = loadAndCheckConnectionData(thingConfig);
        if (config == null) {
            return null;
        }
        logger.info("Loading configuration");
        ArrayList<String> numberExc = null;
        // Parameters can't be null, because of an existing default value.
        if (StringUtils
                .isNotBlank(thingConfig.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVAL).toString())) {
            try {
                config.setSensordataRefreshInterval(Integer.parseInt(
                        thingConfig.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVAL).toString() + "000"));
            } catch (NumberFormatException e) {
                if (numberExc == null) {
                    numberExc = new ArrayList<String>();
                }
                numberExc.add("\"Sensor update interval\"");
            }
        }
        if (StringUtils
                .isNotBlank(thingConfig.get(DigitalSTROMBindingConstants.TOTAL_POWER_UPDATE_INTERVAL).toString())) {
            try {
                config.setTotalPowerUpdateInterval(Integer.parseInt(
                        thingConfig.get(DigitalSTROMBindingConstants.TOTAL_POWER_UPDATE_INTERVAL).toString() + "000"));
            } catch (NumberFormatException e) {
                if (numberExc == null) {
                    numberExc = new ArrayList<String>();
                }
                numberExc.add("\"Total power update interval\"");
            }
        }
        if (StringUtils.isNotBlank(thingConfig.get(DigitalSTROMBindingConstants.SENSOR_WAIT_TIME).toString())) {
            try {
                config.setSensorReadingWaitTime(Integer
                        .parseInt(thingConfig.get(DigitalSTROMBindingConstants.SENSOR_WAIT_TIME).toString() + "000"));
            } catch (NumberFormatException e) {
                if (numberExc == null) {
                    numberExc = new ArrayList<String>();
                }
                numberExc.add("\"Wait time sensor reading\"");
            }
        }
        if (StringUtils.isNotBlank(
                thingConfig.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY).toString())) {
            try {
                config.setTrashDeviceDeleteTime(Integer.parseInt(
                        thingConfig.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY).toString()));
            } catch (NumberFormatException e) {
                if (numberExc == null) {
                    numberExc = new ArrayList<String>();
                }
                numberExc.add("\"Days to be slaked trash bin devices\"");
            }
        }
        if (numberExc != null) {
            String excText = "The field ";
            for (int i = 0; i < numberExc.size(); i++) {
                excText = excText + numberExc.get(i);
                if (i < numberExc.size() - 2) {
                    excText = excText + ", ";
                } else if (i < numberExc.size() - 1) {
                    excText = excText + " and ";
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, excText + " have to be a number.");
            return null;
        }
        if (StringUtils.isNotBlank(getThing().getProperties().get(DigitalSTROMBindingConstants.SERVER_CERT))) {
            config.setCert(getThing().getProperties().get(DigitalSTROMBindingConstants.SERVER_CERT));
        }
        return config;
    }

    private Config loadAndCheckConnectionData(Configuration thingConfig) {
        if (this.config == null) {
            this.config = new Config();
        }
        // load and check connection and authorization data
        if (StringUtils.isNotBlank((String) thingConfig.get(HOST))) {
            config.setHost(thingConfig.get(HOST).toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the digitalSTROM-Server can't established, because the host address is missing. Please set the host address.");
            return null;
        }
        if (thingConfig.get(USER_NAME) != null) {
            config.setUserName(thingConfig.get(USER_NAME).toString());
        }
        if (thingConfig.get(PASSWORD) != null) {
            config.setPassword(thingConfig.get(PASSWORD).toString());
        }
        if (thingConfig.get(APPLICATION_TOKEN) != null) {
            config.setAppToken(thingConfig.get(APPLICATION_TOKEN).toString());
        }

        if (!checkLoginConfig(config)) {
            return null;
        }
        return config;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed");
        if (devStatMan != null) {
            devStatMan.unregisterTotalPowerConsumptionListener();
            devStatMan.unregisterStatusListener();
            this.devStatMan.stop();
        }
        if (connMan != null) {
            connMan.unregisterConnectionListener();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ID_TOTAL_ACTIVE_POWER:
                    updateState(channelUID, new DecimalType(devStatMan.getTotalPowerConsumption()));
                    break;
                case CHANNEL_ID_TOTAL_ELECTRIC_METER:
                    updateState(channelUID, new DecimalType(devStatMan.getTotalEnergyMeterValue()));
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void handleRemoval() {
        for (Thing thing : getThing().getThings()) {
            // Inform Thing-Child's about removed bridge.
            final ThingHandler thingHandler = thing.getHandler();
            if (thingHandler != null) {
                thingHandler.bridgeStatusChanged(ThingStatusInfoBuilder.create(ThingStatus.REMOVED).build());
            }
        }
        if (StringUtils.isNotBlank((String) super.getConfig().get(APPLICATION_TOKEN))) {
            if (connMan == null) {
                Config config = loadAndCheckConnectionData(this.getConfig());
                if (config != null) {
                    this.connMan = new ConnectionManagerImpl(config, null, false);
                } else {
                    updateStatus(ThingStatus.REMOVED);
                    return;
                }
            }
            if (connMan.removeApplicationToken()) {
                logger.debug("Application-Token deleted");
            }
        }
        updateStatus(ThingStatus.REMOVED);
    }

    /* methods to store listener */

    /**
     * Registers a new {@link DeviceStatusListener} on the {@link DeviceStatusManager}.
     *
     * @param deviceStatusListener (must not be null)
     */
    public synchronized void registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener == null) {
                throw new IllegalArgumentException("It's not allowed to pass null.");
            }

            if (deviceStatusListener.getDeviceStatusListenerID() != null) {
                devStatMan.registerDeviceListener(deviceStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a DeviceStatusListener with ID = null.");
            }
        } else {
            devListener = new LinkedList<DeviceStatusListener>();
            devListener.add(deviceStatusListener);
        }
    }

    /**
     * Unregisters a new {@link DeviceStatusListener} on the {@link BridgeHandler}.
     *
     * @param devicetatusListener (must not be null)
     */
    public void unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener.getDeviceStatusListenerID() != null) {
                this.devStatMan.unregisterDeviceListener(deviceStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a DeviceStatusListener with ID = null.");
            }
        }
    }

    /**
     * Registers a new {@link SceneStatusListener} on the {@link BridgeHandler}.
     *
     * @param deviceStatusListener (must not be null)
     */
    public synchronized void registerSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null && !generatingScenes) {
            if (sceneStatusListener == null) {
                throw new IllegalArgumentException("It's not allowed to pass null.");
            }

            if (sceneStatusListener.getSceneStatusListenerID() != null) {
                this.sceneMan.registerSceneListener(sceneStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a SceneStatusListener with ID = null.");
            }
        } else {
            if (sceneListener == null) {
                sceneListener = new LinkedList<SceneStatusListener>();
            }
            sceneListener.add(sceneStatusListener);
        }

    }

    /**
     * Unregisters a new {@link SceneStatusListener} on the {@link DeviceStatusManager}.
     *
     * @param sceneStatusListener (must not be null)
     */
    public void unregisterSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null && !generatingScenes) {
            if (sceneStatusListener.getSceneStatusListenerID() != null) {
                this.sceneMan.unregisterSceneListener(sceneStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a SceneStatusListener with ID = null..");
            }
        } else {
            if (unregisterSceneStatusListeners == null) {
                unregisterSceneStatusListeners = new LinkedList<SceneStatusListener>();
            }
            unregisterSceneStatusListeners.add(sceneStatusListener);
        }
    }

    /**
     * Has to be called from a removed Thing-Child to rediscovers the Thing.
     *
     * @param scene or device id (must not be null)
     */
    public void childThingRemoved(String id) {
        if (id.split("-").length == 3) {
            InternalScene scene = sceneMan.getInternalScene(id);
            if (scene != null) {
                sceneMan.removeInternalScene(id);
                sceneMan.addInternalScene(scene);
            }
        } else {
            devStatMan.removeDevice(id);
        }
    }

    /**
     * Delegate a stop command from a Thing to the {@link DeviceStatusManager#sendStopComandsToDSS(Device)}.
     *
     * @param device
     */
    public void stopOutputValue(Device device) {
        this.devStatMan.sendStopComandsToDSS(device);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_ID_TOTAL_ACTIVE_POWER:
                if (devStatMan != null) {
                    onTotalPowerConsumptionChanged(devStatMan.getTotalPowerConsumption());
                }
                break;
            case CHANNEL_ID_TOTAL_ELECTRIC_METER:
                if (devStatMan != null) {
                    onEnergyMeterValueChanged(devStatMan.getTotalEnergyMeterValue());
                }
        }
    }

    @Override
    public void onTotalPowerConsumptionChanged(int newPowerConsumption) {
        updateChannelState(CHANNEL_ID_TOTAL_ACTIVE_POWER, newPowerConsumption);
    }

    @Override
    public void onEnergyMeterValueChanged(int newEnergyMeterValue) {
        updateChannelState(CHANNEL_ID_TOTAL_ELECTRIC_METER, newEnergyMeterValue * 0.001);
    }

    private void updateChannelState(String channelID, double value) {
        if (getThing().getChannel(channelID) != null) {
            updateState(new ChannelUID(getThing().getUID(), channelID), new DecimalType(value));
        }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState) {
        switch (newConnectionState) {
            case CONNECTION_LOST:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "The connection to the digitalSTROM-Server cannot be established.");
                devStatMan.stop();
                break;
            case CONNECTION_RESUMED:
                if (connectionTimeoutCounter > 0) {
                    setStatus(ThingStatus.ONLINE);
                    devStatMan.start();
                }
                break;
            case APPLICATION_TOKEN_GENERATED:
                if (connMan != null) {
                    Configuration config = this.getConfig();
                    if (config != null) {
                        config.remove(USER_NAME);
                        config.remove(PASSWORD);
                        config.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                        this.updateConfiguration(config);
                    }
                }
                return;
            default:
        }
        // reset connection timeout counter
        connectionTimeoutCounter = 0;
    }

    private void setStatus(ThingStatus status) {
        updateStatus(status);
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                handler.initialize();
            }
        }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState, String reason) {
        if (newConnectionState.equals(NOT_AUTHENTICATED) || newConnectionState.equals(CONNECTION_LOST)) {
            switch (reason) {
                case WRONG_APP_TOKEN:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "User defined Application-Token is wrong. "
                                    + "Please set user name and password to generate an Application-Token or set an valide Application-Token.");
                    break;
                case WRONG_USER_OR_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "The set username or password is wrong.");
                    break;
                case NO_USER_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No username or password is set to generate Application-Token. Please set user name and password or Application-Token.");
                    break;
                case CONNECTON_TIMEOUT:
                    // ignore the first connection timeout
                    if (connectionTimeoutCounter++ > 0) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection lost because connection timeout to Server.");
                    }
                    return;
                case HOST_NOT_FOUND:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Server not found! Please check these points:\n" + " - Is digitalSTROM-Server turned on?\n"
                                    + " - Is the host address correct?\n"
                                    + " - Is the ethernet cable connection established?");
                    break;
                case UNKNOWN_HOST:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unkown host name, please check the set host name!");
                    break;
                case INVALID_URL:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid URL is set.");
                    break;
                default:
            }
            // reset connection timeout counter
            connectionTimeoutCounter = 0;
            if (devStatMan != null) {
                devStatMan.stop();
            }
        }

    }

    /**
     * Returns a list of all {@link Device}'s.
     *
     * @return device list (cannot be null)
     */
    public List<Device> getDevices() {
        return this.structMan != null && this.structMan.getDeviceMap() != null
                ? new LinkedList<Device>(this.structMan.getDeviceMap().values())
                : null;
    }

    /**
     * Returns the {@link StructureManager}.
     *
     * @return StructureManager
     */
    public StructureManager getStructureManager() {
        return this.structMan;
    }

    /**
     * Delegates a scene command of a Thing to the
     * {@link DeviceStatusManager#sendSceneComandsToDSS(InternalScene, boolean)}
     *
     * @param the called scene
     * @param call_undo (true = call scene | false = undo scene)
     */
    public void sendSceneComandToDSS(InternalScene scene, boolean call_undo) {
        if (devStatMan != null) {
            devStatMan.sendSceneComandsToDSS(scene, call_undo);
        }
    }

    /**
     * Delegates a device command of a Thing to the
     * {@link DeviceStatusManager#sendComandsToDSS(Device, DeviceStateUpdate)}
     *
     * @param device
     * @param deviceStateUpdate
     */
    public void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (devStatMan != null) {
            devStatMan.sendComandsToDSS(device, deviceStateUpdate);
        }
    }

    /**
     * Returns a list of all {@link InternalScene}'s.
     *
     * @return Scene list (cannot be null)
     */
    public List<InternalScene> getScenes() {
        return sceneMan != null ? sceneMan.getScenes() : new LinkedList<InternalScene>();
    }

    /**
     * Returns the {@link ConnectionManager}.
     *
     * @return ConnectionManager
     */
    public ConnectionManager getConnectionManager() {
        return this.connMan;
    }

    @Override
    public void onStatusChanged(ManagerTypes managerType, ManagerStates state) {
        if (managerType.equals(ManagerTypes.DEVICE_STATUS_MANAGER)) {
            switch (state) {
                case INITIALIZING:
                    logger.info("Building digitalSTROM model");
                    break;
                case RUNNING:
                    if (devListener != null) {
                        for (DeviceStatusListener deviceListener : devListener) {
                            devStatMan.registerDeviceListener(deviceListener);
                        }
                    }
                    setStatus(ThingStatus.ONLINE);
                    break;
                case STOPPED:
                    if (!getThing().getStatusInfo().getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)
                            && !getThing().getStatusInfo().getStatusDetail()
                                    .equals(ThingStatusDetail.CONFIGURATION_ERROR)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "DeviceStatusManager is stopped.");
                    }
                    break;
                default:
                    break;
            }
        }
        if (managerType.equals(ManagerTypes.SCENE_MANAGER)) {
            switch (state) {
                case GENERATING_SCENES:
                    generatingScenes = true;
                    break;
                case RUNNING:
                    if (unregisterSceneStatusListeners != null) {
                        for (SceneStatusListener sceneListener : this.unregisterSceneStatusListeners) {
                            sceneMan.registerSceneListener(sceneListener);
                        }
                    }
                    if (sceneListener != null) {
                        for (SceneStatusListener sceneListener : this.sceneListener) {
                            sceneMan.registerSceneListener(sceneListener);
                        }
                    }
                    generatingScenes = false;
                    break;
                default:
                    break;
            }
        }
    }
}
