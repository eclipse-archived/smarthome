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

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.esp.EspPacket;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.EnOceanHostImplException;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;
import org.osgi.framework.BundleContext;

/**
 * EnOceanHostTestImpl.
 */
public class EnOceanHostTestImpl extends EnOceanHostImpl {

    private ByteArrayOutputStream byteStream;
    private CustomInputStream duplicatedStream;

    /**
     * @param path
     * @param bc
     */
    public EnOceanHostTestImpl(String path, BundleContext bc) {
        super(path, bc);
    }

    public void startup() throws EnOceanHostImplException {
        this.isRunning = true;
        this.inputStream = new CustomInputStream(new byte[] {});
        this.outputStream = new ByteArrayOutputStream();
        this.duplicatedStream = (CustomInputStream) inputStream;
        this.byteStream = (ByteArrayOutputStream) outputStream;
        this.start();
    }

    public void run() {
        while (this.isRunning) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                if (byteStream.size() == 0) {
                    continue;
                }
                byte[] data = byteStream.toByteArray();
                if (data[0] != ENOCEAN_ESP_FRAME_START)
                    continue;
                duplicatedStream.replace(data);
                duplicatedStream.read();
                byteStream.reset();
                Logger.d(TAG, "read bytes: " + Utils.bytesToHexString(data));
                if (data[0] == ENOCEAN_ESP_FRAME_START) {
                    EspPacket packet = readPacket();
                    if (packet.getPacketType() == EspPacket.TYPE_RADIO) {
                        dispatchToListeners(packet.getFullData());
                    }
                }
            } catch (IOException ioexception) {
                Logger.e(TAG, "Error while reading input packet: " + ioexception.getMessage());
            }
        }
    }

    /**
	 * 
	 */
    public void close() {
        this.isRunning = false;
        if (this.outputStream != null)
            try {
                this.outputStream.close();
            } catch (IOException ioexception) {
                Logger.w(TAG, "Error while closing output stream.");
            }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException ioexception1) {
                Logger.w(TAG, "Error while closing input stream.");
            }
        }
    }

    public void send(byte[] data) {
        duplicatedStream.replace(data);
    }

    class CustomInputStream extends ByteArrayInputStream {

        public CustomInputStream(byte[] var0) {
            super(var0);
        }

        public void replace(byte[] data) {
            this.buf = (byte[]) data.clone();
            this.mark = 0;
            this.pos = 0;
            this.count = data.length;
        }

    }

}
