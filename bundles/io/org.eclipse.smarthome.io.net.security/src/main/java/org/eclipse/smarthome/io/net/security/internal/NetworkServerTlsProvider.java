/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.security.api.TLSContextChangedListener;
import org.eclipse.smarthome.io.security.api.TlsNetworkServer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is responsible for providing SSLContexts that can be used all over ESH
 * for server applications like webservlets, Mqtt servers or any extension that need to
 * provide for instance a REST interface.
 *
 * The SSLContext is configured to use either the user provided java keystore file
 * (`../etc/default.keystore`) or if none is provided to generate a self-signed certificate
 * and public/private key-pair.
 *
 * @author David Graeff - Initial contribution
 */
@SuppressWarnings("restriction")
@Component(configurationPid = NetworkServerTlsProvider.CONFIG_PID, immediate = false, service = TlsNetworkServer.class)
@NonNullByDefault
public class NetworkServerTlsProvider implements TlsNetworkServer {
    public static final String CONFIG_PID = "networkServerTlsProvider";
    protected ServiceConfiguration configuration = new ServiceConfiguration();
    protected ContextConfiguration defaultContext = new ContextConfiguration();
    protected @Nullable ComputeConfigurationDifference utils;
    protected Collection<TLSContextChangedListener> contextChangedListener = Collections.emptyList();
    private final Logger logger = LoggerFactory.getLogger(NetworkServerTlsProvider.class);

    @Activate
    public void activated(BundleContext bundleContext, Map<String, Object> config) throws NoSuchAlgorithmException {
        configuration = new Configuration(config).as(ServiceConfiguration.class);
        utils = new ComputeConfigurationDifference(configuration.contexts,
                Paths.get(ConfigConstants.getUserDataFolder()), MessageDigest.getInstance("SHA-256"));
    }

    @Modified
    public void modified(Map<String, Object> config) {
        ServiceConfiguration newConfiguration = new Configuration(config).as(ServiceConfiguration.class);
        Collection<String> changedContexts = utils.computeDifference(newConfiguration.contexts,
                Paths.get(ConfigConstants.getUserDataFolder()));
        configuration = newConfiguration;
        ContextConfiguration defaultContext = configuration.contexts.stream().filter(c -> "default".equals(c.context))
                .findFirst().orElse(null);
        if (defaultContext == null) {
            defaultContext = new ContextConfiguration();
            configuration.contexts.add(defaultContext);
            try {
                utils.writeConfiguration(configuration);
            } catch (IOException e) {
                logger.warn("Couldn't write ", e);
            }
        }
        this.defaultContext = defaultContext;
        changedContexts.forEach(contextName -> contextChangedListener.forEach(l -> l.tlsContextChanged(contextName)));
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

    @Override
    public SSLContext createSSLContext(String context, String securityProvider)
            throws GeneralSecurityException, IOException {
        ContextConfiguration contextConfig = context == null ? defaultContext
                : configuration.contexts.stream().filter(c -> context.equals(c.context)).findFirst()
                        .orElse(defaultContext);

        KeyStore keyStore = getKeyStore(context);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(configuration.algorithmKeyManager);
        kmf.init(keyStore, contextConfig.privateKeyPasswordBase64);

        SSLContext sslContext = SSLContext.getInstance(securityProvider);
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        return sslContext;
    }

    @Override
    public void registerContextChangedListener(TLSContextChangedListener listener) {
        contextChangedListener.add(listener);
    }

    @Override
    public void unregisterContextChangedListener(TLSContextChangedListener listener) {
        contextChangedListener.remove(listener);
    }

    private KeyStore getKeyStore(@Nullable String context) throws GeneralSecurityException, IOException {
        ContextConfiguration contextConfig = context == null ? defaultContext
                : configuration.contexts.stream().filter(c -> context.equals(c.context)).findFirst()
                        .orElse(defaultContext);

        KeyStore ks = KeyStore.getInstance(contextConfig.keystoreFormat);
        File keystoreFilename = contextConfig.getAbsoluteKeystoreFilename(basedir);
        ks.load(new FileInputStream(keystoreFilename), contextConfig.keystorePasswordBase64);

        String alias = contextConfig.keystoreAlias;
        boolean createSelfSigned = true;
        // Check certificates for the users chosen alias. If autoRefresh is off this will throw.
        if (ks.containsAlias(alias)) {
            createSelfSigned = !CertificateUtils.checkCertificate(dateNow(), ks.getCertificateChain(alias),
                    !contextConfig.autoRefreshInvalidCertificate);
        }

        // Create KeyPair and Cert and add it to the KeyStore. Save the KeyStore to disk.
        if (createSelfSigned) {
            for (TLSAlgorithm algo : configuration.algorithms) {
                KeyPairGenerator gen = KeyPairGenerator.getInstance(algo.algorithm);
                gen.initialize(algo.keysize.intValue(), new SecureRandom());
                KeyPair keyPair = gen.generateKeyPair();
                assert keyPair != null;
                if (!"X.509".equalsIgnoreCase(keyPair.getPublic().getFormat())) {
                    throw new IllegalArgumentException(
                            "publicKey is not X.509, but " + keyPair.getPublic().getFormat());
                }
                ks.setKeyEntry(alias, keyPair.getPrivate(), contextConfig.privateKeyPasswordBase64,
                        CertificateUtils.createX509Cert(configuration, contextConfig.domain, algo.algorithmSigning,
                                keyPair, new Date()));
            }

            // keytool -genseckey -keystore keystore.jck -storetype jceks -storepass secret -keyalg AES
            // -keysize 256 -alias main -keypass mykeypass

            if (contextConfig.preSharedKeyBase64 != null) {
                SecretKey AESkey = new SecretKeySpec(contextConfig.preSharedKeyBase64, 0,
                        contextConfig.preSharedKeyBase64.length, "AES");
                ks.setEntry(contextConfig.keystoreAlias, new KeyStore.SecretKeyEntry(AESkey),
                        new KeyStore.PasswordProtection(contextConfig.privateKeyPasswordBase64));
            }

            ks.store(new FileOutputStream(keystoreFilename), contextConfig.keystorePasswordBase64);
            ks.load(new FileInputStream(keystoreFilename), contextConfig.keystorePasswordBase64);

            contextConfig.autoRefreshInvalidCertificate = true;

            contextChangedListener.forEach(l -> {
                assert contextConfig.context != null;
                l.tlsContextChanged(contextConfig.context);
            });
        }

        return ks;
    }
}
