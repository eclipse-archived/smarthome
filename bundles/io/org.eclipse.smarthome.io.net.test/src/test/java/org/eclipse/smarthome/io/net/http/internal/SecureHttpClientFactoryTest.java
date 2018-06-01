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
package org.eclipse.smarthome.io.net.http.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

public class SecureHttpClientFactoryTest {

    private SecureHttpClientFactory secureHttpClientFactory;

    private static final String TEST_URL = "https://www.eclipse.org/";

    @Mock
    private TrustManagerProvider trustmanagerProvider;

    @Before
    public void setup() {
        initMocks(this);
        secureHttpClientFactory = new SecureHttpClientFactory();
        secureHttpClientFactory.setTrustmanagerProvider(trustmanagerProvider);
    }

    @Test
    public void testGetClient() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 200, 60, 5, 10, 60));

        HttpClient client = secureHttpClientFactory.getCommonHttpClient();

        assertThat(client, is(notNullValue()));

        secureHttpClientFactory.deactivate();
    }

    @Test
    public void testGetClientWithEndpoint() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 200, 60, 5, 10, 60));

        when(trustmanagerProvider.getTrustManagers("https://www.heise.de")).thenReturn(Stream.empty());

        HttpClient client = secureHttpClientFactory.createHttpClient("consumer", TEST_URL);

        assertThat(client, is(notNullValue()));

        verify(trustmanagerProvider).getTrustManagers(TEST_URL);

        client.stop();

        secureHttpClientFactory.deactivate();
    }

    @Ignore("only for manual test")
    @Test
    public void testMultiThreadedShared() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 200, 60, 5, 10, 60));

        ThreadPoolExecutor workers = new ThreadPoolExecutor(20, 80, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(50 * 50));

        List<HttpClient> clients = new ArrayList<>();

        final int CLIENTS = 2;
        final int REQUESTS = 2;

        for (int i = 0; i < CLIENTS; i++) {
            HttpClient httpClient = secureHttpClientFactory.getCommonHttpClient();
            clients.add(httpClient);
        }

        final List<String> failures = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            for (final HttpClient client : clients) {
                workers.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ContentResponse response = client.GET(TEST_URL);
                            if (response.getStatus() != 200) {
                                failures.add("Statuscode != 200");
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            failures.add("Unexpected exception:" + e.getMessage());
                        }
                    }
                });
            }
        }

        workers.shutdown();
        workers.awaitTermination(120, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.toString());
        }

        secureHttpClientFactory.deactivate();
    }

    @Ignore("only for manual test")
    @Test
    public void testMultiThreadedCustom() throws Exception {
        secureHttpClientFactory.activate(createConfigMap(10, 200, 60, 5, 10, 60));

        ThreadPoolExecutor workers = new ThreadPoolExecutor(20, 80, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(50 * 50));

        List<HttpClient> clients = new ArrayList<>();

        final int CLIENTS = 2;
        final int REQUESTS = 2;

        for (int i = 0; i < CLIENTS; i++) {
            HttpClient httpClient = secureHttpClientFactory.createHttpClient("consumer" + i, "https://www.heise.de");
            clients.add(httpClient);
        }

        final List<String> failures = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            for (final HttpClient client : clients) {
                workers.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ContentResponse response = client.GET(TEST_URL);
                            if (response.getStatus() != 200) {
                                failures.add("Statuscode != 200");
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            failures.add("Unexpected exception:" + e.getMessage());
                        }
                    }
                });
            }
        }

        workers.shutdown();
        workers.awaitTermination(120, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.toString());
        }

        for (HttpClient client : clients) {
            client.stop();
        }

        secureHttpClientFactory.deactivate();
    }

    private Map<String, Object> createConfigMap(int minThreadsShared, int maxThreadsShared, int keepAliveTimeoutShared,
            int minThreadsCustom, int maxThreadsCustom, int keepAliveTimeoutCustom) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("minThreadsShared", minThreadsShared);
        configMap.put("maxThreadsShared", maxThreadsShared);
        configMap.put("keepAliveTimeoutShared", keepAliveTimeoutShared);
        configMap.put("minThreadsCustom", minThreadsCustom);
        configMap.put("maxThreadsCustom", maxThreadsCustom);
        configMap.put("keepAliveTimeoutCustom", keepAliveTimeoutCustom);
        return configMap;
    }
}
