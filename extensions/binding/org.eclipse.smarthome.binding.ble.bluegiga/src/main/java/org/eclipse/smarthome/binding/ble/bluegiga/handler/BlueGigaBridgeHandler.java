/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.ble.bluegiga.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.ble.BleBindingConstants;
import org.eclipse.smarthome.binding.ble.BleBridgeApi;
import org.eclipse.smarthome.binding.ble.BleDevice;
import org.eclipse.smarthome.binding.ble.BleDeviceListener;
import org.eclipse.smarthome.binding.ble.BluetoothAddress;
import org.eclipse.smarthome.binding.ble.bluegiga.BlueGigaBindingConstants;
import org.eclipse.smarthome.binding.ble.discovery.BleDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zsmartsystems.bluetooth.bluegiga.BlueGigaCommand;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaEventListener;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaResponse;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaSerialHandler;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeWriteCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeWriteResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByGroupTypeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByGroupTypeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByHandleCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByHandleResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectDirectCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectDirectResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaDiscoverCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaEndProcedureCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaEndProcedureResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaScanResponseEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetModeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetModeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetScanParametersCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetConnectionsCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetConnectionsResponse;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BgApiResponse;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BluetoothAddressType;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapConnectableMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverableMode;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link BlueGigaBridgeHandler} is responsible for interfacing to the BlueGiga Bluetooth adapter.
 * It provides a private interface for {@link BlueGigaBleDevice}s to access the dongle and provides top
 * level adaptor functionality for scanning and arbitration.
 * <p>
 * The handler provides the serial interface to the dongle via the BlueGiga BG-API library.
 * <p>
 * In the BlueGiga dongle, we leave scanning enabled most of the time. Normally, it's just passive scanning, and active
 * scanning is enabled when we want to include new devices. Passive scanning is enough for us to receive beacons etc
 * that are transmitted periodically, and active scanning will get more information which may be useful when we are
 * including new devices.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BlueGigaBridgeHandler extends BaseBridgeHandler implements BleBridgeApi, BlueGigaEventListener {

    private final Logger logger = LoggerFactory.getLogger(BlueGigaBridgeHandler.class);

    // The Serial port name
    private String portId;

    // The serial port.
    private SerialPort serialPort;

    // The serial port input stream.
    private InputStream inputStream;

    // The serial port output stream.
    private OutputStream outputStream;

    // The BlueGiga API handler
    private BlueGigaSerialHandler bgHandler;

    // The maximum number of connections this interface supports
    private int maxConnections = 0;

    private int passiveScanInterval = 0x40;
    private int passiveScanWindow = 0x08;

    private int activeScanInterval = 0x40;
    private int activeScanWindow = 0x20;

    // Our BT address
    private BluetoothAddress address;

    // Map of Bluetooth devices known to this bridge.
    // This is all devices we have heard on the network - not just things bound to the bridge
    private final Map<BluetoothAddress, BleDevice> devices = new HashMap<BluetoothAddress, BleDevice>();

    // Map of open connections
    private final Map<Integer, BlueGigaBleDevice> connections = new HashMap<Integer, BlueGigaBleDevice>();

    // List of device listeners
    protected final ConcurrentHashMap<BluetoothAddress, BleDeviceListener> deviceListeners = new ConcurrentHashMap<BluetoothAddress, BleDeviceListener>();

    private BleDiscoveryService discoveryService;
    private ServiceRegistration discoveryRegistration;

    public BlueGigaBridgeHandler(Bridge bridge) {
        super(bridge);

        // Read the configuration
        portId = (String) getConfig().get(BlueGigaBindingConstants.CONFIGURATION_PORT);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported for the bridge
    }

    @Override
    public void initialize() {
        final BlueGigaBridgeHandler me = this;

        // Initialisation takes some time, so do it in another thread.
        Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                openSerialPort(portId, 115200);
                if (serialPort == null) {
                    // Create the handler
                    bgHandler = new BlueGigaSerialHandler(inputStream, outputStream);
                }

                // BlueGigaCommand command1 = new BlueGigaAddressGetCommand();
                // BlueGigaAddressGetResponse addressResponse1 = (BlueGigaAddressGetResponse) bgHandler
                // .sendTransaction(command1);
                // if (addressResponse1 != null) {
                // btAddress = addressResponse1.getAddress();
                // }

                // Create and send the reset command to the dongle
                BlueGigaCommand command; // = new BlueGigaResetCommand();
                // bgHandler.queueFrame(command);

                // The reset command has no response. It will however reset the serial interface!
                // We wait a short while, then close and re-open the serial port.
                // try {
                // Thread.sleep(250);
                // closeSerialPort();
                // Thread.sleep(250);
                // } catch (InterruptedException e) {
                // If we're interrupted, then close.
                // Should only happen if the binding gets closed
                // return;
                // }

                // Open the port for the final time
                // openSerialPort(portId, 115200);

                // Create the handler
                bgHandler = new BlueGigaSerialHandler(inputStream, outputStream);
                bgHandler.addEventListener(me);

                // Stop any procedures that are running
                bgStopProcedure();

                // Close all transactions
                command = new BlueGigaGetConnectionsCommand();
                BlueGigaGetConnectionsResponse connectionsResponse = (BlueGigaGetConnectionsResponse) bgHandler
                        .sendTransaction(command);
                if (connectionsResponse != null) {
                    maxConnections = connectionsResponse.getMaxconn();
                }

                Map<String, String> properties = editProperties();
                properties.put(BleBindingConstants.PROPERTY_MAXCONNECTIONS, Integer.toString(maxConnections));
                updateProperties(properties);

                // Close all connections so we start from a known position
                for (int connection = 0; connection < maxConnections; connection++) {
                    bgCloseConnection(connection);
                }

                // Get our Bluetooth address
                command = new BlueGigaAddressGetCommand();
                BlueGigaAddressGetResponse addressResponse = (BlueGigaAddressGetResponse) bgHandler
                        .sendTransaction(command);
                if (addressResponse != null) {
                    address = new BluetoothAddress(addressResponse.getAddress());
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

                // Set mode to non-discoverable etc.
                // Not doing this will cause connection failures later
                bgSetMode();

                // Start the discovery service
                discoveryService = new BleDiscoveryService(me.getThing().getUID(), me);
                discoveryService.activate();

                // And register it as an OSGi service
                discoveryRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                        discoveryService, new Hashtable<String, Object>());

                // Start passive scan
                bgStartScanning(false, passiveScanInterval, passiveScanWindow);
            }
        };

        // Schedule the initialisation task
        scheduler.schedule(pollingRunnable, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        // Remove the discovery service
        discoveryService.deactivate();
        discoveryRegistration.unregister();

        closeSerialPort();
    }

    private void openSerialPort(final String serialPortName, int baudRate) {
        logger.info("Connecting to serial port [{}]", serialPortName);
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            CommPort commPort = portIdentifier.open("org.openhab.binding.zigbee", 2000);
            serialPort = (gnu.io.SerialPort) commPort;
            serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    gnu.io.SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(gnu.io.SerialPort.FLOWCONTROL_RTSCTS_OUT);

            ((CommPort) serialPort).enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(2000);

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.notifyOnDataAvailable(true);

            logger.info("Serial port [{}] is initialized.", portId);
        } catch (NoSuchPortException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port does not exist");
            return;
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Port in use");
            return;
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Unsupported operation");
            return;
        }

        try {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
            logger.error("Error getting serial streams", e);
        }

        return;
    }

    private void closeSerialPort() {
        try {
            if (serialPort != null) {
                serialPort.enableReceiveTimeout(1);

                inputStream.close();
                outputStream.flush();
                outputStream.close();

                serialPort.close();

                serialPort = null;
                inputStream = null;
                outputStream = null;

                logger.info("Serial port [{}] is closed.", portId);
            }
        } catch (Exception e) {
            logger.error("Error closing serial port", e);
        }
    }

    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent) {
            BlueGigaScanResponseEvent scanEvent = (BlueGigaScanResponseEvent) event;

            // We use the scan event to add any devices we hear to the devices list
            // The device gets created, and then manages itself for discovery etc.
            BluetoothAddress sender = new BluetoothAddress(scanEvent.getSender());
            BlueGigaBleDevice device;
            if (devices.get(sender) == null) {
                logger.debug("BlueGiga adding new device to adaptor {}: {}", address, sender);
                device = new BlueGigaBleDevice(this, new BluetoothAddress(scanEvent.getSender()),
                        scanEvent.getAddressType());
                devices.put(sender, device);
            }

            return;
        }
    }

    @Override
    public boolean scanStart() {
        // Stop the passive scan
        bgStopProcedure();

        // Start a active scan
        bgStartScanning(true, activeScanInterval, activeScanWindow);
        return false;
    }

    @Override
    public void scanStop() {
        // Stop the active scan
        bgStopProcedure();

        // Start a passive scan
        bgStartScanning(false, passiveScanInterval, passiveScanWindow);
    }

    @Override
    public BluetoothAddress getAddress() {
        return address;
    }

    @Override
    public BleDevice getDevice(BluetoothAddress address) {
        BleDevice device = devices.get(address);
        if (device == null) {
            // This method always needs to return a device, even if we don't currently know about it.
            device = new BlueGigaBleDevice(this, address, BluetoothAddressType.UNKNOWN);
            devices.put(address, device);
        }
        return device;
    }

    /*
     * The following methods provide adaptor level functions for the BlueGiga interface. Typically these methods
     * are used by the device but are provided in the adapter to allow common knowledge and to support conflict
     * resolution.
     */

    /**
     * Connects to a device
     *
     * @return true if the connection was started
     */
    public boolean bgConnect(String address, BluetoothAddressType addressType) {
        // Check the connection to make sure we're not already connected

        bgSetMode();

        // Connect...
        int connIntervalMin = 60;
        int connIntervalMax = 100;
        int latency = 0;
        int timeout = 100;

        BlueGigaConnectDirectCommand connect = new BlueGigaConnectDirectCommand();
        connect.setAddress(address);
        connect.setAddrType(addressType);
        connect.setConnIntervalMin(connIntervalMin);
        connect.setConnIntervalMax(connIntervalMax);
        connect.setLatency(latency);
        connect.setTimeout(timeout);
        BlueGigaConnectDirectResponse connectResponse = (BlueGigaConnectDirectResponse) bgHandler
                .sendTransaction(connect);
        if (connectResponse.getResult() != BgApiResponse.SUCCESS) {
            return false;
        }

        return true;
    }

    /**
     * Device discovered. This simply passes the discover information to the discovery service for processing.
     */
    public void deviceDiscovered(BlueGigaBleDevice device) {
        if (discoveryService == null) {
            return;
        }
        discoveryService.deviceDiscovered(device);
    }

    /**
     * Close a connection using {@link BlueGigaDisconnectCommand}
     *
     * @param connectionHandle
     * @return
     */
    public boolean bgCloseConnection(int connectionHandle) {
        BlueGigaDisconnectCommand command = new BlueGigaDisconnectCommand();
        command.setConnection(connectionHandle);
        BlueGigaDisconnectResponse response = (BlueGigaDisconnectResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Start a read of all primary services using {@link BlueGigaReadByGroupTypeCommand}
     *
     * @param connectionHandle
     * @return true if successful
     */
    public boolean bgFindPrimaryServices(int connectionHandle) {
        logger.debug("BlueGiga FindPrimary: connection {}", connectionHandle);
        BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand();
        command.setConnection(connectionHandle);
        command.setStart(1);
        command.setEnd(65535);
        command.setUuid(UUID.fromString("00002800-0000-0000-0000-000000000000"));
        BlueGigaReadByGroupTypeResponse response = (BlueGigaReadByGroupTypeResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Start a read of all characteristics using {@link BlueGigaFindInformationCommand}
     *
     * @param connectionHandle
     * @return true if successful
     */
    public boolean bgFindCharacteristics(int connectionHandle) {
        logger.debug("BlueGiga Find: connection {}", connectionHandle);
        BlueGigaFindInformationCommand command = new BlueGigaFindInformationCommand();
        command.setConnection(connectionHandle);
        command.setStart(1);
        command.setEnd(65535);
        BlueGigaFindInformationResponse response = (BlueGigaFindInformationResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Read a characteristic using {@link BlueGigaReadByHandleCommand}
     *
     * @param connectionHandle
     * @param handle
     * @return true if successful
     */
    public boolean bgReadCharacteristic(int connectionHandle, int handle) {
        logger.debug("BlueGiga Read: connection {}, handle {}", connectionHandle, handle);
        BlueGigaReadByHandleCommand command = new BlueGigaReadByHandleCommand();
        command.setConnection(connectionHandle);
        command.setChrHandle(handle);
        BlueGigaReadByHandleResponse response = (BlueGigaReadByHandleResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Write a characteristic using {@link BlueGigaAttributeWriteCommand}
     *
     * @param connectionHandle
     * @param handle
     * @param value
     * @return true if successful
     */
    public boolean bgWriteCharacteristic(int connectionHandle, int handle, int[] value) {
        logger.debug("BlueGiga Write: connection {}, handle {}", connectionHandle, handle);
        BlueGigaAttributeWriteCommand command = new BlueGigaAttributeWriteCommand();
        command.setConnection(connectionHandle);
        command.setAttHandle(handle);
        command.setData(value);
        BlueGigaAttributeWriteResponse response = (BlueGigaAttributeWriteResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /*
     * The following methods are private methods for handling the BlueGiga protocol
     */
    private boolean bgStopProcedure() {
        BlueGigaCommand command = new BlueGigaEndProcedureCommand();
        BlueGigaEndProcedureResponse response = (BlueGigaEndProcedureResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    private boolean bgSetMode() {
        BlueGigaSetModeCommand command = new BlueGigaSetModeCommand();
        command.setConnect(GapConnectableMode.GAP_NON_CONNECTABLE);
        command.setDiscover(GapDiscoverableMode.GAP_NON_DISCOVERABLE);
        BlueGigaSetModeResponse response = (BlueGigaSetModeResponse) bgHandler.sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Starts scanning on the dongle
     *
     * @param active true for active scanning
     */
    private void bgStartScanning(boolean active, int interval, int window) {
        BlueGigaSetScanParametersCommand scanCommand = new BlueGigaSetScanParametersCommand();
        scanCommand.setActiveScanning(active);
        scanCommand.setScanInterval(interval);
        scanCommand.setScanWindow(window);
        bgHandler.sendTransaction(scanCommand);

        BlueGigaDiscoverCommand discoverCommand = new BlueGigaDiscoverCommand();
        discoverCommand.setMode(GapDiscoverMode.GAP_DISCOVER_OBSERVATION);
        bgHandler.sendTransaction(discoverCommand);
    }

    public void addEventListener(BlueGigaEventListener listener) {
        bgHandler.addEventListener(listener);
    }

    public void removeEventListener(BlueGigaEventListener listener) {
        bgHandler.removeEventListener(listener);
    }

}
