/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.sslcontext;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptAllCertifcatesSSLContext implements SSLContextProvider {
    private final Logger logger = LoggerFactory.getLogger(PeriodicReconnectPolicy.class);

    @Override
    public SSLContext getContext() throws ConfigurationException {
        // use standard JSSE available in the runtime and
        // use TLSv1.2 which is the default for a secured mosquitto
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } }, new java.security.SecureRandom());
            return sslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.warn("SSL configuration failed", e);
            throw new ConfigurationException(e.getMessage());
        }
    }
}
