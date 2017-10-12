/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.internal;

import java.net.URI;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TradfriCoapClient} provides some convenience features over the
 * plain {@link CoapClient} from californium.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriCoapClient extends CoapClient {

    private static final int TIMEOUT = 2000;
    private static final int DEFAULT_DELAY_MILLIS = 600;
    private final Logger logger;
    private final LinkedList<PayloadCallbackPair> commandsQueue;
    private final Runnable commandExecutor;
    private Future<?> job;

    public TradfriCoapClient(URI uri) {
        super(uri);
        setTimeout(TIMEOUT);
        logger = LoggerFactory.getLogger(getClass());

        commandsQueue = new LinkedList<>();
        
        commandExecutor = () -> {
            while (true) {
                try {
                    int delayTime = 0;
                    synchronized (this.commandsQueue) {
                        PayloadCallbackPair payloadCallbackPair = TradfriCoapClient.this.commandsQueue.poll();
                        if (payloadCallbackPair != null) {
                            logger.debug("Proccessing payload: {}", payloadCallbackPair.payload);
                            TradfriCoapClient.this.put(new TradfriCoapHandler(payloadCallbackPair.callback), payloadCallbackPair.payload, MediaTypeRegistry.TEXT_PLAIN);
                            delayTime = Optional.ofNullable(payloadCallbackPair.delay).orElse(DEFAULT_DELAY_MILLIS);
                        } else {
                            return;
                        }
                    }
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                    logger.debug("commandExecutorThread was interrupted", e);
                }
            }
        };
        
        job = null;
    }

    /**
     * Starts observation of the resource and uses the given callback to provide updates.
     *
     * @param callback the callback to use for updates
     */
    public void startObserve(CoapCallback callback) {
        observe(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result through a {@link CompletableFuture}.
     *
     * @return the future that will hold the result
     */
    public CompletableFuture<String> asyncGet() {
        CompletableFuture<String> future = new CompletableFuture<>();
        get(new TradfriCoapHandler(future));
        return future;
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result to a given callback.
     *
     * @param callback the callback to use for the response
     */
    public void asyncGet(CoapCallback callback) {
        get(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a PUT on the resource with a payload, provides the result to a given callback
     * and blocks sending new requests to the resource for specified amount of milliseconds.
     *
     * @param payload the payload to send with the PUT request
     * @param callback the callback to use for the response
     * @param delay the amount of time (in milliseconds) after which processing of the command by the bridge should be
     *            finished.
     *            (if not specified a default value will be used)
     * @param scheduler scheduler to be used for sending commands
     */
    public void asyncPut(String payload, CoapCallback callback, Integer delay, ScheduledExecutorService scheduler) {
        this.asyncPut(new PayloadCallbackPair(payload, callback, delay), scheduler);
    }

    /**
     * Asynchronously executes a PUT on the resource with a payload and provides the result to a given callback.
     *
     * @param payloadCallbackPair object which holds the payload and callback process the PUT request
     * @param scheduler scheduler to be used for sending commands
     */
    public void asyncPut(PayloadCallbackPair payloadCallbackPair, ScheduledExecutorService scheduler) {
        
        synchronized (this.commandsQueue) {
            if (this.commandsQueue.isEmpty()) {
                this.commandsQueue.offer(payloadCallbackPair);
                if (job == null || job.isDone()) {
                    job = scheduler.submit(commandExecutor);
                }
            } else {
                this.commandsQueue.offer(payloadCallbackPair);
            }
        }
    }

    @Override
    public void shutdown() {
        if (job != null) {
            job.cancel(true);
        }

        super.shutdown();
    }

    public final class PayloadCallbackPair {
        public final String payload;
        public final CoapCallback callback;
        public final Integer delay;

        public PayloadCallbackPair(String payload, CoapCallback callback, Integer delay) {
            this.payload = payload;
            this.callback = callback;
            this.delay = delay;
        }
    }
}
