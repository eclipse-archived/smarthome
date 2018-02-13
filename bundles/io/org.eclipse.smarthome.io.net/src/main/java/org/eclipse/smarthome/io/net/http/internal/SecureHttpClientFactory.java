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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.io.net.http.CustomTrustManagerProvider;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to create jetty http clients
 *
 * @author Michael Bock - initial API
 */
@Component(service = HttpClientFactory.class, immediate = true, name = "org.eclipse.smarthome.io.net.http.internal.SecureHttpClientFactory", configurationPid = "org.eclipse.smarthome.io.net.http.HttpClientFactory")
public class SecureHttpClientFactory implements HttpClientFactory {

    private final Logger logger = LoggerFactory.getLogger(SecureHttpClientFactory.class);

    private static final String CONFIG_MIN_THREADS = "minThreads";
    private static final String CONFIG_MAX_THREADS = "maxThreads";
    private static final String CONFIG_KEEP_ALIVE = "keepAliveTimeout";
    private static final String CONFIG_QUEUE_SIZE = "queueSize";

    private static final int MIN_CONSUMER_NAME_LENGTH = 4;
    private static final int MAX_CONSUMER_NAME_LENGTH = 20;
    private static final Pattern CONSUMER_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-]*");

    private CustomTrustManagerProvider customTrustmanagerProvider;

    private int minThreads;
    private int maxThreads;
    private int keepAliveTimeout;
    private int queueSize;

    private ThreadPoolExecutor threadPool;

    @Activate
    protected void activate(Map<String, Object> parameters) {
        minThreads = (Integer) parameters.get(CONFIG_MIN_THREADS);
        maxThreads = (Integer) parameters.get(CONFIG_MAX_THREADS);
        keepAliveTimeout = (Integer) parameters.get(CONFIG_KEEP_ALIVE);
        queueSize = (Integer) parameters.get(CONFIG_QUEUE_SIZE);
        threadPool = new ThreadPoolExecutor(minThreads, maxThreads, keepAliveTimeout, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize), new NamedThreadFactory("jetty"));
        logger.info("jetty thread pool started with min threads {}, max threads {}", minThreads, maxThreads);
    }

    @Modified
    protected void modified(Map<String, Object> parameters) {
        minThreads = (Integer) parameters.get(CONFIG_MIN_THREADS);
        maxThreads = (Integer) parameters.get(CONFIG_MAX_THREADS);
        keepAliveTimeout = (Integer) parameters.get(CONFIG_KEEP_ALIVE);
        queueSize = (Integer) parameters.get(CONFIG_QUEUE_SIZE);
        threadPool.setCorePoolSize(minThreads);
        threadPool.setMaximumPoolSize(maxThreads);
        threadPool.setKeepAliveTime(keepAliveTimeout, TimeUnit.SECONDS);
        logger.debug("jetty thread pool changed to min threads {}, max threads {}", minThreads, maxThreads);
    }

    @Deactivate
    protected void deactivate() {
        threadPool.shutdown();
        logger.info("jetty thread pool shutdown");
    }

    @Override
    public HttpClient getHttpClient(String consumerName, String endpoint) {
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        logger.debug("httpClient for endpoint {} requested", endpoint);
        checkConsumerName(consumerName);
        return createHttpClient(consumerName, endpoint);
    }

    @Override
    public HttpClient getHttpClient(String consumerName) {
        logger.debug("httpClient for requested");
        checkConsumerName(consumerName);
        return createHttpClient(consumerName, null);
    }

    private HttpClient createHttpClient(String consumerName, String endpoint) {
        logger.info("creating httpClient for endpoint {}", endpoint);
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        if (endpoint != null && customTrustmanagerProvider != null) {
            Stream<TrustManager> trustManagerStream = customTrustmanagerProvider.getTrustManagers(endpoint);
            TrustManager[] trustManagers = trustManagerStream.toArray(TrustManager[]::new);
            if (trustManagers.length > 0) {
                logger.info("using custom trustmanagers (certificate pinning) for httpClient for endpoint {}",
                        endpoint);
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustManagers, null);
                    sslContextFactory.setSslContext(sslContext);
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot create an TLS context for the endpoint '" + endpoint + "'!", ex);
                }
            }
        }
        String excludeCipherSuites[] = { "^.*_(MD5)$" };
        sslContextFactory.setExcludeCipherSuites(excludeCipherSuites);

        HttpClient httpClient = new HttpClient(sslContextFactory);
        httpClient.setExecutor(threadPool);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Could not start jetty client", e);
            throw new RuntimeException("Could not start jetty client", e);
        }

        return httpClient;
    }

    private void checkConsumerName(String consumerName) {
        Objects.requireNonNull(consumerName, "consumerName must not be null");
        if (consumerName.length() < MIN_CONSUMER_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "consumerName " + consumerName + " too short, minimum " + MIN_CONSUMER_NAME_LENGTH);
        }
        if (consumerName.length() > MAX_CONSUMER_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "consumerName " + consumerName + " too long, maximum " + MAX_CONSUMER_NAME_LENGTH);
        }
        if (!CONSUMER_NAME_PATTERN.matcher(consumerName).matches()) {
            throw new IllegalArgumentException(
                    "consumerName " + consumerName + " contains illegal character, allowed only [a-zA-Z0-9_-]");
        }
    }

    @Reference(service = CustomTrustManagerProvider.class, cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void bindCustomTrustmanagerProvider(CustomTrustManagerProvider customTrustmanagerProvider) {
        this.customTrustmanagerProvider = customTrustmanagerProvider;
    }

    protected void unbindCustomTrustmanagerProvider(CustomTrustManagerProvider customTrustmanagerProvider) {
        this.customTrustmanagerProvider = null;
    }
}
