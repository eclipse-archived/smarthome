/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal.providerImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.net.security.SecureSocketProvider;
import org.eclipse.smarthome.io.net.security.internal.utils.KeystoreUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is responsible for always providing at least one working SSLConfig, based
 * on a self-signed certificate. The parameters for the certificate are user changeable.
 *
 * All certificate parameters, see {@link SelfSignedSSLContextProviderConfig} have default
 * parameters. An early generated self-signed certificate will use those values. As soon as the
 * user commits a new set of configuration parameters, the underlying java keystore will
 * be regenerated and consumers will be notified of the changed SSLContext.
 *
 * @author David Graeff - Initial contribution
 */
@Component(configurationPid = "org.eclipse.smarthome.selfSignedCertificate", immediate = true, service = {
        SelfSignedSSLContextProvider.class, SecureSocketProvider.class }, property = {
                Constants.SERVICE_PID + "=org.eclipse.smarthome.selfSignedCertificate",
                ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=selfSignedCertificateParameters",
                ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=Security",
                ConfigurableService.SERVICE_PROPERTY_LABEL + "=Self signed certificate" })
@NonNullByDefault
public class SelfSignedSSLContextProvider extends SSLContextConfigurableProvider implements SecureSocketProvider {
    private final Logger logger = LoggerFactory.getLogger(SelfSignedSSLContextProvider.class);
    // MessageDigest.getInstance("SHA-256"))

    /**
     * Return the keystore file name.
     * Might be mocked by tests.
     */
    @Override
    protected Path getKeystoreFilename() {
        return Paths.get(ConfigConstants.getUserDataFolder()).resolve("selfsigned.keystore");
    }

    @Activate
    public void activated(@Nullable Map<String, Object> config) throws GeneralSecurityException, IOException {
        serviceContext = "_selfsigned_";
        modified(config);
    }

    @Override
    @Modified
    public void modified(@Nullable Map<String, Object> config) throws GeneralSecurityException, IOException {
        SelfSignedSSLContextProviderConfig c = new Configuration(config).as(SelfSignedSSLContextProviderConfig.class);

        KeyStore keystore = null;
        try {
            keystore = openKeystore(c, getKeystoreFilename(), dateNow());
        } catch (IOException ignored) {
            // File not found. Ignore this error. File has been deliberately removed or it is the first start.
        } catch (GeneralSecurityException e) {
            logger.warn("Access to the self-signed certificate failed: {}", e.getMessage());
        }

        if (keystore == null) {
            // Failed to open the keystore or to access entries. Create a new keystore and tell the user
            logger.warn("No valid TLS keystore with a self-signed certificate found. Creating a new one");
            keystore = KeystoreUtils.createNewKeystoreFile(c, getKeystoreFilename());
        }

        createTLSContexts(keystore, c);
    }
}
