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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;

/**
 * Message.
 */
public abstract class Message implements EnOceanMessage {

    /** TAG */
    public static final String TAG = Message.class.getName();

    /** MESSAGE_4BS */
    public static final byte MESSAGE_4BS = (byte) 0xA5;
    /** MESSAGE_RPS */
    public static final byte MESSAGE_RPS = (byte) 0xF6;
    /** MESSAGE_1BS */
    public static final byte MESSAGE_1BS = (byte) 0xD5;
    /** MESSAGE_VLD */
    public static final byte MESSAGE_VLD = (byte) 0xD2;
    /** MESSAGE_SYS_EX */
    public static final byte MESSAGE_SYS_EX = (byte) 0xC5;
    /**
     * MESSAGE_UTE, see EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf, page
     * 167, 3.6) UTE - Universla Uni- and Bidirectional Teach-in.
     */
    public static final byte MESSAGE_UTE = (byte) 0xD4;

    private byte rorg;
    private byte[] data;
    private byte[] senderId;
    private byte status;
    private byte subTelNum;
    private byte[] destinationId;
    private byte dbm;
    private byte securityLevel;
    private byte func;
    private byte type;

    private byte[] messageBytes;

    /**
     * 
     */
    public Message() {

    }

    /**
     * @param data
     *            , i.e the data, and the optional data parts of an EnOcean
     *            packet/telegram (see, section "1.6.1 Packet description", page
     *            13 of EnOceanSerialProtocol3.pdf). E.g. for a (full) 1BS
     *            telegram 55000707017ad500008a92390001ffffffff3000eb, then data
     *            is d500008a92390001ffffffff3000
     */
    public Message(byte[] data) {
        Logger.d(TAG, "////////Message(byte[] data: " + data);
        Logger.d(TAG, "////////data.length: " + data.length);

        int i = 0;
        while (i < data.length) {
            Logger.d(TAG, "////data[" + i + "]: " + data[i]);
            i = i + 1;
        }

        this.messageBytes = data;
        setRORG(data[0]);
        setPayloadBytes(Utils.byteRange(data, 1, data.length - 6 - 7));
        setSenderId(Utils.byteRange(data, data.length - 5 - 7, 4));
        setStatus(data[data.length - 1 - 7]);
        setSubTelNum(data[data.length - 7]);
        setDestinationId(Utils.byteRange(data, data.length - 6, 4));
        setDbm(data[data.length - 2]);
        setSecurityLevel(data[data.length - 1]);
    }

    private void setPayloadBytes(byte[] byteRange) {
        this.data = byteRange;
    }

    public String toString() {
        byte[] out = Utils.byteConcat(this.rorg, data);
        out = Utils.byteConcat(out, senderId);
        out = Utils.byteConcat(out, status);
        return Utils.bytesToHexString(out);
    }

    /**
     * The message's RadioTelegram Type
     */
    public int getRorg() {
        return (this.rorg & 0xff);
    }

    /**
     * @param rorg
     */
    public void setRORG(int rorg) {
        this.rorg = (byte) (rorg & 0xff);
    }

    public byte[] getBytes() {
        return messageBytes;
    }

    public int getSenderId() {
        return Utils.bytes2intLE(senderId, 0, 4);
    }

    /**
     * Sender ID of the message
     *
     * @param senderId
     */
    public void setSenderId(byte[] senderId) {
        this.senderId = senderId;
    }

    /**
     * EnOceanMessage status byte. bit 7 : if set, use crc8 else use checksum
     * bits 5-6 : reserved bits 0-4 : repeater count
     */
    public int getStatus() {
        return (status & 0xff);
    }

    /**
     * @param status
     */
    public void setStatus(int status) {
        this.status = (byte) (status & 0xff);
    }

    public int getSubTelNum() {
        return subTelNum;
    }

    /**
     * @param subTelNum
     */
    public void setSubTelNum(byte subTelNum) {
        this.subTelNum = subTelNum;
    }

    public int getDestinationId() {
        return Utils.bytes2intLE(destinationId, 0, 4);
    }

    /**
     * @param destinationId
     */
    public void setDestinationId(byte[] destinationId) {
        this.destinationId = destinationId;
    }

    public int getDbm() {
        return dbm;
    }

    /**
     * @param dbm
     */
    public void setDbm(byte dbm) {
        this.dbm = dbm;
    }

    public int getSecurityLevelFormat() {
        return securityLevel;
    }

    /**
     * @param securityLevel
     */
    public void setSecurityLevel(byte securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * @param func
     */
    public void setFunc(int func) {
        this.func = (byte) (func & 0xff);
    }

    public int getFunc() {
        return (this.func & 0xff);
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = (byte) (type & 0xff);
    }

    public int getType() {
        return (this.type & 0xff);
    }

    public byte[] getPayloadBytes() {
        return data;
    }

    /**
     * @return telegrams
     */
    public List getTelegrams() {
        List list = new ArrayList();
        list.add(getBytes());
        return list;
    }

    /**
     * @return isTeachin
     */
    public abstract boolean isTeachin();

    /**
     * @return hasTeachInInfo
     */
    public abstract boolean hasTeachInInfo();

    /**
     * @return teachInFunc
     */
    public abstract int teachInFunc();

    /**
     * @return teachInType
     */
    public abstract int teachInType();

    /**
     * @return teachInManuf
     */
    public abstract int teachInManuf();
}
