/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.sonyaudio.internal.protocol;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link SonyAudioConnection} is responsible for communicating with SONY audio products
 * handlers.
 *
 * @author David Åberg - Initial contribution
 */
public class SonyAudioClientSocket {
    private final Logger logger = LoggerFactory.getLogger(SonyAudioClientSocket.class);

    private final ScheduledExecutorService scheduler;
    private static final int REQUEST_TIMEOUT_MS = 60000;

    private CountDownLatch commandLatch = null;
    private JsonObject commandResponse = null;
    private int nextMessageId = 1;

    private boolean connected = false;

    private URI uri;
    private Session session;
    private WebSocketClient client;

    private final JsonParser parser = new JsonParser();
    private final Gson mapper;

    private final SonyAudioClientSocketEventListener eventHandler;

    public SonyAudioClientSocket(SonyAudioClientSocketEventListener eventHandler, URI uri,
            ScheduledExecutorService scheduler) {
        mapper = new GsonBuilder().disableHtmlEscaping().create();
        this.eventHandler = eventHandler;
        this.uri = uri;
        client = new WebSocketClient();
        this.scheduler = scheduler;
    }

    public synchronized void open() throws Exception {
        if (isConnected()) {
            logger.warn("connect: connection is already open");
        }
        logger.debug("connect: connection to {}", uri.toString());
        if (!client.isStarted()) {
            client.start();
        }
        SonyAudioWebSocketListener socket = new SonyAudioWebSocketListener();
        ClientUpgradeRequest request = new ClientUpgradeRequest();

        client.connect(socket, uri, request);
    }

    public void close() {
        // if there is an old web socket then clean up and destroy
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("Exception during closing the websocket {}", e.getMessage(), e);
            }
            session = null;
        }
        try {
            client.stop();
        } catch (Exception e) {
            logger.error("Exception during closing the websocket {}", e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        if (session == null || !session.isOpen()) {
            return false;
        }

        return connected;
    }

    @WebSocket
    public class SonyAudioWebSocketListener {

        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            logger.debug("Connected to server");
            session = wssession;
            connected = true;
            if (eventHandler != null) {
                scheduler.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            eventHandler.onConnectionOpened(uri);
                        } catch (Exception e) {
                            logger.error("Error handling onConnectionOpened() {}", e.getMessage(), e);
                        }

                    }
                });

            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.debug("Message received from server: {}", message);
            final JsonObject json = parser.parse(message).getAsJsonObject();
            if (json.has("id")) {
                logger.debug("Response received from server: {}", json);
                int messageId = json.get("id").getAsInt();
                if (messageId == nextMessageId - 1) {
                    commandResponse = json;
                    commandLatch.countDown();
                }
            } else {
                logger.debug("Event received from server: {}", json);
                try {
                    if (eventHandler != null) {
                        scheduler.submit(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    eventHandler.handleEvent(json);
                                } catch (Exception e) {
                                    logger.error("Error handling event {} player state change message: {}", json,
                                            e.getMessage(), e);
                                }

                            }
                        });

                    }
                } catch (Exception e) {
                    logger.error("Error handling player state change message", e);
                }
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            session = null;
            connected = false;
            logger.debug("Closing a WebSocket due to {}", reason);
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        eventHandler.onConnectionClosed();
                    } catch (Exception e) {
                        logger.error("Error handling onConnectionClosed()", e);
                    }
                }
            });
        }
    }

    private void sendMessage(String str) throws Exception {
        if (isConnected()) {
            logger.debug("send message: {}", str);
            session.getRemote().sendString(str);
        } else {
            throw new Exception("socket not initialized");
        }
    }

    public synchronized JsonElement callMethod(SonyAudioMethod method) throws IOException {
        try {
            method.id = nextMessageId;
            String message = mapper.toJson(method);

            commandLatch = new CountDownLatch(1);
            commandResponse = null;
            nextMessageId++;

            sendMessage(message);
            if (commandLatch.await(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.debug("callMethod returns {}", commandResponse.toString());
                return commandResponse.get("result");
            } else {
                logger.error("Timeout during callMethod({}, {})", method.method);
                throw new IOException("Timeout during callMethod");
            }
        } catch (Exception e) {
            throw new IOException("Error during callMethod", e);
        }
    }

    public URI getURI() {
        return uri;
    }
}
