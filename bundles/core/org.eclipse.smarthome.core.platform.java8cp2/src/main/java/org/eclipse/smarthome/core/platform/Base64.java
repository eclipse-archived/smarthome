/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.platform;

public class Base64 {

    public static Encoder getEncoder() {
        return new Encoder();
    }

    public static Decoder getDecoder() {
        return new Decoder();
    }

    public static class Encoder {

        private java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

        public String encode(final byte[] base64) {
            return encoder.encodeToString(base64);
        }
    }

    public static class Decoder {

        private java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();

        public byte[] decode(final String str) {
            return decoder.decode(str);
        }
    }
}
