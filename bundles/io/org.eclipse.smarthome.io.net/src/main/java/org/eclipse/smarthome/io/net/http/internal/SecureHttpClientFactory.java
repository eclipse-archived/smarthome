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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.io.net.http.CommonHttpClient;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
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
@Component(service = HttpClientFactory.class, immediate = true, configurationPid = "org.eclipse.smarthome.HttpClientFactory")
public class SecureHttpClientFactory implements HttpClientFactory {

    private final Logger logger = LoggerFactory.getLogger(SecureHttpClientFactory.class);

    private static final String CONFIG_MIN_THREADS_SHARED = "minThreadsShared";
    private static final String CONFIG_MAX_THREADS_SHARED = "maxThreadsShared";
    private static final String CONFIG_KEEP_ALIVE_SHARED = "keepAliveTimeoutShared";
    private static final String CONFIG_MIN_THREADS_CUSTOM = "minThreadsCustom";
    private static final String CONFIG_MAX_THREADS_CUSTOM = "maxThreadsCustom";
    private static final String CONFIG_KEEP_ALIVE_CUSTOM = "keepAliveTimeoutCustom";

    private static final int MIN_CONSUMER_NAME_LENGTH = 4;
    private static final int MAX_CONSUMER_NAME_LENGTH = 20;
    private static final Pattern CONSUMER_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-]*");

    private volatile TrustManagerProvider trustmanagerProvider;

    private ThreadPoolExecutor threadPool = null;
    private HttpClient sharedHttpClient = null;
    private CommonHttpClient sharedHttpClientFacade = null;

    private int minThreadsShared;
    private int maxThreadsShared;
    private int keepAliveTimeoutShared;
    private int minThreadsCustom;
    private int maxThreadsCustom;
    private int keepAliveTimeoutCustom;

    @Activate
    protected void activate(Map<String, Object> parameters) {
        getConfigParameters(parameters);
    }

    @Modified
    protected void modified(Map<String, Object> parameters) {
        getConfigParameters(parameters);
        if (threadPool != null) {
            threadPool.setCorePoolSize(minThreadsShared);
            threadPool.setMaximumPoolSize(maxThreadsShared);
            threadPool.setKeepAliveTime(keepAliveTimeoutShared, TimeUnit.SECONDS);
        }
    }

    @Deactivate
    protected void deactivate() {
        try {
            if (sharedHttpClient != null) {
                sharedHttpClient.stop();
                sharedHttpClientFacade = null;
                sharedHttpClient = null;
                logger.info("jetty shared http client stopped");
            }
        } catch (Exception e) {
            logger.error("error while stppping jetty shared http client", e);
            // nothing else we can do here
        }

        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
            logger.info("jetty thread pool shutdown");
        }
    }

    @Override
    public HttpClient createHttpClient(String consumerName, String endpoint) {
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        logger.debug("httpClient for endpoint {} requested", endpoint);
        checkConsumerName(consumerName);
        return createHttpClientInternal(consumerName, endpoint);
    }

    @Override
    public CommonHttpClient getHttpClient() {
        initialize();
        logger.debug("shared httpClient requested");
        return sharedHttpClientFacade;
    }

    private void getConfigParameters(Map<String, Object> parameters) {
        minThreadsShared = (int) parameters.get(CONFIG_MIN_THREADS_SHARED);
        maxThreadsShared = (int) parameters.get(CONFIG_MAX_THREADS_SHARED);
        keepAliveTimeoutShared = (int) parameters.get(CONFIG_KEEP_ALIVE_SHARED);
        minThreadsCustom = (int) parameters.get(CONFIG_MIN_THREADS_CUSTOM);
        maxThreadsCustom = (int) parameters.get(CONFIG_MAX_THREADS_CUSTOM);
        keepAliveTimeoutCustom = (int) parameters.get(CONFIG_KEEP_ALIVE_CUSTOM);
    }

    private void initialize() {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public synchronized Void run() {

                    if (threadPool == null) {
                        threadPool = createThreadPool("common", minThreadsShared, maxThreadsShared,
                                keepAliveTimeoutShared);
                    }

                    if (sharedHttpClient == null) {
                        sharedHttpClient = createHttpClientInternal("common", null);
                        sharedHttpClientFacade = new HttpClientDelegate(sharedHttpClient);
                        logger.info("jetty shared http client created");
                    }

                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }

        }
    }

    private HttpClient createHttpClientInternal(String consumerName, String endpoint) {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<HttpClient>() {
                @Override
                public HttpClient run() {

                    logger.info("creating httpClient for endpoint {}", endpoint);
                    SslContextFactory sslContextFactory = new SslContextFactory();
                    sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
                    if (endpoint != null && trustmanagerProvider != null) {
                        Stream<TrustManager> trustManagerStream = trustmanagerProvider.getTrustManagers(endpoint);
                        TrustManager[] trustManagers = trustManagerStream.toArray(TrustManager[]::new);
                        if (trustManagers.length > 0) {
                            logger.info(
                                    "using custom trustmanagers (certificate pinning) for httpClient for endpoint {}",
                                    endpoint);
                            try {
                                SSLContext sslContext = SSLContext.getInstance("TLS");
                                sslContext.init(null, trustManagers, null);
                                sslContextFactory.setSslContext(sslContext);
                            } catch (Exception ex) {
                                throw new RuntimeException(
                                        "Cannot create an TLS context for the endpoint '" + endpoint + "'!", ex);
                            }
                        }
                    }
                    String excludeCipherSuites[] = { "^.*_(MD5)$" };
                    sslContextFactory.setExcludeCipherSuites(excludeCipherSuites);

                    HttpClient httpClient = new HttpClient(sslContextFactory);
                    final ThreadPoolExecutor threadPoolExecutor = createThreadPool(consumerName, minThreadsCustom,
                            maxThreadsCustom, keepAliveTimeoutCustom);

                    // we need to add this thread pool to the client as managed object,
                    // so that it will become shutdown when the client is stopped
                    httpClient.addManaged(new AbstractLifeCycle() {
                        @Override
                        protected void doStop() throws Exception {
                            threadPoolExecutor.shutdown();
                        }
                    });

                    httpClient.setExecutor(threadPoolExecutor);

                    try {
                        httpClient.start();
                    } catch (Exception e) {
                        logger.error("Could not start jetty client", e);
                        throw new RuntimeException("Could not start jetty client", e);
                    }

                    return httpClient;
                }
            });
        } catch (PrivilegedActionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    private ThreadPoolExecutor createThreadPool(String consumerName, int minThreads, int maxThreads,
            int keepAliveTimeout) {
        // We need an "empty" queue, because otherwise jetty workers become queued instead of executed.
        // This will cause jetty to hang!
        // SynchronousQueue is fine for this, because it is effectively an empty queue
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(minThreads, maxThreads, keepAliveTimeout,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("jetty-" + consumerName));
        logger.info("jetty thread pool started with min threads {}, max threads {}", minThreads, maxThreads);
        return threadPoolExecutor;
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

    @Reference(service = TrustManagerProvider.class, cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setTrustmanagerProvider(TrustManagerProvider trustmanagerProvider) {
        this.trustmanagerProvider = trustmanagerProvider;
    }

    protected void unsetTrustmanagerProvider(TrustManagerProvider trustmanagerProvider) {
        if (this.trustmanagerProvider == trustmanagerProvider) {
            this.trustmanagerProvider = null;
        }
    }
}
