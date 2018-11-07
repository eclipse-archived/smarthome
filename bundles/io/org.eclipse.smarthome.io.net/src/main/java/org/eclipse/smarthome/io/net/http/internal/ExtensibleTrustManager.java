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

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.security.auth.x500.X500Principal;

import org.eclipse.smarthome.io.net.http.TlsCertificateProvider;
import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an extensible composite TrustManager
 *
 * The trust manager can be extended with implementations of the following interfaces:
 *
 * - {@code TlsTrustManagerProvider}
 * - {@code TlsCertificateProvider}
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@Component(service = ExtensibleTrustManager.class, immediate = true)
public class ExtensibleTrustManager extends X509ExtendedTrustManager {
    private final Logger logger = LoggerFactory.getLogger(ExtensibleTrustManager.class);

    private static final Queue<X509ExtendedTrustManager> EMPTY_QUEUE = new ConcurrentLinkedQueue<>();

    private final X509ExtendedTrustManager defaultTrustManager = TrustManagerUtil.keyStoreToTrustManager(null);
    private final Map<String, Queue<X509ExtendedTrustManager>> linkedTrustManager = new ConcurrentHashMap<>();
    private final Map<TlsCertificateProvider, X509ExtendedTrustManager> mappingFromTlsCertificateProvider = new ConcurrentHashMap<>();

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkClientTrusted(chain, authType, (Socket) null);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkServerTrusted(chain, authType, (Socket) null);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkClientTrusted(chain, authType, socket);
        } else {
            linkedTrustManager.checkClientTrusted(chain, authType, socket);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkClientTrusted(chain, authType, sslEngine);
        } else {
            linkedTrustManager.checkClientTrusted(chain, authType, sslEngine);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkServerTrusted(chain, authType, socket);
        } else {
            linkedTrustManager.checkServerTrusted(chain, authType, socket);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine sslEngine)
            throws CertificateException {
        X509ExtendedTrustManager linkedTrustManager = getLinkedTrustMananger(chain);
        if (linkedTrustManager == null) {
            logger.trace("No specific trust manager found, falling back to default");
            defaultTrustManager.checkServerTrusted(chain, authType, sslEngine);
        } else {
            linkedTrustManager.checkServerTrusted(chain, authType, sslEngine);
        }
    }

    private X509ExtendedTrustManager getLinkedTrustMananger(X509Certificate[] chain) {
        try {
            String commonName = getCommonName(chain[0]);

            X509ExtendedTrustManager trustManager = linkedTrustManager.getOrDefault(commonName, EMPTY_QUEUE).peek();

            if (trustManager != null) {
                logger.trace("Found trustManager by common name: {}", commonName);
                return trustManager;
            }

            logger.trace("Searching trustManager by Subject Alternative Names: {}",
                    chain[0].getSubjectAlternativeNames());
            // @formatter:off
            return chain[0].getSubjectAlternativeNames().stream()
                    .map(e -> e.get(1))
                    .map(Object::toString)
                    .map(linkedTrustManager::get)
                    .filter(Objects::nonNull)
                    .map(Queue::peek)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            // @formatter:on
        } catch (CertificateParsingException e) {
            throw new IllegalStateException("Problem while parsing certificate", e);
        }
    }

    private String getCommonName(X509Certificate x509Certificate) {
        String dn = x509Certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);
        for (String group : dn.split(",")) {
            if (group.contains("CN=")) {
                return group.trim().replace("CN=", "");
            }
        }
        throw new IllegalStateException("No Common Name found");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addTlsCertificateProvider(TlsCertificateProvider tlsCertificateProvider) {
        X509ExtendedTrustManager trustManager = new TlsCertificateTrustManagerAdapter(tlsCertificateProvider)
                .getTrustManager();
        mappingFromTlsCertificateProvider.put(tlsCertificateProvider, trustManager);
        addLinkedTrustManager(tlsCertificateProvider.getHostName(), trustManager);
    }

    protected void removeTlsCertificateProvider(TlsCertificateProvider tlsCertificateProvider) {
        removeLinkedTrustManager(tlsCertificateProvider.getHostName(),
                mappingFromTlsCertificateProvider.remove(tlsCertificateProvider));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addTlsTrustManagerProvider(TlsTrustManagerProvider tlsTrustManagerProvider) {
        addLinkedTrustManager(tlsTrustManagerProvider.getHostName(), tlsTrustManagerProvider.getTrustManager());
    }

    protected void removeTlsTrustManagerProvider(TlsTrustManagerProvider tlsTrustManagerProvider) {
        removeLinkedTrustManager(tlsTrustManagerProvider.getHostName(), tlsTrustManagerProvider.getTrustManager());
    }

    private void addLinkedTrustManager(String hostName, X509ExtendedTrustManager trustManager) {
        linkedTrustManager.computeIfAbsent(hostName, h -> new ConcurrentLinkedQueue<>()).add(trustManager);
    }

    private void removeLinkedTrustManager(String hostName, X509ExtendedTrustManager trustManager) {
        linkedTrustManager.computeIfAbsent(hostName, h -> new ConcurrentLinkedQueue<>()).remove(trustManager);
    }
}
