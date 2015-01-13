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
 * MessageRPS: Prototype of a RPS telegram
 */
public class MessageRPS extends Message {

    /**
     * @param data
     */
    public MessageRPS(byte[] data) {
        super(data);
    }

    public boolean isTeachin() {
        return true;
    }

    /**
     * @return true if the message teach-in embeds profile & manufacturer info.
     */
    public boolean hasTeachInInfo() {
        return true;
    }

    /**
     * @return a fake FUNC
     */
    public int teachInFunc() {
        return -1;
    }

    /**
     * @return a fake TYPE
     */
    public int teachInType() {
        return -1;
    }

    /**
     * @return a fake MANUF
     */
    public int teachInManuf() {
        return -1;
    }
}
