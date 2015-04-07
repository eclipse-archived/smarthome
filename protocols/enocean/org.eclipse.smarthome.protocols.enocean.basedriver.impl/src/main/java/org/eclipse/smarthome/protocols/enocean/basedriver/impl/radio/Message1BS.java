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

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;

/**
 * Message1BS: Prototype of a 1BS (EnOcean 1 Byte Communication) telegram.
 * 
 * EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf, page 10.
 * 
 * RORG - 1 byte - 0xD5
 * 
 * Data DB_0 - 1 byte
 * 
 * Sender ID - 4 bytes
 * 
 * Status - 1 byte
 * 
 * EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf, page 24.
 * 
 * D5: 1BS Telegram
 * 
 * D5-00: Contacts and Switches
 * 
 * RORG D5 1BS Telegram
 * 
 * FUNC 00 Contacts and Switches
 * 
 * TYPE 01 Single Input Contact
 * 
 * Offset | Size | Bitrange | Data | ShortCut | Description | Valid Range
 * 
 * 4 | 1 | DB0.3 | Learn Button | LRN | ... | Enum: 0: pressed 1: not pressed
 * 
 * 7 | 1 | DB0.0 | Contact | CO | ... | Enum: 0: open 1: closed
 */
public class Message1BS extends Message {

    /**
     * TAG
     */
    public static final String TAG = "Message1BS";

    /**
     * @param data
     *            , i.e the data, and the optional data parts of an EnOcean
     *            packet/telegram (see, section "1.6.1 Packet description", page
     *            13 of EnOceanSerialProtocol3.pdf). E.g. for a (full) 1BS
     *            telegram 55000707017ad500008a92390001ffffffff3000eb, then data
     *            is d500008a92390001ffffffff3000
     */
    public Message1BS(byte[] data) {
        super(data);

        Logger.d(TAG, "getPayloadBytes(): " + getPayloadBytes());
        int i = 0;
        while (i < getPayloadBytes().length) {
            Logger.d(TAG, "getPayloadBytes()[" + i + "]: " + getPayloadBytes()[i]);
            i = i + 1;
        }
    }

    public boolean isTeachin() {
        return (((getPayloadBytes()[0] & 0x08)) == 0);
    }

    /**
     * @return true if the message teach-in embeds profile & manufacturer info,
     *         false otherwise.
     */
    public boolean hasTeachInInfo() {
        // This is always false with 1BS message.
        return false;
    }

    /**
     * @return the FUNC in the case of a teach-in message with information.
     *         Return -1 if non-relevant.
     */
    public int teachInFunc() {
        // This is non-relevant with 1BS message.
        return -1;
    }

    /**
     * @return the TYPE in the case of a teach-in message with information.
     *         Return -1 if non-relevant.
     */
    public int teachInType() {
        // This is non-relevant with 1BS message.
        return -1;
    }

    /**
     * @return the MANUF in the case of a teach-in message with information.
     *         Return -1 if non-relevant.
     */
    public int teachInManuf() {
        // This is non-relevant with 1BS message.
        return -1;
    }
}
