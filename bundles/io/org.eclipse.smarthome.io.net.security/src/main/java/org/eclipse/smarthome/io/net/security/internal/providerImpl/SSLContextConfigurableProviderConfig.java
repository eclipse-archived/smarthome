/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal.providerImpl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents configuration of one context.
 *
 * @author David Graeff - Initial contribution
 */
public class SSLContextConfigurableProviderConfig {
    /** PKCS12 is the strongest of the supported keystore formats and default in Java9+ */
    public String keystoreFormat = "PKCS12";
    public transient ByteBuffer keystorePassword = randomPassword(15);
    public transient ByteBuffer privateKeyPassword = randomPassword(15);
    public @Nullable transient String keystoreHash = null;

    public static ByteBuffer randomPassword(int count) {
        return ByteBuffer.wrap(RandomStringUtils.random(count, 0, 0, false, false, null, new SecureRandom())
                .getBytes(StandardCharsets.UTF_8));
    }
}