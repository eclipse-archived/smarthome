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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.esp.EspPacket;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.EnOceanHostImplException;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * EnOceanHostImpl.
 */
public class EnOceanHostImpl extends Thread implements EnOceanHost {

    /** TAG */
    protected static final String TAG = "EnOceanHostImpl";
    /** ENOCEAN_ESP_FRAME_START */
    protected static final byte ENOCEAN_ESP_FRAME_START = 0x55;
    /** donglePath */
    protected String donglePath;
    /** inputStream */
    protected InputStream inputStream;
    /** outputStream */
    protected OutputStream outputStream;

    private static final int MAX_ALLOCATED_CHIP_ID = 127;
    private ArrayList listeners;
    private int baseId;

    /** isRunning */
    protected boolean isRunning;
    private ChipPIDMapping chipIdPidMap;
    private int repeaterLevel = -1;
    private int baseID = -1;

    /**
     * @param path
     * @param bc
     */
    public EnOceanHostImpl(String path, BundleContext bc) {
        this.isRunning = false;
        this.listeners = new ArrayList();
        this.donglePath = path;
        try {
            this.chipIdPidMap = new ChipPIDMapping(bc);
        } catch (Exception e) {
            Logger.w(TAG, "could not allocate Config Admin chipId/servicePID mapping : " + e.getMessage());
        }
    }

    /**
     * @throws EnOceanHostImplException
     */
    public void startup() throws EnOceanHostImplException {
        //
    }

    /**
     * @param servicePID
     * @throws ArrayIndexOutOfBoundsException
     * @throws IOException
     */
    public void allocChipID(String servicePID) throws ArrayIndexOutOfBoundsException, IOException {
        int chipId = getChipId(servicePID);
        if (chipId == -1) {
            int size = chipIdPidMap.size();
            if (size < MAX_ALLOCATED_CHIP_ID) {
                int newChipId = baseId + size; // FIXME: VERY basic method
                chipIdPidMap.put(servicePID, String.valueOf(newChipId));
            } else {
                throw new ArrayIndexOutOfBoundsException("No more CHIP_ID can be allocated.");
            }
        }
    }

    /**
     * Implementation-specific method to add a remote packet listener.
     *
     * @param packetListener
     *            an object implementing the {@link EnOceanPacketListener} interface.
     */
    public void addPacketListener(EnOceanPacketListener packetListener) {
        if (!listeners.contains(packetListener)) {
            listeners.add(packetListener);
        }
    }

    public void reset() throws EnOceanException {
        // TODO Auto-generated method stub
    }

    public String appVersion() throws EnOceanException {
        // TODO Auto-generated method stub
        return null;
    }

    public String apiVersion() throws EnOceanException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getBaseID() throws EnOceanException {
        return baseID;
    }

    public void setBaseID(int baseID) throws EnOceanException {
        this.baseId = baseID;
    }

    public int getRepeaterLevel() throws EnOceanException {
        return repeaterLevel;
    }

    public void setRepeaterLevel(int level) throws EnOceanException {
        repeaterLevel = level;
    }

    public int getChipId(String servicePID) {
        String chipId = chipIdPidMap.get(servicePID);
        if (chipId == null) {
            return -1;
        } else {
            return Integer.parseInt(chipIdPidMap.get(servicePID));
        }
    }

    /**
     * @param data
     */
    protected void dispatchToListeners(byte[] data) {
        for (int i = 0; i < listeners.size(); i++) {
            EnOceanPacketListener listener = (EnOceanPacketListener) listeners.get(i);
            listener.radioPacketReceived(data);
        }
    }

    /**
     * Low-level ESP3 reader implementation. Reads the header, deducts the
     * paylsoad size, checks for errors, and sends back the read packet to the
     * caller.
     *
     * @return the complete byte[] ESP packet
     * @throws IOException
     */
    protected EspPacket readPacket() throws IOException {
        byte[] header = new byte[4];
        for (int i = 0; i < 4; i++) {
            header[i] = (byte) inputStream.read();
        }
        Logger.d(TAG, "read header: " + Utils.bytesToHexString(header));
        // Check the CRC
        int headerCrc = inputStream.read();
        if (headerCrc == -1) {
            throw new IOException("could not read entire packet");
        }
        Logger.d(TAG, "header_crc = 0x" + Utils.bytesToHexString(new byte[] { (byte) headerCrc }));
        Logger.d(TAG, "h_comp_crc = 0x" + Utils.bytesToHexString(new byte[] { Utils.crc8(header) }));
        if ((byte) headerCrc != Utils.crc8(header)) {
            throw new IOException("header was malformed or corrupt");
        }
        // Read the payload using header info
        int payloadLength = ((header[0] << 8) | header[1]) + header[2];
        byte[] payload = new byte[payloadLength];
        for (int i = 0; i < payloadLength; i++) {
            payload[i] = (byte) inputStream.read();
        }
        Logger.d(TAG, "read payload: " + Utils.bytesToHexString(payload));
        // Check payload CRC
        int payloadCrc = inputStream.read();
        if (payloadCrc == -1) {
            throw new IOException("could not read entire packet");
        }
        Logger.d(TAG, "orig_crc     = 0x" + Utils.bytesToHexString(new byte[] { (byte) payloadCrc }));
        Logger.d(TAG, "computed_crc = 0x" + Utils.bytesToHexString(new byte[] { Utils.crc8(payload) }));
        if ((byte) payloadCrc != Utils.crc8(payload)) {
            String expcetionmessage = "payload was malformed or corrupt";
            Logger.e(TAG, expcetionmessage);
            throw new IOException(expcetionmessage);
        }
        payload = Utils.byteConcat(payload, (byte) payloadCrc);
        // Add the sync byte to the header
        header = Utils.byteConcat(EspPacket.SYNC_BYTE, header);
        header = Utils.byteConcat(header, (byte) headerCrc);

        // The following line prints, for example for a 1BS telegram,
        // [DEBUG-EnOceanHostImpl] Received EnOcean packet. Frame data:
        // 55000707017ad500008a92390001ffffffff3000eb
        Logger.d(TAG,
                "Received EnOcean packet. Frame data: " + Utils.bytesToHexString(Utils.byteConcat(header, payload)));
        return new EspPacket(header, payload);
    }

    /**
     * @param data
     */
    public void send(byte[] data) {
        //
    }

    class ChipPIDMapping {

        private Configuration config;
        private Dictionary mappings;

        public ChipPIDMapping(BundleContext bc) throws UnknownServiceException, IOException {
            ServiceReference ref = bc.getServiceReference(ConfigurationAdmin.class.getName());
            if (ref != null) {
                ConfigurationAdmin configAdmin = (ConfigurationAdmin) bc.getService(ref);
                config = configAdmin.getConfiguration(EnOceanBaseDriver.CONFIG_EXPORTED_PID_TABLE);
                mappings = config.getProperties();
                if (mappings == null) {
                    mappings = new Properties();
                    config.update(mappings);
                }
            } else {
                throw new UnknownServiceException("ConfigAdmin service was not found !");
            }

        }

        public void put(String servicePID, String chipId) {
            mappings.put(servicePID, chipId);
            try {
                config.update(mappings);
            } catch (IOException e) {
                Logger.w(TAG, "chip ID '" + chipId + "' could not be saved in ConfigAdmin with service PID '"
                        + servicePID + "'");
            }
        }

        public int size() {
            return mappings.size();
        }

        public String get(String servicePID) {
            return (String) mappings.get(servicePID);
        }
    }
}
