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
package org.eclipse.smarthome.io.iota.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IotaSeedGenerator} generates seed and key for the iota Tangle
 *
 * @author Theo Giovanna - Initial Contribution
 */
public class IotaSeedGenerator {

    private final Logger logger = LoggerFactory.getLogger(IotaSeedGenerator.class);
    private static final String TRYTE_ALPHABET = "9ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String KEY_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // MAM allows for uppercase char only
    private static final int SEED_LEN = 81;
    private SecureRandom sr = null;

    public IotaSeedGenerator() {

    }

    /**
     * Generate a new seed
     *
     * @return a seed
     */
    public String getNewSeed() {
        try {
            sr = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            logger.debug("Exception happened: {}", e);
        }

        StringBuilder sb = new StringBuilder(SEED_LEN);
        for (int i = 0; i < SEED_LEN; i++) {
            int n = sr.nextInt(27);
            char c = TRYTE_ALPHABET.charAt(n);
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Generate a new private key
     *
     * @return a private key
     */
    public String getNewPrivateKey() {
        try {
            sr = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            logger.debug("Exception happened: {}", e);
        }

        StringBuilder sb = new StringBuilder(SEED_LEN);
        for (int i = 0; i < SEED_LEN; i++) {
            int n = sr.nextInt(26);
            char c = KEY_ALPHABET.charAt(n);
            sb.append(c);
        }
        return sb.toString();
    }
}
