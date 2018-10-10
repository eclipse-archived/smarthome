/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal.providerImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.KeyManagerFactory;

import org.eclipse.smarthome.io.net.security.internal.SecureSocketProviderRegistry;
import org.eclipse.smarthome.io.net.security.internal.utils.TLSAlgorithm;

/**
 * Configuration of the {@link SecureSocketProviderRegistry}.
 * The default signing algorithms use SHA256 for RSA and ES,
 * because {@link https://docs.oracle.com/javase/7/docs/api/java/security/Signature.html}
 * mentions that every "Java platform is required to support the following standard Signature algorithms [...]
 * SHA256withRSA".
 *
 * @author David Graeff - Initial contribution
 */
public class SelfSignedSSLContextProviderConfig extends SSLContextConfigurableProviderConfig {
    // Self-signed certificate configuration data
    public String organizationalUnit = "Eclipse Smarthome";
    public String organization = "Eclipse Foundation, Inc.";
    public String city = "Ottawa";
    public String state = "Ontario";
    public String country = Locale.getDefault().getCountry();
    public Integer validityInDays = 90;
    public String commonName = "127.0.0.1";
    public Boolean autoRefreshCertificate = false;

    public List<String> algorithms = Arrays.asList(
            new TLSAlgorithm("main", "RSA", "SHA256withRSA", 2048, false).toString(),
            new TLSAlgorithm("second", "EC", "SHA256withECDSA", 256, false).toString(),
            new TLSAlgorithm("preshared", "AES", "", 256, true).toString());

    // Defaults to PKIX. Others are SunX509, IbmX509 etc
    public String algorithmKeyManager = KeyManagerFactory.getDefaultAlgorithm();

    // ACME parameters
    public boolean useACME = false;
    public String acmeProvider = "acme://letsencrypt.org/staging";
    public String acmeContact = "mailto:acme@example.com";
}
