/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal.utils;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Defines all parameters for an encryption algorithm
 *
 * @author David Graeff - Initial contribution
 */
public class TLSAlgorithm {
    public @NonNull String keystoreAlias;
    public @NonNull String algorithm;
    public @NonNull String algorithmSigning;
    public int keysize;
    public boolean preShared;

    public TLSAlgorithm(@NonNull String keystoreAlias, @NonNull String algorithm, @NonNull String algorithmSigning,
            int keysize, boolean preShared) {
        this.keystoreAlias = keystoreAlias;
        this.algorithm = algorithm;
        this.algorithmSigning = algorithmSigning;
        this.keysize = keysize;

    }

    public static TLSAlgorithm fromString(String s) {
        String parts[] = s.split("\\:");
        if (parts.length != 4) {
            return null;
        }
        int keySize = Integer.valueOf(parts[3]);
        return new TLSAlgorithm(parts[0], parts[1], parts[2], keySize, false);
    }

    @Override
    public String toString() {
        return keystoreAlias + ":" + algorithm + ":" + algorithmSigning + ":" + String.valueOf(keysize);
    }
}
