/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.KeyManagerFactory;

/**
 * Configuration of the {@link NetworkServerTlsProvider}.
 * The default signing algorithms use SHA256 for RSA and ES,
 * because {@link https://docs.oracle.com/javase/7/docs/api/java/security/Signature.html}
 * mentions that every "Java platform is required to support the following standard Signature algorithms [...]
 * SHA256withRSA".
 *
 * @author David Graeff - Initial contribution
 */
public class ServiceConfiguration {
    // Self-signed certificate configuration data
    public String organizationalUnit = "Eclipse Smarthome";
    public String organization = "Eclipse Foundation, Inc.";
    public String city = "Ottawa";
    public String state = "Ontario";
    public String country = Locale.getDefault().getCountry();
    public BigDecimal validityInDays = BigDecimal.valueOf(365 * 3);

    public List<TLSAlgorithm> algorithms = new ArrayList<>();

    // Defaults to PKIX. Others are SunX509, IbmX509 etc
    public String algorithmKeyManager = KeyManagerFactory.getDefaultAlgorithm();

    // ACME parameters
    public boolean useACME = false;
    public String acmeProvider = "acme://letsencrypt.org/staging";
    public String acmeContact = "mailto:acme@example.com";

    // Configuration contexts
    public List<ContextConfiguration> contexts = new ArrayList<>();

    /**
     * Creates a {@link ServiceConfiguration} with default parameters
     */
    public ServiceConfiguration() {
        algorithms.add(new TLSAlgorithm("main", "RSA", "SHA256withRSA", BigDecimal.valueOf(2048)));
        algorithms.add(new TLSAlgorithm("second", "EC", "SHA256withECDSA", BigDecimal.valueOf(256)));
    }
}
