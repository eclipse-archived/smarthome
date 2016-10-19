/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.sinope.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import ca.tulip.sinope.core.appdata.SinopeOutTempData;
import ca.tulip.sinope.core.appdata.SinopeRoomTempData;
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

    public SinopeGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Channel channel = getThing().getChannel(channelUID.getId());
        // if (channel != null && sceneChannelTypeUID.equals(channel.getChannelTypeUID())) {
        // if (command.equals(OnOffType.ON)) {
        // webTargets.activateScene(Integer.parseInt(channelUID.getId()));
        // }
        // }
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
        SinopeConfig config = getConfigAs(SinopeConfig.class);

        if (thermostatHandlers.size() > 0) {
            logger.debug("Polling for state");
            Socket clientSocket = null;
            try {

                clientSocket = new Socket(config.hostname, config.port);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                SinopeApiLoginRequest loginRequest = new SinopeApiLoginRequest(convert(config.gatewayId),
                        convert(config.apiKey));
                SinopeApiLoginAnswer loginAnswer = (SinopeApiLoginAnswer) execute(outToServer,
                        clientSocket.getInputStream(), loginRequest);

                if (loginAnswer.getStatus() == 0) {
                    logger.debug("Connected to bridge");
                    for (SinopeThermostatHandler sinopeThermostatHandler : thermostatHandlers) {
                        byte[] deviceId = convert(sinopeThermostatHandler.getDeviceId());
                        sinopeThermostatHandler.updateOutsideTemp(readOutsideTemp(clientSocket, outToServer, deviceId));
                        sinopeThermostatHandler.updateRoomTemp(readRoomTemp(clientSocket, outToServer, deviceId));
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

    private double readRoomTemp(Socket clientSocket, DataOutputStream outToServer, byte[] deviceId)
            throws UnknownHostException, IOException {
        SinopeDataReadRequest req;
        SinopeDataAnswer answ;
        logger.debug("Reading room temp for device id : {}", ByteUtil.toString(deviceId));
        req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeRoomTempData());
        answ = (SinopeDataAnswer) execute(outToServer, clientSocket.getInputStream(), req);
        double temp = ((SinopeRoomTempData) answ.getAppData()).getRoomTemp() / 100.0;
        logger.debug(String.format("Room temp is : %2.2f C", temp));
        return temp;
    }

    private double readOutsideTemp(Socket clientSocket, DataOutputStream outToServer, byte[] deviceId)
            throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(newSeq(), deviceId, new SinopeOutTempData());
        logger.debug("Reading outside temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) execute(outToServer, clientSocket.getInputStream(), req);
        double temp = ((SinopeOutTempData) answ.getAppData()).getOutTemp() / 100.0;
        logger.debug(String.format("Outside temp is : %2.2f C", temp));
        return temp;

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

    private synchronized static SinopeAnswer execute(DataOutputStream outToServer, InputStream inputStream,
            SinopeRequest command) throws UnknownHostException, IOException {

        outToServer.write(command.getPayload());
        outToServer.flush();
        SinopeAnswer answ = command.getReplyAnswer(inputStream);

        return answ;

    }

    private static SinopeAnswer execute(DataOutputStream outToServer, InputStream inputStream,
            SinopeDataRequest command) throws UnknownHostException, IOException {

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

    public boolean registerThermostatHandler(SinopeThermostatHandler lightStatusListener) {
        if (lightStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null LightStatusListener.");
        }
        boolean result = thermostatHandlers.add(lightStatusListener);
        if (result) {
            schedulePoll();
            // inform the listener initially about all lights and their states
            /*
             * for (FullLight light : lastLightStates.values()) {
             * lightStatusListener.onLightAdded(bridge, light);
             * }
             */
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
}
