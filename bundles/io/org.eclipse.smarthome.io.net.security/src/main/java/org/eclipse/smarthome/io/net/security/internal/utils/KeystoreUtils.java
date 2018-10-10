/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProviderConfig;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SelfSignedSSLContextProviderConfig;

/**
 * Keystore utility methods like creating a keystore or check if a keystore contains a CA signed certificate.
 *
 * @author david
 *
 */
public class KeystoreUtils {
    public static boolean isCAcert(KeyStore localKeystore) throws KeyStoreException {
        Enumeration<String> aliases = localKeystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (localKeystore.isCertificateEntry(alias)) {
                Certificate[] certificateChain = localKeystore.getCertificateChain(alias);
                if (CertificateUtils.isCAcert(certificateChain)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new keystore file with public/private key and self-signed certificate entries for all asynchronous
     * algorithms and pre-shared key entries for all synchronous algorithms ("AES").
     *
     * @param configuration
     * @param keystoreFilename
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static KeyStore createNewKeystoreFile(SelfSignedSSLContextProviderConfig configuration,
            Path keystoreFilename) throws GeneralSecurityException, IOException {

        KeyStore ks = KeyStore.getInstance(configuration.keystoreFormat);

        // Create KeyPair and Cert and add it to the KeyStore. Save the KeyStore to disk.
        for (String algoString : configuration.algorithms) {
            TLSAlgorithm algo = TLSAlgorithm.fromString(algoString);
            if (algo == null) { // Ignore invalid algorithm descriptions
                continue;
            }
            KeyPairGenerator gen = KeyPairGenerator.getInstance(algo.algorithm);
            gen.initialize(algo.keysize, new SecureRandom());
            KeyPair keyPair = gen.generateKeyPair();
            assert keyPair != null;
            if (!"X.509".equalsIgnoreCase(keyPair.getPublic().getFormat())) {
                throw new IllegalArgumentException("publicKey is not X.509, but " + keyPair.getPublic().getFormat());
            }
            if (algo.preShared) {
                ByteBuffer presharedKey = SSLContextConfigurableProviderConfig.randomPassword(algo.keysize);
                SecretKey AESkey = new SecretKeySpec(presharedKey.array(), 0, presharedKey.array().length,
                        algo.algorithm);
                ks.setEntry(algo.keystoreAlias, new KeyStore.SecretKeyEntry(AESkey),
                        new KeyStore.PasswordProtection(configuration.privateKeyPassword.asCharBuffer().array()));
            } else {
                ks.setKeyEntry(algo.keystoreAlias, keyPair.getPrivate(),
                        configuration.privateKeyPassword.asCharBuffer().array(),
                        CertificateUtils.createX509Cert(configuration, algo.algorithmSigning, keyPair, new Date()));
            }
        }

        // keytool -genseckey -keystore keystore.jck -storetype jceks -storepass secret -keyalg AES
        // -keysize 256 -alias main -keypass mykeypass

        ks.store(Files.newOutputStream(keystoreFilename), configuration.keystorePassword.asCharBuffer().array());
        ks.load(Files.newInputStream(keystoreFilename), configuration.keystorePassword.asCharBuffer().array());

        return ks;
    }
}
