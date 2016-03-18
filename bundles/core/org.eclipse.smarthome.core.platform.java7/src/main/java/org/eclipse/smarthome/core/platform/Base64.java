/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.platform;

import javax.xml.bind.DatatypeConverter;

public class Base64 {

    public static Encoder getEncoder() {
        return new Encoder();
    }

    public static Decoder getDecoder() {
        return new Decoder();
    }

    public static class Encoder {

        public String encode(final byte[] base64) {
            return DatatypeConverter.printBase64Binary(base64);
        }
    }

    public static class Decoder {

        public byte[] decode(final String str) {
            return DatatypeConverter.parseBase64Binary(str);
        }
    }
}
