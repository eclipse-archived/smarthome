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
package org.eclipse.smarthome.binding.hue.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
@NonNullByDefault
public class HttpClient {
    private int timeout = 1000;
    private final Logger logger;
    private final LinkedList<AsyncPutParameters> commandsQueue;
    private final Runnable commandExecutor;
    @Nullable
    private Future<?> job;

    public HttpClient() {
        logger = LoggerFactory.getLogger(getClass());
        commandsQueue = new LinkedList<>();

        commandExecutor = () -> {
            while (true) {
                try {
                    int delayTime = 0;
                    synchronized (this.commandsQueue) {
                        AsyncPutParameters payloadCallbackPair = HttpClient.this.commandsQueue.poll();
                        if (payloadCallbackPair != null) {
                            logger.debug("Async sending put to address: {} delay: {} body: {}",
                                    payloadCallbackPair.address, payloadCallbackPair.delay, payloadCallbackPair.body);
                            try {
                                Result result = HttpClient.this.put(payloadCallbackPair.address,
                                        payloadCallbackPair.body);
                                payloadCallbackPair.future.complete(result);
                            } catch (IOException e) {
                                payloadCallbackPair.future.completeExceptionally(e);
                            }
                            delayTime = payloadCallbackPair.delay;
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
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Result get(String address) throws IOException {
        return doNetwork(address, "GET", "");
    }

    public Result post(String address, String body) throws IOException {
        return doNetwork(address, "POST", body);
    }

    public Result put(String address, String body) throws IOException {
        return doNetwork(address, "PUT", body);
    }

    public CompletableFuture<Result> putAsync(String address, String body, Integer delay,
            ScheduledExecutorService scheduler) {
        AsyncPutParameters asyncPutParameters = new AsyncPutParameters(address, body, delay);

        synchronized (this.commandsQueue) {
            if (this.commandsQueue.isEmpty()) {
                this.commandsQueue.offer(asyncPutParameters);
                if (job == null || job.isDone()) {
                    job = scheduler.submit(commandExecutor);
                }
            } else {
                this.commandsQueue.offer(asyncPutParameters);
            }
        }

        return asyncPutParameters.future;
    }

    public Result delete(String address) throws IOException {
        return doNetwork(address, "DELETE", "");
    }

    protected Result doNetwork(String address, String requestMethod, @Nullable String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(address).openConnection();
        try {
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            if (body != null && !body.equals("")) {
                conn.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(body);
                out.close();
            }

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String output = IOUtils.toString(in, StandardCharsets.UTF_8.name());
            return new Result(output, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    public final class AsyncPutParameters {
        public final String address;
        public final String body;
        public final CompletableFuture<Result> future;
        public final Integer delay;

        public AsyncPutParameters(String address, String body, Integer delay) {
            this.address = address;
            this.body = body;
            this.future = new CompletableFuture<Result>();
            this.delay = delay;
        }
    }
}
