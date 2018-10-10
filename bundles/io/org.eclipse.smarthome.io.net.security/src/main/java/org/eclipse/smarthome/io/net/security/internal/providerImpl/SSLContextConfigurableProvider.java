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
package org.eclipse.smarthome.io.net.security.internal.providerImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.registry.AbstractProvider;
import org.eclipse.smarthome.io.net.security.SecureSocketProvider;
import org.eclipse.smarthome.io.net.security.internal.utils.CertificateUtils;
import org.eclipse.smarthome.io.net.security.internal.utils.KeystoreFileWatcher;
import org.eclipse.smarthome.io.net.security.internal.utils.KeystoreUtils;
import org.eclipse.smarthome.io.net.security.internal.utils.ProviderWithCapabilities;
import org.eclipse.smarthome.io.security.api.SecureSocketCapabilities;
import org.eclipse.smarthome.io.security.api.SecureSocketCapability;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing service for multi-context configurations.
 *
 * @author Stefan Triller - initial contribution
 *
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, service = {
        SecureSocketProvider.class }, configurationPid = "org.eclipse.smarthome.secureSocketProviderInstance")
@NonNullByDefault
public class SSLContextConfigurableProvider extends AbstractProvider<ProviderWithCapabilities>
        implements SecureSocketProvider {
    protected @Nullable String serviceContext = null;
    protected transient @Nullable SSLContextConfigurableProviderConfig config;
    private @Nullable KeystoreFileWatcher watcher;
    private @Nullable Future<?> future;
    protected List<ProviderWithCapabilities> providers = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(SSLContextConfigurableProvider.class);

    protected Path getKeystoreFilename() {
        return Paths.get(ConfigConstants.getUserDataFolder()).resolve(serviceContext + ".keystore");
    }

    @Activate
    public void activate(Map<String, Object> properties) throws IOException, GeneralSecurityException {
        watcher = new KeystoreFileWatcher();
        modified(properties);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        future = executor.submit(watcher);
        executor.shutdown();
    }

    @Deactivate
    public void deactivate() {
        if (future != null) {
            future.cancel(true);
            watcher = null;
            future = null;
        }
    }

    @Modified
    public void modified(Map<String, Object> properties) throws IOException, GeneralSecurityException {
        serviceContext = properties.getOrDefault("esh.servicecontext", properties.get(Constants.SERVICE_PID))
                .toString();

        config = new Configuration(properties).as(SSLContextConfigurableProviderConfig.class);
        loadKeyStore();
    }

    protected void loadKeyStore() throws IOException, GeneralSecurityException {
        SSLContextConfigurableProviderConfig config = this.config;
        if (config == null) {
            return;
        }
        providers.forEach(element -> notifyListenersAboutRemovedElement(element));
        providers.clear();
        KeyStore keystore = openKeystore(config, getKeystoreFilename(), dateNow());
        createTLSContexts(keystore, config);

        if (watcher != null) {
            watcher.setWatchFile(getKeystoreFilename(), () -> {
                try {
                    loadKeyStore();
                } catch (IOException | GeneralSecurityException e) {
                    logger.warn("Reloading keystore after file {} change failed: {}", getKeystoreFilename(),
                            e.getMessage());
                }
            });
        }
    }

    /**
     * Return the current data. Used to check if certificates are still valid.
     * May be mocked by tests.
     *
     * @return The current date
     */
    protected Date dateNow() {
        return new Date();
    }

    protected static KeyStore openKeystore(SSLContextConfigurableProviderConfig keystoreConfig, Path keystoreFilename,
            Date date) throws IOException, GeneralSecurityException {
        // Check if opening the keystore works
        KeyStore localKeystore = KeyStore.getInstance(keystoreConfig.keystoreFormat);
        localKeystore.load(Files.newInputStream(keystoreFilename),
                keystoreConfig.keystorePassword.asCharBuffer().array());
        // Check if all private keys are accessible
        Enumeration<String> aliases = localKeystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (localKeystore.isKeyEntry(alias)) {
                // Try to access private keys
                localKeystore.getKey(alias, keystoreConfig.privateKeyPassword.asCharBuffer().array());
            } else if (localKeystore.isCertificateEntry(alias)) {
                // Check certificates for validity
                CertificateUtils.checkCertificate(date, localKeystore.getCertificateChain(alias), true);
            }

        }
        return localKeystore;
    }

    protected void createTLSContexts(KeyStore keystore, SSLContextConfigurableProviderConfig configuration)
            throws GeneralSecurityException, IOException {

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, configuration.privateKeyPassword.asCharBuffer().array());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keystore);

        SSLContext tlsContext = SSLContext.getInstance("TLSv1.2");
        if (tlsContext != null) {
            tlsContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            Set<SecureSocketCapability> caps = Stream
                    .of(SecureSocketCapabilities.TLS, SecureSocketCapabilities.SSLCONTEXT).collect(Collectors.toSet());
            if (KeystoreUtils.isCAcert(keystore)) {
                caps.add(SecureSocketCapabilities.CA_SIGNED);
            }
            providers.add(new ProviderWithCapabilities(tlsContext, caps, this));
            notifyListenersAboutAddedElement(providers.get(providers.size() - 1));
        }

        // Works in Java9+
        SSLContext dtlsContext = SSLContext.getInstance("DTLSv1.2");
        if (dtlsContext != null) {
            dtlsContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            Set<SecureSocketCapability> caps = Stream
                    .of(SecureSocketCapabilities.DTLS, SecureSocketCapabilities.SSLCONTEXT).collect(Collectors.toSet());
            if (KeystoreUtils.isCAcert(keystore)) {
                caps.add(SecureSocketCapabilities.CA_SIGNED);
            }
            providers.add(new ProviderWithCapabilities(tlsContext, caps, this));
            notifyListenersAboutAddedElement(providers.get(providers.size() - 1));
        }
    }

    @Override
    public String getUID() {
        String s = (@NonNull String) serviceContext;
        return s;
    }

    @Override
    public Collection<ProviderWithCapabilities> getAll() {
        return providers;
    }
}
