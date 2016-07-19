/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.fields;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Tim Buckley
 */
public class StringField extends Field<String> {

    public static final Charset CHARSET = StandardCharsets.US_ASCII;

    private Charset charset;

    public StringField() {
        charset = StandardCharsets.US_ASCII;
    }

    public StringField(int length) {
        super(length);

        charset = StandardCharsets.US_ASCII;
    }

    public StringField(int length, Charset charset) {
        super(length);

        this.charset = charset;
    }

    @Override
    public int defaultLength() {
        return 3;
    }

    @Override
    public String value(ByteBuffer bytes) {
        byte[] buf = new byte[length];
        bytes.get(buf);

        ByteBuffer field = ByteBuffer.wrap(buf);

        String ret = charset.decode(field).toString();
        ret = ret.replace("\0", "");

        return ret;
    }

    @Override
    public ByteBuffer bytesInternal(String value) {
        return CHARSET.encode(value);
    }

    public StringField ascii() {
        charset = StandardCharsets.US_ASCII;
        return this;
    }

    public StringField utf8() {
        charset = StandardCharsets.UTF_8;
        return this;
    }

}
