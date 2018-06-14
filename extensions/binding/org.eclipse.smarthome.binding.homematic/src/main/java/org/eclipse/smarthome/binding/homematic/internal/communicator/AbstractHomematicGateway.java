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
package org.eclipse.smarthome.binding.homematic.internal.communicator;

import static org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.binding.homematic.internal.common.HomematicConfig;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.BinRpcClient;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.RpcClient;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.TransferMode;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.UnknownParameterSetException;
import org.eclipse.smarthome.binding.homematic.internal.communicator.client.XmlRpcClient;
import org.eclipse.smarthome.binding.homematic.internal.communicator.server.BinRpcServer;
import org.eclipse.smarthome.binding.homematic.internal.communicator.server.RpcEventListener;
import org.eclipse.smarthome.binding.homematic.internal.communicator.server.RpcServer;
import org.eclipse.smarthome.binding.homematic.internal.communicator.server.XmlRpcServer;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.BatteryTypeVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.DeleteDeviceModeVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.DeleteDeviceVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.DisplayOptionsVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.DisplayTextVirtualDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.FirmwareVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.HmwIoModuleVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.InstallModeDurationVirtualDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.InstallModeVirtualDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.OnTimeAutomaticVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.PressVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.ReloadAllFromGatewayVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.ReloadFromGatewayVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.ReloadRssiVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.RssiVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.SignalStrengthVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.StateContactVirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.VirtualDatapointHandler;
import org.eclipse.smarthome.binding.homematic.internal.communicator.virtual.VirtualGateway;
import org.eclipse.smarthome.binding.homematic.internal.misc.DelayedExecuter;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicClientException;
import org.eclipse.smarthome.binding.homematic.internal.misc.HomematicConstants;
import org.eclipse.smarthome.binding.homematic.internal.misc.MiscUtils;
import org.eclipse.smarthome.binding.homematic.internal.misc.DelayedExecuter.DelayedExecuterCallback;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointConfig;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapointInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.binding.homematic.internal.model.HmGatewayInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.internal.model.HmRssiInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.HmValueType;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractHomematicGateway} is the main class for the communication with a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AbstractHomematicGateway implements RpcEventListener, HomematicGateway, VirtualGateway {
    private final Logger logger = LoggerFactory.getLogger(AbstractHomematicGateway.class);
    public static final double DEFAULT_DISABLE_DELAY = 2.0;
    private static final long CONNECTION_TRACKER_INTERVAL_SECONDS = 15;
    private static final String GATEWAY_POOL_NAME = "homematicGateway";

    private Map<TransferMode, RpcClient<?>> rpcClients = new HashMap<TransferMode, RpcClient<?>>();
    private Map<TransferMode, RpcServer> rpcServers = new HashMap<TransferMode, RpcServer>();

    protected HomematicConfig config;
    protected HttpClient httpClient;
    private String id;
    private HomematicGatewayAdapter gatewayAdapter;
    private DelayedExecuter sendDelayedExecutor = new DelayedExecuter();
    private DelayedExecuter receiveDelayedExecutor = new DelayedExecuter();
    private Set<HmDatapointInfo> echoEvents = Collections.synchronizedSet(new HashSet<HmDatapointInfo>());
    private ScheduledFuture<?> connectionTrackerFuture;
    private ConnectionTrackerThread connectionTrackerThread;
    private Map<String, HmDevice> devices = Collections.synchronizedMap(new HashMap<String, HmDevice>());
    private Map<HmInterface, TransferMode> availableInterfaces = new TreeMap<HmInterface, TransferMode>();
    private static List<VirtualDatapointHandler> virtualDatapointHandlers = new ArrayList<VirtualDatapointHandler>();
    private boolean cancelLoadAllMetadata;
    private boolean initialized;
    private boolean newDeviceEventsEnabled;
    private ScheduledFuture<?> enableNewDeviceFuture;
    private ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(GATEWAY_POOL_NAME);

    static {
        // loads all virtual datapoints
        virtualDatapointHandlers.add(new BatteryTypeVirtualDatapointHandler());
        virtualDatapointHandlers.add(new FirmwareVirtualDatapointHandler());
        virtualDatapointHandlers.add(new DisplayOptionsVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadFromGatewayVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadAllFromGatewayVirtualDatapointHandler());
        virtualDatapointHandlers.add(new OnTimeAutomaticVirtualDatapointHandler());
        virtualDatapointHandlers.add(new InstallModeVirtualDatapoint());
        virtualDatapointHandlers.add(new InstallModeDurationVirtualDatapoint());
        virtualDatapointHandlers.add(new DeleteDeviceModeVirtualDatapointHandler());
        virtualDatapointHandlers.add(new DeleteDeviceVirtualDatapointHandler());
        virtualDatapointHandlers.add(new RssiVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadRssiVirtualDatapointHandler());
        virtualDatapointHandlers.add(new StateContactVirtualDatapointHandler());
        virtualDatapointHandlers.add(new SignalStrengthVirtualDatapointHandler());
        virtualDatapointHandlers.add(new DisplayTextVirtualDatapoint());
        virtualDatapointHandlers.add(new HmwIoModuleVirtualDatapointHandler());
        virtualDatapointHandlers.add(new PressVirtualDatapointHandler());
    }

    public AbstractHomematicGateway(String id, HomematicConfig config, HomematicGatewayAdapter gatewayAdapter,
            HttpClient httpClient) {
        this.id = id;
        this.config = config;
        this.gatewayAdapter = gatewayAdapter;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() throws IOException {
        logger.debug("Initializing gateway with id '{}'", id);

        HmGatewayInfo gatewayInfo = config.getGatewayInfo();
        if (gatewayInfo.isHomegear()) {
            // Homegear
            availableInterfaces.put(HmInterface.RF, TransferMode.BIN_RPC);
        } else if (gatewayInfo.isCCU()) {
            // CCU
            availableInterfaces.put(HmInterface.RF, TransferMode.BIN_RPC);
            if (gatewayInfo.isWiredInterface()) {
                availableInterfaces.put(HmInterface.WIRED, TransferMode.BIN_RPC);
            }
            if (gatewayInfo.isHmipInterface()) {
                availableInterfaces.put(HmInterface.HMIP, TransferMode.XML_RPC);
            }
            if (gatewayInfo.isCuxdInterface()) {
                availableInterfaces.put(HmInterface.CUXD, TransferMode.BIN_RPC);
            }
            if (gatewayInfo.isGroupInterface()) {
                availableInterfaces.put(HmInterface.GROUP, TransferMode.XML_RPC);
            }
        } else {
            // other
            availableInterfaces.put(HmInterface.RF, TransferMode.XML_RPC);
            if (gatewayInfo.isWiredInterface()) {
                availableInterfaces.put(HmInterface.WIRED, TransferMode.XML_RPC);
            }
            if (gatewayInfo.isHmipInterface()) {
                availableInterfaces.put(HmInterface.HMIP, TransferMode.XML_RPC);
            }
        }

        logger.info("{}", config.getGatewayInfo());
        StringBuilder sb = new StringBuilder();
        for (Entry<HmInterface, TransferMode> entry : availableInterfaces.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        logger.debug("Used Homematic transfer modes: {}", sb.toString());
        startClients();
        startServers();

        if (!config.getGatewayInfo().isHomegear()) {
            // delay the newDevice event handling at startup, reduces some API calls
            long delay = config.getGatewayInfo().isCCU1() ? 10 : 3;
            enableNewDeviceFuture = scheduler.schedule(() -> {
                newDeviceEventsEnabled = true;
            }, delay, TimeUnit.MINUTES);
        } else {
            newDeviceEventsEnabled = true;
        }
    }

    @Override
    public void dispose() {
        initialized = false;
        if (enableNewDeviceFuture != null) {
            enableNewDeviceFuture.cancel(true);
        }
        newDeviceEventsEnabled = false;
        stopWatchdogs();
        sendDelayedExecutor.stop();
        receiveDelayedExecutor.stop();
        stopServers();
        stopClients();
        devices.clear();
        echoEvents.clear();
        availableInterfaces.clear();
        config.setGatewayInfo(null);
    }

    /**
     * Starts the Homematic gateway client.
     */
    protected void startClients() throws IOException {
        for (TransferMode mode : availableInterfaces.values()) {
            if (!rpcClients.containsKey(mode)) {
                rpcClients.put(mode,
                        mode == TransferMode.XML_RPC ? new XmlRpcClient(config, httpClient) : new BinRpcClient(config));
            }
        }
    }

    /**
     * Stops the Homematic gateway client.
     */
    protected void stopClients() {
        for (RpcClient<?> rpcClient : rpcClients.values()) {
            rpcClient.dispose();
        }
        rpcClients.clear();
    }

    /**
     * Starts the Homematic RPC server.
     */
    private void startServers() throws IOException {
        for (TransferMode mode : availableInterfaces.values()) {
            if (!rpcServers.containsKey(mode)) {
                RpcServer rpcServer = mode == TransferMode.XML_RPC ? new XmlRpcServer(this, config)
                        : new BinRpcServer(this, config);
                rpcServers.put(mode, rpcServer);
                rpcServer.start();
            }
        }
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            getRpcClient(hmInterface).init(hmInterface, hmInterface.toString() + "-" + id);
        }
    }

    /**
     * Stops the Homematic RPC server.
     */
    private void stopServers() {
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            try {
                getRpcClient(hmInterface).release(hmInterface);
            } catch (IOException ex) {
                // recoverable exception, therefore only debug
                logger.debug("Unable to release the connection to the gateway with id '{}': {}", id, ex.getMessage(),
                        ex);
            }
        }

        for (TransferMode mode : rpcServers.keySet()) {
            rpcServers.get(mode).shutdown();
        }
        rpcServers.clear();
    }

    @Override
    public void startWatchdogs() {
        logger.debug("Starting connection tracker for gateway with id '{}'", id);
        connectionTrackerThread = new ConnectionTrackerThread();
        connectionTrackerFuture = scheduler.scheduleWithFixedDelay(connectionTrackerThread, 30,
                CONNECTION_TRACKER_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopWatchdogs() {
        if (connectionTrackerFuture != null) {
            connectionTrackerFuture.cancel(true);
        }
        connectionTrackerThread = null;
    }

    /**
     * Returns the default interface to communicate with the Homematic gateway.
     */
    protected HmInterface getDefaultInterface() {
        return HmInterface.RF;
    }

    @Override
    public RpcClient<?> getRpcClient(HmInterface hmInterface) throws IOException {
        RpcClient<?> rpcClient = rpcClients.get(availableInterfaces.get(hmInterface));
        if (rpcClient == null) {
            throw new IOException("RPC client for interface " + hmInterface + " not available");
        }
        return rpcClient;
    }

    /**
     * Loads all gateway variables into the given device.
     */
    protected abstract void loadVariables(HmChannel channel) throws IOException;

    /**
     * Loads all gateway scripts into the given device.
     */
    protected abstract void loadScripts(HmChannel channel) throws IOException;

    /**
     * Loads all names of the devices.
     */
    protected abstract void loadDeviceNames(Collection<HmDevice> devices) throws IOException;

    /**
     * Sets a variable on the Homematic gateway.
     */
    protected abstract void setVariable(HmDatapoint dp, Object value) throws IOException;

    /**
     * Execute a script on the Homematic gateway.
     */
    protected abstract void executeScript(HmDatapoint dp) throws IOException;

    @Override
    public HmDatapoint getDatapoint(HmDatapointInfo dpInfo) throws HomematicClientException {
        HmDevice device = getDevice(dpInfo.getAddress());
        HmChannel channel = device.getChannel(dpInfo.getChannel());
        if (channel == null) {
            throw new HomematicClientException(String.format("Channel %s in device '%s' not found on gateway '%s'",
                    dpInfo.getChannel(), dpInfo.getAddress(), id));
        }
        HmDatapoint dp = channel.getDatapoint(dpInfo);
        if (dp == null) {
            throw new HomematicClientException(String.format("Datapoint '%s' not found on gateway '%s'", dpInfo, id));
        }
        return dp;
    }

    @Override
    public HmDevice getDevice(String address) throws HomematicClientException {
        HmDevice device = devices.get(address);
        if (device == null) {
            throw new HomematicClientException(
                    String.format("Device with address '%s' not found on gateway '%s'", address, id));
        }
        return device;
    }

    @Override
    public void cancelLoadAllDeviceMetadata() {
        cancelLoadAllMetadata = true;
    }

    @Override
    public void loadAllDeviceMetadata() throws IOException {
        cancelLoadAllMetadata = false;
        // load all device descriptions
        List<HmDevice> deviceDescriptions = getDeviceDescriptions();

        // loading datapoints for all channels
        Set<String> loadedDevices = new HashSet<String>();
        Map<String, Collection<HmDatapoint>> datapointsByChannelIdCache = new HashMap<String, Collection<HmDatapoint>>();
        for (HmDevice device : deviceDescriptions) {
            if (!cancelLoadAllMetadata) {
                try {
                    logger.trace("Loading metadata for device '{}' of type '{}'", device.getAddress(),
                            device.getType());
                    if (device.isGatewayExtras()) {
                        loadChannelValues(device.getChannel(HmChannel.CHANNEL_NUMBER_VARIABLE));
                        loadChannelValues(device.getChannel(HmChannel.CHANNEL_NUMBER_SCRIPT));
                    } else {
                        for (HmChannel channel : device.getChannels()) {
                            logger.trace("  Loading channel {}", channel);
                            // speed up metadata generation a little bit for equal channels in the gateway devices
                            if ((DEVICE_TYPE_VIRTUAL.equals(device.getType())
                                    || DEVICE_TYPE_VIRTUAL_WIRED.equals(device.getType())) && channel.getNumber() > 1) {
                                HmChannel previousChannel = device.getChannel(channel.getNumber() - 1);
                                cloneAllDatapointsIntoChannel(channel, previousChannel.getDatapoints());
                            } else {
                                String channelId = String.format("%s:%s:%s", channel.getDevice().getType(),
                                        channel.getDevice().getFirmware(), channel.getNumber());
                                Collection<HmDatapoint> cachedDatapoints = datapointsByChannelIdCache.get(channelId);
                                if (cachedDatapoints != null) {
                                    // clone all datapoints
                                    cloneAllDatapointsIntoChannel(channel, cachedDatapoints);
                                } else {
                                    logger.trace("    Loading datapoints into channel {}", channel);
                                    addChannelDatapoints(channel, HmParamsetType.MASTER);
                                    addChannelDatapoints(channel, HmParamsetType.VALUES);

                                    // Make sure to only cache non-reconfigurable channels. For reconfigurable channels,
                                    // the data point set might change depending on the selected mode.
                                    if (!channel.isReconfigurable()) {
                                        datapointsByChannelIdCache.put(channelId, channel.getDatapoints());
                                    }
                                }
                            }
                        }
                    }
                    prepareDevice(device);
                    loadedDevices.add(device.getAddress());
                    gatewayAdapter.onDeviceLoaded(device);
                } catch (IOException ex) {
                    logger.warn("Can't load device with address '{}' from gateway '{}': {}", device.getAddress(), id,
                            ex.getMessage());
                }
            }
        }
        if (!cancelLoadAllMetadata) {
            devices.keySet().retainAll(loadedDevices);
        }
        initialized = true;
    }

    /**
     * Loads all datapoints from the gateway.
     */
    protected void addChannelDatapoints(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            getRpcClient(channel.getDevice().getHmInterface()).addChannelDatapoints(channel, paramsetType);
        } catch (UnknownParameterSetException ex) {
            logger.info(
                    "Can not load metadata for device: {}, channel: {}, paramset: {}, maybe there are no channels available",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);
        }
    }

    /**
     * Loads all device descriptions from the gateway.
     */
    private List<HmDevice> getDeviceDescriptions() throws IOException {
        List<HmDevice> deviceDescriptions = new ArrayList<HmDevice>();
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            deviceDescriptions.addAll(getRpcClient(hmInterface).listDevices(hmInterface));
        }
        if (!cancelLoadAllMetadata) {
            deviceDescriptions.add(createGatewayDevice());
            loadDeviceNames(deviceDescriptions);
        }
        return deviceDescriptions;
    }

    /**
     * Clones all datapoints into the given channel.
     */
    private void cloneAllDatapointsIntoChannel(HmChannel channel, Collection<HmDatapoint> datapoints) {
        logger.trace("    Cloning {} datapoints into channel {}", datapoints.size(), channel);
        for (HmDatapoint dp : datapoints) {
            if (!dp.isVirtual()) {
                HmDatapoint clonedDp = dp.clone();
                clonedDp.setValue(null);
                channel.addDatapoint(clonedDp);
            }
        }
    }

    @Override
    public void loadChannelValues(HmChannel channel) throws IOException {
        if (channel.getDevice().isGatewayExtras()) {
            if (channel.getNumber() != HmChannel.CHANNEL_NUMBER_EXTRAS) {
                List<HmDatapoint> datapoints = channel.getDatapoints();

                if (channel.getNumber() == HmChannel.CHANNEL_NUMBER_VARIABLE) {
                    loadVariables(channel);
                    logger.debug("Loaded {} gateway variable(s)", datapoints.size());
                } else if (channel.getNumber() == HmChannel.CHANNEL_NUMBER_SCRIPT) {
                    loadScripts(channel);
                    logger.debug("Loaded {} gateway script(s)", datapoints.size());
                }
            }
        } else {
            logger.debug("Loading values for channel {} of device '{}'", channel, channel.getDevice().getAddress());
            setChannelDatapointValues(channel, HmParamsetType.MASTER);
            setChannelDatapointValues(channel, HmParamsetType.VALUES);
        }

        for (HmDatapoint dp : channel.getDatapoints()) {
            handleVirtualDatapointEvent(dp, false);
        }

        channel.setInitialized(true);
    }

    @Override
    public void updateChannelValueDatapoints(HmChannel channel) throws IOException {
        logger.debug("Updating value datapoints for channel {} of device '{}', has {} datapoints before", channel,
                channel.getDevice().getAddress(), channel.getDatapoints().size());

        channel.removeValueDatapoints();
        addChannelDatapoints(channel, HmParamsetType.VALUES);
        setChannelDatapointValues(channel, HmParamsetType.VALUES);

        logger.debug("Updated value datapoints for channel {} of device '{}' (function {}), now has {} datapoints",
                channel, channel.getDevice().getAddress(), channel.getCurrentFunction(),
                channel.getDatapoints().size());
    }

    /**
     * Sets all datapoint values for the given channel.
     */
    protected void setChannelDatapointValues(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            getRpcClient(channel.getDevice().getHmInterface()).setChannelDatapointValues(channel, paramsetType);
        } catch (UnknownParameterSetException ex) {
            logger.info(
                    "Can not load values for device: {}, channel: {}, paramset: {}, maybe there are no values available",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);
        }
    }

    @Override
    public void loadRssiValues() throws IOException {
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            if (hmInterface == HmInterface.RF || hmInterface == HmInterface.CUXD) {
                List<HmRssiInfo> rssiInfos = getRpcClient(hmInterface).loadRssiInfo(hmInterface);
                for (HmRssiInfo hmRssiInfo : rssiInfos) {
                    updateRssiInfo(hmRssiInfo.getAddress(), DATAPOINT_NAME_RSSI_DEVICE, hmRssiInfo.getDevice());
                    updateRssiInfo(hmRssiInfo.getAddress(), DATAPOINT_NAME_RSSI_PEER, hmRssiInfo.getPeer());
                }
            }
        }
    }

    @Override
    public void setInstallMode(boolean enable, int seconds) throws IOException {
        HmDevice gwExtrasHm = devices.get(HmDevice.ADDRESS_GATEWAY_EXTRAS);

        if (gwExtrasHm != null) {
            // since the homematic virtual device exist: try setting install mode via its dataPoints
            HmDatapoint installModeDataPoint = null;
            HmDatapoint installModeDurationDataPoint = null;

            // collect virtual datapoints to be accessed
            HmChannel hmChannel = gwExtrasHm.getChannel(HmChannel.CHANNEL_NUMBER_EXTRAS);
            HmDatapointInfo installModeDurationDataPointInfo = new HmDatapointInfo(HmParamsetType.VALUES, hmChannel,
                    HomematicConstants.VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION);
            if (enable) {
                installModeDurationDataPoint = hmChannel.getDatapoint(installModeDurationDataPointInfo);
            }

            HmDatapointInfo installModeDataPointInfo = new HmDatapointInfo(HmParamsetType.VALUES, hmChannel,
                    HomematicConstants.VIRTUAL_DATAPOINT_NAME_INSTALL_MODE);

            installModeDataPoint = hmChannel.getDatapoint(installModeDataPointInfo);

            // first set duration on the datapoint
            if (installModeDurationDataPoint != null) {
                try {
                    VirtualDatapointHandler handler = getVirtualDatapointHandler(installModeDurationDataPoint, null);
                    handler.handleCommand(this, installModeDurationDataPoint, new HmDatapointConfig(), seconds);

                    // notify thing if exists
                    gatewayAdapter.onStateUpdated(installModeDurationDataPoint);
                } catch (HomematicClientException ex) {
                    logger.warn("Failed to send datapoint {}", installModeDurationDataPoint, ex);
                }
            }

            // now that the duration is set, we can enable / disable
            if (installModeDataPoint != null) {
                try {
                    VirtualDatapointHandler handler = getVirtualDatapointHandler(installModeDataPoint, null);
                    handler.handleCommand(this, installModeDataPoint, new HmDatapointConfig(), enable);

                    // notify thing if exists
                    gatewayAdapter.onStateUpdated(installModeDataPoint);

                    return;
                } catch (HomematicClientException ex) {
                    logger.warn("Failed to send datapoint {}", installModeDataPoint, ex);
                }
            }
        }

        // no gwExtrasHm available (or previous approach failed), therefore use rpc client directly
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            if (hmInterface == HmInterface.RF || hmInterface == HmInterface.CUXD) {
                getRpcClient(hmInterface).setInstallMode(hmInterface, enable, seconds);
            }
        }
    }

    @Override
    public int getInstallMode() throws IOException {
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            if (hmInterface == HmInterface.RF || hmInterface == HmInterface.CUXD) {
                return getRpcClient(hmInterface).getInstallMode(hmInterface);
            }
        }

        throw new IllegalStateException("Could not determine install mode because no suitable interface exists");
    }

    private void updateRssiInfo(String address, String datapointName, Integer value) {
        HmDatapointInfo dpInfo = new HmDatapointInfo(address, HmParamsetType.VALUES, 0, datapointName);
        HmChannel channel;
        try {
            channel = getDevice(dpInfo.getAddress()).getChannel(0);
            if (channel != null) {
                eventReceived(dpInfo, value);
            }
        } catch (HomematicClientException e) {
            // ignore
        }
    }

    @Override
    public void triggerDeviceValuesReload(HmDevice device) {
        logger.debug("Triggering values reload for device '{}'", device.getAddress());
        for (HmChannel channel : device.getChannels()) {
            channel.setInitialized(false);
        }
        gatewayAdapter.reloadDeviceValues(device);
    }

    @Override
    public void sendDatapointIgnoreVirtual(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException {
        sendDatapoint(dp, dpConfig, newValue, true);
    }

    @Override
    public void sendDatapoint(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException {
        sendDatapoint(dp, dpConfig, newValue, false);
    }

    /**
     * Main method for sending datapoints to the gateway. It handles scripts, variables, virtual datapoints, delayed
     * executions and auto disabling.
     */
    private void sendDatapoint(final HmDatapoint dp, final HmDatapointConfig dpConfig, final Object newValue,
            final boolean ignoreVirtualDatapoints) throws IOException, HomematicClientException {
        final HmDatapointInfo dpInfo = new HmDatapointInfo(dp);
        if (dp.isPressDatapoint() || (config.getGatewayInfo().isHomegear() && dp.isVariable())) {
            echoEvents.add(dpInfo);
        }
        if (dp.isReadOnly()) {
            logger.warn("Datapoint is readOnly, it is not published to the gateway with id '{}': '{}'", id, dpInfo);
        } else if (HmValueType.ACTION == dp.getType() && MiscUtils.isFalseValue(newValue)) {
            logger.warn(
                    "Datapoint of type ACTION cannot be set to false, it is not published to the gateway with id '{}': '{}'",
                    id, dpInfo);
        } else {
            final VirtualGateway gateway = this;
            sendDelayedExecutor.start(dpInfo, dpConfig.getDelay(), new DelayedExecuterCallback() {

                @Override
                public void execute() throws IOException, HomematicClientException {
                    VirtualDatapointHandler virtualDatapointHandler = ignoreVirtualDatapoints ? null
                            : getVirtualDatapointHandler(dp, newValue);
                    if (virtualDatapointHandler != null) {
                        logger.debug("Handling virtual datapoint '{}' on gateway with id '{}'", dp.getName(), id);
                        virtualDatapointHandler.handleCommand(gateway, dp, dpConfig, newValue);
                    } else if (dp.isScript()) {
                        if (MiscUtils.isTrueValue(newValue)) {
                            logger.debug("Executing script '{}' on gateway with id '{}'", dp.getInfo(), id);
                            executeScript(dp);
                        }
                    } else if (dp.isVariable()) {
                        logger.debug("Sending variable '{}' with value '{}' to gateway with id '{}'", dp.getInfo(),
                                newValue, id);
                        setVariable(dp, newValue);
                    } else {
                        logger.debug("Sending datapoint '{}' with value '{}' to gateway with id '{}'", dpInfo, newValue,
                                id);
                        getRpcClient(dp.getChannel().getDevice().getHmInterface()).setDatapointValue(dp, newValue);
                    }
                    dp.setValue(newValue);

                    if (MiscUtils.isTrueValue(newValue)
                            && (dp.isPressDatapoint() || dp.isScript() || dp.isActionType())) {
                        disableDatapoint(dp, DEFAULT_DISABLE_DELAY);
                    }
                }
            });
        }
    }

    /**
     * Returns a VirtualDatapointHandler for the given datapoint if available.
     */
    private VirtualDatapointHandler getVirtualDatapointHandler(HmDatapoint dp, Object value) {
        for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
            if (vdph.canHandleCommand(dp, value)) {
                return vdph;
            }
        }
        return null;
    }

    private void handleVirtualDatapointEvent(HmDatapoint dp, boolean publishToGateway) {
        for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
            if (vdph.canHandleEvent(dp)) {
                vdph.handleEvent(this, dp);
                if (publishToGateway) {
                    gatewayAdapter.onStateUpdated(vdph.getVirtualDatapoint(dp.getChannel()));
                }
            }
        }
    }

    @Override
    public void eventReceived(HmDatapointInfo dpInfo, Object newValue) {
        String className = newValue == null ? "Unknown" : newValue.getClass().getSimpleName();
        logger.debug("Received new ({}) value '{}' for '{}' from gateway with id '{}'", className, newValue, dpInfo,
                id);

        if (echoEvents.remove(dpInfo)) {
            logger.debug("Echo event detected, ignoring '{}'", dpInfo);
        } else {
            try {
                if (connectionTrackerThread != null && dpInfo.isPong() && id.equals(newValue)) {
                    connectionTrackerThread.pongReceived();
                }
                if (initialized) {
                    final HmDatapoint dp = getDatapoint(dpInfo);
                    HmDatapointConfig config = gatewayAdapter.getDatapointConfig(dp);
                    receiveDelayedExecutor.start(dpInfo, config.getReceiveDelay(), () -> {
                        dp.setValue(newValue);

                        gatewayAdapter.onStateUpdated(dp);
                        handleVirtualDatapointEvent(dp, true);
                        if (dp.isPressDatapoint() && MiscUtils.isTrueValue(dp.getValue())) {
                            disableDatapoint(dp, DEFAULT_DISABLE_DELAY);
                        }
                    });
                }
            } catch (HomematicClientException | IOException ex) {
                // ignore
            }
        }
    }

    @Override
    public void newDevices(List<String> adresses) {
        if (initialized && newDeviceEventsEnabled) {
            for (String address : adresses) {
                try {
                    logger.debug("New device '{}' detected on gateway with id '{}'", address, id);
                    List<HmDevice> deviceDescriptions = getDeviceDescriptions();
                    for (HmDevice device : deviceDescriptions) {
                        if (device.getAddress().equals(address)) {
                            for (HmChannel channel : device.getChannels()) {
                                addChannelDatapoints(channel, HmParamsetType.MASTER);
                                addChannelDatapoints(channel, HmParamsetType.VALUES);
                            }
                            prepareDevice(device);
                            gatewayAdapter.onNewDevice(device);
                        }
                    }
                } catch (Exception ex) {
                    logger.error("{}", ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public void deleteDevices(List<String> addresses) {
        if (initialized) {
            for (String address : addresses) {
                logger.debug("Device '{}' removed from gateway with id '{}'", address, id);
                HmDevice device = devices.remove(address);
                if (device != null) {
                    gatewayAdapter.onDeviceDeleted(device);
                }
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public HomematicGatewayAdapter getGatewayAdapter() {
        return gatewayAdapter;
    }

    /**
     * Creates a virtual device for handling variables, scripts and other special gateway functions.
     */
    private HmDevice createGatewayDevice() {
        String type = String.format("%s-%s", HmDevice.TYPE_GATEWAY_EXTRAS, StringUtils.upperCase(id));
        HmDevice device = new HmDevice(HmDevice.ADDRESS_GATEWAY_EXTRAS, getDefaultInterface(), type,
                config.getGatewayInfo().getId(), null, null);
        device.setName(HmDevice.TYPE_GATEWAY_EXTRAS);

        device.addChannel(new HmChannel(HmChannel.TYPE_GATEWAY_EXTRAS, HmChannel.CHANNEL_NUMBER_EXTRAS));
        device.addChannel(new HmChannel(HmChannel.TYPE_GATEWAY_VARIABLE, HmChannel.CHANNEL_NUMBER_VARIABLE));
        device.addChannel(new HmChannel(HmChannel.TYPE_GATEWAY_SCRIPT, HmChannel.CHANNEL_NUMBER_SCRIPT));

        return device;
    }

    /**
     * Adds virtual datapoints to the device.
     */
    private void prepareDevice(HmDevice device) {
        for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
            vdph.initialize(device);

        }
        devices.put(device.getAddress(), device);
        logger.debug("Loaded device '{}' ({}) with {} datapoints", device.getAddress(), device.getType(),
                device.getDatapointCount());

        if (logger.isTraceEnabled()) {
            logger.trace("{}", device);
            for (HmChannel channel : device.getChannels()) {
                logger.trace("  {}", channel);
                for (HmDatapoint dp : channel.getDatapoints()) {
                    logger.trace("    {}", dp);
                }
            }
        }
    }

    @Override
    public void disableDatapoint(final HmDatapoint dp, double delay) {
        try {
            sendDelayedExecutor.start(new HmDatapointInfo(dp), delay, new DelayedExecuterCallback() {

                @Override
                public void execute() throws IOException {
                    if (MiscUtils.isTrueValue(dp.getValue())) {
                        dp.setValue(Boolean.FALSE);
                        gatewayAdapter.onStateUpdated(dp);
                        handleVirtualDatapointEvent(dp, true);
                    } else if (dp.getType() == HmValueType.ENUM && dp.getValue() != null && !dp.getValue().equals(0)) {
                        dp.setValue(dp.getMinValue());
                        gatewayAdapter.onStateUpdated(dp);
                        handleVirtualDatapointEvent(dp, true);
                    }
                }
            });
        } catch (IOException | HomematicClientException ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteDevice(String address, boolean reset, boolean force, boolean defer) {
        for (RpcClient<?> rpcClient : rpcClients.values()) {
            try {
                rpcClient.deleteDevice(getDevice(address), translateFlags(reset, force, defer));
            } catch (HomematicClientException e) {
                // thrown by getDevice(address) if no device for the given address is paired on the gateway
                logger.info("Device deletion not possible: {}", e.getMessage());
            } catch (IOException e) {
                logger.warn("Device deletion failed: {}", e.getMessage(), e);
            }
        }
    }

    private int translateFlags(boolean reset, boolean force, boolean defer) {
        final int resetFlag = 0b001;
        final int forceFlag = 0b010;
        final int deferFlag = 0b100;
        int resultFlag = 0;

        if (reset) {
            resultFlag += resetFlag;
        }
        if (force) {
            resultFlag += forceFlag;
        }
        if (defer) {
            resultFlag += deferFlag;
        }

        return resultFlag;
    }

    /**
     * Thread which validates the connection to the gateway and restarts the RPC client if necessary.
     */
    private class ConnectionTrackerThread implements Runnable {
        private boolean connectionLost;
        private boolean ping;
        private boolean pong;

        @Override
        public void run() {
            try {
                if (ping && !pong) {
                    handleInvalidConnection();
                }

                pong = false;
                if (config.getGatewayInfo().isCCU1()) {
                    // the CCU1 does not support the ping command, we need a workaround
                    getRpcClient(getDefaultInterface()).listBidcosInterfaces(getDefaultInterface());
                    // if there is no exception, connection is valid
                    pongReceived();
                } else {
                    getRpcClient(getDefaultInterface()).ping(getDefaultInterface(), id);
                }
                ping = true;
            } catch (IOException ex) {
                try {
                    handleInvalidConnection();
                } catch (IOException ex2) {
                    // ignore
                }
            }
        }

        public void pongReceived() {
            pong = true;
            connectionConfirmed();
        }

        private void connectionConfirmed() {
            if (connectionLost) {
                connectionLost = false;
                logger.info("Connection resumed on gateway '{}'", id);
                gatewayAdapter.onConnectionResumed();
            }
        }

        private void handleInvalidConnection() throws IOException {
            ping = false;
            if (!connectionLost) {
                connectionLost = true;
                logger.warn("Connection lost on gateway '{}'", id);
                gatewayAdapter.onConnectionLost();
            }
            stopServers();
            stopClients();
            startClients();
            startServers();
        }

    }
}
