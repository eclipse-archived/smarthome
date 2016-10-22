/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sinope.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.sinope.config.SinopeConfig;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tulip.sinope.core.SinopeApiLoginAnswer;
import ca.tulip.sinope.core.SinopeApiLoginRequest;
import ca.tulip.sinope.core.SinopeDataReadRequest;
import ca.tulip.sinope.core.SinopeDataWriteRequest;
import ca.tulip.sinope.core.appdata.SinopeHeatLevelData;
import ca.tulip.sinope.core.appdata.SinopeOutTempData;
import ca.tulip.sinope.core.appdata.SinopeRoomTempData;
import ca.tulip.sinope.core.appdata.SinopeSetPointModeData;
import ca.tulip.sinope.core.appdata.SinopeSetPointTempData;
import ca.tulip.sinope.core.internal.SinopeAnswer;
import ca.tulip.sinope.core.internal.SinopeDataAnswer;
import ca.tulip.sinope.core.internal.SinopeDataRequest;
import ca.tulip.sinope.core.internal.SinopeRequest;
import ca.tulip.sinope.util.ByteUtil;

/**
 * The {@link SinopeGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeGatewayHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SinopeGatewayHandler.class);
    private ScheduledFuture<?> pollFuture;
    private long refreshInterval;
    private List<SinopeThermostatHandler> thermostatHandlers = new CopyOnWriteArrayList<>();
    private int seq = 1;
    private Socket clientSocket;

    public SinopeGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Sinope Gateway");
        super.initialize();
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        if (config.hostname == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Gateway hostname must be set");
        }
        if (config.port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Gateway port must be set");
        }

        if (config.gatewayId == null || convert(config.gatewayId) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Gateway Id must be set");
        }
        if (config.apiKey == null || convert(config.apiKey) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Api Key must be set");
        }
        refreshInterval = config.refresh;
        schedulePoll();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        stopPoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    void pollNow() {
        if (thingIsInitialized()) {
            schedulePoll();
        }
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 500ms out, then every {} ms", refreshInterval);
        pollFuture = scheduler.scheduleAtFixedRate(pollingRunnable, 500, refreshInterval, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopPoll() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    private synchronized void poll() {

        if (thermostatHandlers.size() > 0) {
            logger.debug("Polling for state");

            try {

                if (connectToBridge()) {
                    logger.debug("Connected to bridge");
                    for (SinopeThermostatHandler sinopeThermostatHandler : thermostatHandlers) {
                        byte[] deviceId = convert(sinopeThermostatHandler.getDeviceId());
                        sinopeThermostatHandler.updateOutsideTemp(readOutsideTemp(clientSocket, deviceId));
                        sinopeThermostatHandler.updateRoomTemp(readRoomTemp(clientSocket, deviceId));
                        sinopeThermostatHandler.updateSetPointTemp(readSetPointTemp(clientSocket, deviceId));
                        sinopeThermostatHandler.updateSetPointMode(readSetPointMode(clientSocket, deviceId));
                        sinopeThermostatHandler.updateHeatLevel(readHeatLevel(clientSocket, deviceId));
                    }
                }

            } catch (IOException e) {
                logger.debug("Could not connect to gateway", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            } catch (Exception e) {
                logger.warn("Unexpected error connecting to gateway", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } finally {
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        logger.warn("Unexpected error when closing connection to gateway", e);
                    }
                }
            }
        } else {
            logger.debug("nothing to poll");
        }
    }

    private boolean connectToBridge() throws UnknownHostException, IOException {
        SinopeConfig config = getConfigAs(SinopeConfig.class);
        this.clientSocket = new Socket(config.hostname, config.port);
        SinopeApiLoginRequest loginRequest = new SinopeApiLoginRequest(convert(config.gatewayId),
                convert(config.apiKey));
        SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), loginRequest);
        return loginAnswer.getStatus() == 0;
    }

    private double readRoomTemp(Socket clientSocket, byte[] deviceId) throws UnknownHostException, IOException {

        logger.debug("Reading room temp for device id : {}", ByteUtil.toString(deviceId));
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeRoomTempData());
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), req);
        double temp = ((SinopeRoomTempData) answ.getAppData()).getRoomTemp() / 100.0;
        logger.debug(String.format("Room temp is : %2.2f C", temp));
        return temp;
    }

    private double readOutsideTemp(Socket clientSocket, byte[] deviceId) throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeOutTempData());
        logger.debug("Reading outside temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), req);
        double temp = ((SinopeOutTempData) answ.getAppData()).getOutTemp() / 100.0;
        logger.debug(String.format("Outside temp is : %2.2f C", temp));
        return temp;

    }

    private double readSetPointTemp(Socket clientSocket, byte[] deviceId) throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeSetPointTempData());
        logger.debug("Reading Set Point temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), req);
        double temp = ((SinopeSetPointTempData) answ.getAppData()).getSetPointTemp() / 100.0;
        logger.debug(String.format("Set Point temp is : %2.2f C", temp));
        return temp;
    }

    private int readSetPointMode(Socket clientSocket, byte[] deviceId) throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeSetPointModeData());
        logger.debug("Reading Set Point mode for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), req);
        byte mode = ((SinopeSetPointModeData) answ.getAppData()).getSetPointMode();
        logger.debug(String.format("Set Point mode is : %d", mode));
        return mode & 0xFF;
    }

    private int readHeatLevel(Socket clientSocket, byte[] deviceId) throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeHeatLevelData());
        logger.debug("Reading Heat Level for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                clientSocket.getInputStream(), req);
        int level = ((SinopeHeatLevelData) answ.getAppData()).getHeatLevel();
        logger.debug(String.format("Heat level is  : %d", level));
        return level;
    }

    private synchronized byte[] newSeq() {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(seq++).array();
    }

    public byte[] convert(String value) {
        if (value == null) {
            return null;
        }
        value = value.replace("-", "");
        value = value.replace("0x", "");
        value = value.replace(" ", "");
        if (value.length() % 2 == 0 && value.length() > 1) {
            byte[] b = new byte[value.length() / 2];

            for (int i = 0; i < value.length(); i = i + 2) {
                b[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
            }
            return b;
        } else {
            return null;
        }
    }

    private synchronized static SinopeAnswer execute(OutputStream outToServer, InputStream inputStream,
            SinopeRequest command) throws UnknownHostException, IOException {

        outToServer.write(command.getPayload());
        outToServer.flush();
        SinopeAnswer answ = command.getReplyAnswer(inputStream);

        return answ;

    }

    private static SinopeAnswer execute(OutputStream outToServer, InputStream inputStream, SinopeDataRequest command)
            throws UnknownHostException, IOException {

        outToServer.write(command.getPayload());

        SinopeDataAnswer answ = command.getReplyAnswer(inputStream);

        while (answ.getMore() == 0x01) {
            answ = command.getReplyAnswer(inputStream);

        }
        return answ;

    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            poll();
        }

    };

    public boolean registerThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        if (thermostatHandler == null) {
            throw new NullPointerException("It's not allowed to pass a null thermostatHandler.");
        }
        boolean result = thermostatHandlers.add(thermostatHandler);
        if (result) {
            schedulePoll();
        }
        return result;
    }

    public boolean unregisterThermostatHandler(SinopeThermostatHandler thermostatHandler) {
        boolean result = thermostatHandlers.remove(thermostatHandler);
        if (result) {
            schedulePoll();
        }
        return result;
    }

    public void setSetPointTemp(SinopeThermostatHandler sinopeThermostatHandler, float temp)
            throws UnknownHostException, IOException {
        int newTemp = (int) (temp * 100.0);

        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        try {
            if (connectToBridge()) {
                logger.debug("Connected to bridge");
                byte[] deviceId = convert(sinopeThermostatHandler.getDeviceId());
                SinopeDataWriteRequest req = new SinopeDataWriteRequest(newSeq(), deviceId,
                        new SinopeSetPointTempData());
                ((SinopeSetPointTempData) req.getAppData()).setSetPointTemp(newTemp);

                SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                        clientSocket.getInputStream(), req);

                if (answ.getStatus() == 0) {
                    logger.debug(String.format("Set Point temp is now : %2.2f C", newTemp));
                } else {
                    logger.debug("Cannot Set Point temp, status: {}", answ.getStatus());
                }
            } else {
                logger.error("Could not connect to bridge to update Set Point Temp");
            }
        } finally {
            clientSocket.close();
            clientSocket = null;
            schedulePoll();
        }
    }

    public void setSetPointMode(SinopeThermostatHandler sinopeThermostatHandler, int mode)
            throws UnknownHostException, IOException {

        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        try {
            if (connectToBridge()) {
                logger.debug("Connected to bridge");
                byte[] deviceId = convert(sinopeThermostatHandler.getDeviceId());
                SinopeDataWriteRequest req = new SinopeDataWriteRequest(newSeq(), deviceId,
                        new SinopeSetPointModeData());
                ((SinopeSetPointModeData) req.getAppData()).setSetPointMode((byte) mode);

                SinopeDataAnswer answ = (SinopeDataAnswer) execute(clientSocket.getOutputStream(),
                        clientSocket.getInputStream(), req);

                if (answ.getStatus() == 0) {
                    logger.debug(String.format("Set Point mode is now : %d", mode));
                } else {
                    logger.debug("Cannot Set Point mode, status: {}", answ.getStatus());
                }
            } else {
                logger.error("Could not connect to bridge to update Set Point Mode");
            }
        } finally {
            clientSocket.close();
            clientSocket = null;
            schedulePoll();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }
}
