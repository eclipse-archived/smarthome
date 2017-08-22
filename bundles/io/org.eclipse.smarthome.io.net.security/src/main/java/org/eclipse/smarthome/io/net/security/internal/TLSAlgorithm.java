/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.math.BigDecimal;

import org.eclipse.smarthome.io.net.security.internal.config.FieldRequired;

/**
 * Defines all parameters for an encryption algorithm
 *
 * @author David Graeff - Initial contribution
 */
public class TLSAlgorithm {
    public @FieldRequired String keystoreAlias;
    public @FieldRequired String algorithm;
    public @FieldRequired String algorithmSigning;
    public @FieldRequired BigDecimal keysize;

    public TLSAlgorithm(String keystoreAlias, String algorithm, String algorithmSigning, BigDecimal keysize) {
        this.keystoreAlias = keystoreAlias;
        this.algorithm = algorithm;
        this.algorithmSigning = algorithmSigning;
        this.keysize = keysize;

    }

    public TLSAlgorithm() {

    }
}
