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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.TooManyListenersException;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.esp.EspPacket;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.EnOceanHostImplException;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;
import org.osgi.framework.BundleContext;

/**
 * EnOceanHostSerialImpl.
 */
public class EnOceanHostSerialImpl extends EnOceanHostImpl implements SerialPortEventListener {

    /** TAG */
    public static final String TAG = EnOceanHostSerialImpl.class.getName();

    private static final int ENOCEAN_DEFAULT_TTY_SPEED = 57600;

    private Object syncObject;
    private RXTXPort serialPort;

    /**
     * @param path
     * @param bc
     */
    public EnOceanHostSerialImpl(String path, BundleContext bc) {
        super(path, bc);
        Logger.d(TAG, "EnOceanHostSerialImpl.EnOceanHostSerialImpl(String path: " + path + ", BundleContext bc: " + bc
                + ")");
        this.syncObject = new Object();
    }

    public void startup() throws EnOceanHostImplException {
        Logger.d(TAG, "EnOceanHostSerialImpl.startup()");
        this.isRunning = true;
        try {
            openSerialPort(donglePath, ENOCEAN_DEFAULT_TTY_SPEED);
        } catch (Exception exception) {
            throw new EnOceanHostImplException("Could not open serial port: " + exception.getMessage());
        }
        this.start();
    }

    public void run() {
        while (this.isRunning) {
            try {
                synchronized (this.syncObject) {
                    if (this.inputStream.available() == 0)
                        this.syncObject.wait();
                }
                if (!this.isRunning) {
                    return;
                }
                Logger.d(TAG, "EnOceanHostSerialImpl.run(), try to read from this.serialPort.getInputStream()...");
                int _byte = inputStream.read();
                Logger.d(TAG, "EnOceanHostSerialImpl.run(), just read _byte: " + _byte);
                if (_byte == -1) {
                    throw new IOException("buffer end was reached");
                }
                Logger.d(TAG, "read a byte: " + Utils.bytesToHexString(new byte[] { (byte) _byte }));
                if (_byte == ENOCEAN_ESP_FRAME_START) {
                    EspPacket packet = readPacket();
                    if (packet.getPacketType() == EspPacket.TYPE_RADIO) {
                        dispatchToListeners(packet.getFullData());
                    }
                }
            } catch (IOException ioexception) {
                Logger.e(TAG, "Error while reading input packet: " + ioexception.getMessage());
                ioexception.printStackTrace();
            } catch (InterruptedException interruptedexception) {
                Logger.e(TAG, "Error while reading input packet: " + interruptedexception.getMessage());
                interruptedexception.printStackTrace();
            } finally {
                try {
                    inputStream.skip(inputStream.available());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
	 * 
	 */
    public void close() {
        this.isRunning = false;
        synchronized (this.syncObject) {
            this.syncObject.notify();
        }
        if (this.outputStream != null)
            try {
                this.outputStream.close();
            } catch (IOException ioexception) {
                Logger.w(TAG, "Error while closing output stream.");
            }
        if (this.inputStream != null)
            try {
                this.inputStream.close();
            } catch (IOException ioexception1) {
                Logger.w(TAG, "Error while closing input stream.");
            }
        if (this.serialPort != null)
            this.serialPort.close();
    }

    public void send(byte[] data) {
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            Logger.e(TAG, "an exception occured while writing to stream '" + donglePath + "' : " + e.getMessage());
        }

    }

    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == 1) {
            synchronized (this.syncObject) {
                this.syncObject.notify();
            }
        }
    }

    private void openSerialPort(String s, int i) throws IOException, UnsupportedCommOperationException,
            TooManyListenersException, PortInUseException, NoSuchPortException {
        this.serialPort = new RXTXPort(s);
        this.serialPort.setSerialPortParams(i, 8, 1, 0);
        this.serialPort.setFlowControlMode(0);
        this.serialPort.notifyOnDataAvailable(true);
        this.serialPort.addEventListener(this);
        this.inputStream = this.serialPort.getInputStream();
        this.outputStream = this.serialPort.getOutputStream();
    }

}
