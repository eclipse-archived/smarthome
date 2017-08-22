/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * This class creates {@link X509Certificate}s, check if a certificate chain is valid and
 *
 * @author david
 *
 */
@SuppressWarnings("restriction")
public class CertificateUtils {

    /**
     * Decodes a BASE64 string to utf8 characters
     */
    public static char[] base64ToChars(String o) {
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(DatatypeConverter.parseBase64Binary(o))).toString()
                .toCharArray();
    }

    /**
     * Check the certificates of the given keystore for validity.
     *
     * @param ks A KeyStore
     * @param alias The alias within the KeyStore
     * @return Return true if all certificates are valid, false otherwise.
     * @throws KeyStoreException Throws if the KeyStore is not loaded
     * @throws CertificateException Throws if no certificates can be found
     */
    protected static boolean checkCertificate(Date date, Certificate[] certificateChain, boolean throwException)
            throws KeyStoreException, CertificateException {
        for (Certificate c : certificateChain) {
            try {
                ((X509Certificate) c).checkValidity(date);
            } catch (CertificateException e) {
                if (throwException) {
                    throw e;
                }
                return false;
            }
        }
        return true;
    }

    public static X509Certificate[] createX509Cert(ServiceConfiguration configuration, String domain, String sigAlg,
            KeyPair key, Date firstDate) throws GeneralSecurityException, IOException {
        X509CertImpl cert;
        Date lastDate;
        long validityInS = configuration.validityInDays.longValue() * 24 * 60 * 60;
        X500Name x500Name = new X500Name(domain, configuration.organizationalUnit, configuration.organization,
                configuration.city, configuration.state, configuration.country);

        try {
            lastDate = new Date();
            lastDate.setTime(firstDate.getTime() + validityInS * 1000);

            X509CertInfo info = new X509CertInfo();
            // Add all mandatory attributes
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            info.set(X509CertInfo.SERIAL_NUMBER,
                    new CertificateSerialNumber(new SecureRandom().nextInt() & 0x7fffffff));
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(sigAlg)));
            info.set(X509CertInfo.SUBJECT, x500Name);
            info.set(X509CertInfo.KEY, new CertificateX509Key(key.getPublic()));
            info.set(X509CertInfo.VALIDITY, new CertificateValidity(firstDate, lastDate));
            info.set(X509CertInfo.ISSUER, x500Name);

            cert = new X509CertImpl(info);
            cert.sign(key.getPrivate(), sigAlg);

            return new X509Certificate[] { cert };

        } catch (IOException e) {
            throw new CertificateEncodingException("getSelfCert: " + e.getMessage());
        }
    }
}
