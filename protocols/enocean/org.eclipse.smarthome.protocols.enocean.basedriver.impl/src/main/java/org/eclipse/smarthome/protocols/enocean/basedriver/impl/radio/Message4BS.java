/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio;

/**
 * Message4BS: Prototype of a 4BS telegram
 * 
 * Teach-in procedure:
 * 
 * - if DB0.3 is 0, then it's a teach-in telegram.
 * 
 * - if DB0.7 is also 0, no manufacturer info.
 * 
 * - if DB0.7 is 1, manufacturer info is present.
 */
public class Message4BS extends Message {

    /**
     * @param data
     *            , i.e the data, and the optional data parts of an EnOcean
     *            packet/telegram (see, section "1.6.1 Packet description", page
     *            13 of EnOceanSerialProtocol3.pdf). E.g. for a (full) 1BS
     *            telegram 55000707017ad500008a92390001ffffffff3000eb, then data
     *            is d500008a92390001ffffffff3000
     */
    public Message4BS(byte[] data) {
        super(data);
    }

    public boolean isTeachin() {
        return (((getPayloadBytes()[3] & 0x08)) == 0);
    }

    /**
     * @return true if the message teach-in embeds profile & manufacturer info.
     */
    public boolean hasTeachInInfo() {
        return (getPayloadBytes()[3] & 0x80) != 0;
    }

    /**
     * @return the FUNC in the case of a teach-in message with information.
     */
    public int teachInFunc() {
        return (getPayloadBytes()[0] >> 2) & 0xff;
    }

    /**
     * @return the TYPE in the case of a teach-in message with information.
     */
    public int teachInType() {
        byte b0 = getPayloadBytes()[0];
        byte b1 = getPayloadBytes()[1];
        return (((b0 & 0x03) << 5) & 0xff) | ((((b1 >> 3)) & 0xff));
    }

    /**
     * @return the MANUF in the case of a teach-in message with information.
     */
    public int teachInManuf() {
        byte b0 = (byte) ((getPayloadBytes()[1]) & 0x07);
        byte b1 = getPayloadBytes()[2];
        return ((b0 & 0xff) << 8) + (b1 & 0xff);
    }
}
