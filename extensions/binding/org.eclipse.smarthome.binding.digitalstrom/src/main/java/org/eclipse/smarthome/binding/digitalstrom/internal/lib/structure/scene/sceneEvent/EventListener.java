/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.sceneEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONRequestConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl.JSONResponseHandler;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * If someone call or undo a scene, the {@link SceneManager} will get a notification
 * to update the state of the internal saved {@link InternalScene} or directly the {@link Device}, if it was a
 * device scene.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class EventListener {

    private Logger logger = LoggerFactory.getLogger(EventListener.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(Config.THREADPOOL_NAME);
    private ScheduledFuture<?> pollingScheduler = null;

    private final String EVENT_NAME_CALL = "callScene";
    private final String EVENT_NAME_UNDO = "undoScene";
    private final int ID = 11;

    private final String INVALID_SESSION = "Invalid session!";
    private final String UNKNOWN_TOKEN = "Token " + ID + " not found!";

    private final ConnectionManager connManager;
    private SceneManager sceneManager;
    private Config config;

    /**
     * Creates a new {@link EventListener}. To get notified by call and undo scene events you have to call
     * {@link #start()}.
     *
     * @param connectionManager must not be null
     * @param sceneManager must not be null
     */
    public EventListener(ConnectionManager connectionManager, SceneManager sceneManager) {
        this.connManager = connectionManager;
        this.config = connectionManager.getConfig();
        this.sceneManager = sceneManager;
    }

    /**
     * Stops this {@link EventListener}.
     */
    public synchronized void stop() {
        if (pollingScheduler != null || !pollingScheduler.isCancelled()) {
            pollingScheduler.cancel(true);
            pollingScheduler = null;
            unsubscribe();
            logger.debug("Stop EventListener");
        }
    }

    /**
     * Starts this {@link EventListener}.
     */
    public synchronized void start() {
        if (subscribe() && (pollingScheduler == null || pollingScheduler.isCancelled())) {
            pollingScheduler = scheduler.scheduleAtFixedRate(runableListener, 0,
                    config.getEventListenerRefreshinterval(), TimeUnit.MICROSECONDS);
            logger.debug("Start EventListener");
        }
    }

    private boolean subscribe() {
        if (connManager.checkConnection()) {
            boolean transmitted = connManager.getDigitalSTROMAPI().subscribeEvent(this.connManager.getSessionToken(),
                    EVENT_NAME_CALL, this.ID, config.getConnectionTimeout(), config.getReadTimeout());
            transmitted = connManager.getDigitalSTROMAPI().subscribeEvent(this.connManager.getSessionToken(),
                    EVENT_NAME_UNDO, this.ID, config.getConnectionTimeout(), config.getReadTimeout());

            if (!transmitted) {
                logger.error("Couldn't subscribe EventListener ... maybe timeout because system is to busy ...");
            } else {
                logger.debug("subscribe successfull");
                return true;
            }
        } else {
            logger.error("Couldn't subscribe eventListener, because there is no token (no connection)");
        }
        return false;
    }

    private Runnable runableListener = new Runnable() {

        @Override
        public void run() {
            String request = getEventAsRequest(ID, 500);
            if (request != null) {
                String response = connManager.getHttpTransport().execute(request);
                JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

                if (JSONResponseHandler.checkResponse(responseObj)) {
                    JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                    if (obj != null && obj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT.getKey()) instanceof JsonArray) {
                        JsonArray array = (JsonArray) obj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT.getKey());
                        try {
                            handleEvent(array);
                        } catch (Exception e) {
                            logger.error("An Exception occurred", e);
                        }
                    }
                } else {
                    String errorStr = null;
                    if (responseObj != null
                            && responseObj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT_ERROR.getKey()) != null) {
                        errorStr = responseObj.get(JSONApiResponseKeysEnum.EVENT_GET_EVENT_ERROR.getKey())
                                .getAsString();
                    }
                    if (errorStr != null && (errorStr.equals(INVALID_SESSION) || errorStr.contains(UNKNOWN_TOKEN))) {
                        unsubscribe();
                        subscribe();
                    } else if (errorStr != null) {
                        pollingScheduler.cancel(true);
                        logger.error("Unknown error message at event response: " + errorStr);
                    }
                }
            }
        }
    };

    private String getEventAsRequest(int subscriptionID, int timeout) {
        if (connManager.checkConnection()) {
            return JSONRequestConstants.JSON_EVENT_GET + JSONRequestConstants.PARAMETER_TOKEN
                    + connManager.getSessionToken() + JSONRequestConstants.INFIX_PARAMETER_SUBSCRIPTION_ID
                    + subscriptionID + JSONRequestConstants.INFIX_PARAMETER_TIMEOUT + timeout;
        }
        return null;
    }

    private boolean unsubscribeEvent(String name, int subscriptionID) {
        if (connManager.checkConnection()) {
            return connManager.getDigitalSTROMAPI().unsubscribeEvent(connManager.getSessionToken(), EVENT_NAME_CALL,
                    this.ID, Config.DEFAULT_CONNECTION_TIMEOUT, Config.DEFAULT_READ_TIMEOUT);
        }
        return false;
    }

    private boolean unsubscribe() {
        return this.unsubscribeEvent(this.EVENT_NAME_CALL, this.ID);
    }

    private void handleEvent(JsonArray array) {
        if (array.size() > 0) {
            Event event = new JSONEventImpl(array);
            for (EventItem item : event.getEventItems()) {
                logger.info(item.getProperties().toString());
                this.sceneManager.handleEvent(item);
            }
        }
    }
}