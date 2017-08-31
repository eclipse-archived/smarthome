/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.crypto.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Extension of the default OSGi bundle activator
 *
 * @author Pauli Anttila - Intial contribution
 */
public final class CryptoActivator implements BundleActivator {

    private static byte[] encKey;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    @Override
    public void start(BundleContext bc) throws Exception {

        String key = System.getenv(CryptoModule.ENV_VARIABLE_ENC_PASSPHRASE);
        if ("ASK".equalsIgnoreCase(key)) {
            while (true) {
                System.out.println("Give secret key to continue: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line = br.readLine();
                if (!line.isEmpty()) {
                    // System.out.println("secret key: " + line);
                    encKey = line.getBytes();
                    break;
                }
            }

        } else {
            encKey = key.getBytes();
        }
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    @Override
    public void stop(BundleContext bc) throws Exception {
    }

    public static byte[] getKey() {
        return encKey;
    }

    public static void clearKey() {
        Arrays.fill(encKey, (byte) 0);
        encKey = null;
    }
}
