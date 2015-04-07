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

import java.util.Map;
import java.util.Properties;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.conf.ConfigurationFileManager;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.conf.RorgFuncTypeFriendlynameDescription;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.MessageSYS_EX;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Constants;
import org.osgi.service.enocean.EnOceanDevice;
import org.osgi.service.enocean.EnOceanException;
import org.osgi.service.enocean.EnOceanHandler;
import org.osgi.service.enocean.EnOceanMessage;
import org.osgi.service.enocean.EnOceanRPC;
import org.osgi.service.enocean.descriptions.EnOceanMessageDescription;
import org.osgi.service.enocean.descriptions.EnOceanMessageDescriptionSet;

/**
 * EnOceanDeviceImpl.
 */
public class EnOceanDeviceImpl implements EnOceanDevice {

    /** TAG */
    public static final String TAG = EnOceanDeviceImpl.class.getName();

    private BundleContext bc;
    private ServiceRegistration sReg;

    private Properties props;
    private EnOceanMessage lastMessage;
    private EnOceanBaseDriver driver;
    private int chip_id;

    private int rollingCode = -1;
    private byte[] encryptionKey = null;
    private String name = null;
    private String profileName = null;
    private int securityLevelFormat = -1;

    /**
     * An {@link EnOceanDeviceImpl} creation is directly related to its
     * registration within the framework. Such a Device should only be
     * registered after a proper teach-in procedure, so that the RORG, FUNC and
     * TYPE are already known.
     * 
     * @param bc
     * @param driver
     * @param uid
     * @param rorg
     * @param func
     *            the int value or -1 if no value.
     * @param type
     *            the int value or -1 if no value.
     * @param manuf
     *            the int value or -1 if no value.
     */
    public EnOceanDeviceImpl(BundleContext bc, EnOceanBaseDriver driver, int uid, int rorg, int func, int type,
            int manuf) {
        Logger.d(TAG, "EnOceanDeviceImpl(bc: " + bc + ", driver: " + driver + ", uid: " + uid + ", rorg: " + rorg
                + ", func: " + func + ", type: " + type + ", manuf: " + manuf);

        this.bc = bc;
        this.driver = driver;
        this.chip_id = uid;
        props = new Properties();
        props.put(Constants.DEVICE_CATEGORY, new String[] { EnOceanDevice.DEVICE_CATEGORY });
        props.put(EnOceanDevice.CHIP_ID, String.valueOf(this.chip_id));
        props.put(EnOceanDevice.RORG, String.valueOf(rorg));
        String friendlyName = null;
        String description = null;

        // Check if something is defined in the config file
        RorgFuncTypeFriendlynameDescription rFTFD = ConfigurationFileManager
                .getRorgFuncTypeAndFriendlynameFromConfigFile("0x"
                        + Utils.bytesToHexString(Utils.intTo4Bytes(this.chip_id)));

        int correctedFunc = func;
        int correctedType = type;
        if (rFTFD != null) {
            // The given rorg, and the rFTF.getRorg() may be checked.
            Logger.d(TAG, "The device with uid: " + uid + " appears in the config file; rFTFD: " + rFTFD);
            friendlyName = rFTFD.getFriendlyname();
            description = rFTFD.getDescription();
            correctedFunc = Integer.parseInt(rFTFD.getFunc());
            props.put(EnOceanDevice.FUNC, String.valueOf(correctedFunc));
            correctedType = Integer.parseInt(rFTFD.getType());
            props.put(EnOceanDevice.TYPE, String.valueOf(correctedType));
        } else {
            Logger.d(TAG, "The device with uid: " + uid + " does NOT appear in the config file; rFTFD: " + rFTFD);
        }

        if ("165".equals(String.valueOf(rorg))) {
            // hex 0xa5 == int 165.
            if ("2".equals(String.valueOf(correctedFunc))) {
                if ("5".equals(String.valueOf(correctedType))) {
                    Logger.d(TAG, "This is an A5-02-05 device.");
                    if (friendlyName == null) {
                        // default friendlyName
                        friendlyName = "A5-02-05";
                    }
                    if (description == null) {
                        String descriptionTemp = getEnOceanMessageDescription(rorg, correctedFunc, correctedType);
                        if (descriptionTemp != null) {
                            description = descriptionTemp;
                        } else {
                            // default description
                            description = "Temperature Sensor Range 0°C to +40°C";
                        }
                    }
                } else {
                    Logger.d(TAG, "This is an A5-02-yz device.");
                    if (friendlyName == null) {
                        friendlyName = "A5-02-yz";
                    }
                    if (description == null) {
                        description = "Not handled";
                    }
                }
            } else {
                Logger.d(TAG, "This is an A5-wx-yz device.");
                if (friendlyName == null) {
                    friendlyName = "A5-wx-yz";
                }
                if (description == null) {
                    description = "Not handled";
                }
            }
        } else if ("246".equals(String.valueOf(rorg))) {
            // hex 0xf6 == int 246.
            Logger.d(
                    TAG,
                    "This is a F6-wx-yz device. FUNC, and TYPE are NOT sent by F6-wx-yz device. The system then assumes that the device is an F6-02-01.");
            if (friendlyName == null) {
                // default friendlyName
                friendlyName = "F6-02-01";
            }
            if (description == null) {
                String descriptionTemp = getEnOceanMessageDescription(rorg, correctedFunc, correctedType);
                if (descriptionTemp != null) {
                    description = descriptionTemp;
                } else {
                    // default description
                    description = "Light and Blind Control - Application Style 1";
                }
            }
        } else if ("213".equals(String.valueOf(rorg))) {
            // hex 0xd5 == int 213.
            Logger.d(
                    TAG,
                    "This is a D5-wx-yz device. FUNC, and TYPE are NOT sent by D5-wx-yz device. The system then assumes that the device is a D5-00-01.");
            if (friendlyName == null) {
                friendlyName = "D5-00-01";
            }
            if (description == null) {
                String descriptionTemp = getEnOceanMessageDescription(rorg, correctedFunc, correctedType);
                if (descriptionTemp != null) {
                    description = descriptionTemp;
                } else {
                    // default description
                    description = "Single Input Contact";
                }
            }
        } else {
            Logger.d(TAG, "This is a NOT HANDLED device (rorg: " + String.valueOf(rorg)
                    + ", i.e. neither an A5-02-05 device, nor a F6-wx-yz device, nor a D5-wx-yz device. "
                    + "RORG is NOT equal to a5, nor f6,nor d5 (0xa5 is equal to int 165; 0xf6 -> 246, 0xd5 -> 213).");
        }

        // If the EEP is not handle, then friendlyName is null, here.
        // So Properties.put(..., ...) will throw a NullPointerException.
        props.put("DEVICE_FRIENDLY_NAME", friendlyName);
        // If the EEP is not handle, then description is null, here.
        // So Properties.put(..., ...) will throw a NullPointerException.
        props.put(Constants.DEVICE_DESCRIPTION, description);

        props.put(Constants.DEVICE_SERIAL, String.valueOf(this.chip_id));
        props.put("service.pid", String.valueOf(this.chip_id));
        sReg = this.bc.registerService(EnOceanDevice.class.getName(), this, props);
        Logger.d(TAG, "registering EnOceanDevice : " + Utils.bytesToHexString(Utils.intTo4Bytes(this.chip_id)));
        /* Initializations */
        lastMessage = null;
    }

    private String getEnOceanMessageDescription(int rorg, int func, int type) {
        try {
            ServiceReference[] srs = bc.getAllServiceReferences(EnOceanMessageDescriptionSet.class.getName(), null);
            Logger.d(TAG, "srs: " + srs);
            if (srs == null) {
                Logger.d(TAG, "There is NO service registered with the following class name: "
                        + EnOceanMessageDescriptionSet.class.getName());
            } else {
                Logger.d(TAG, "srs.length: " + srs.length);

                int i = 0;
                while (i < srs.length) {
                    ServiceReference sRef = srs[i];
                    Logger.d(TAG, "sRef: " + sRef);
                    EnOceanMessageDescriptionSet eomds = (EnOceanMessageDescriptionSet) bc.getService(sRef);
                    Logger.d(TAG, "eomds: " + eomds);
                    EnOceanMessageDescription eomd = eomds.getMessageDescription(rorg, func, type, -1);
                    Logger.d(TAG, "eomd: " + eomd);
                    if (eomd != null) {
                        return eomd.getMessageDescription();
                    }
                    i = i + 1;
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return service properties
     */
    public Properties getServiceProperties() {
        return props;
    }

    /**
     * @param func
     * @param type
     * @param manuf
     */
    public void registerProfile(int func, int type, int manuf) {
        props.put(EnOceanDevice.FUNC, String.valueOf(func));
        props.put(EnOceanDevice.TYPE, String.valueOf(type));
        props.put(EnOceanDevice.MANUFACTURER, String.valueOf(manuf));
        sReg.setProperties(props);
    }

    public void setLearningMode(boolean learnMode) {
        // TODO Auto-generated method stub
    }

    public int getRollingCode() {
        return this.rollingCode;
    }

    public void setRollingCode(int rollingCode) {
        this.rollingCode = rollingCode;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] key) {
        this.encryptionKey = key;
    }

    public int[] getLearnedDevices() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param message
     * @param handler
     * @throws EnOceanException
     */
    public void send(EnOceanMessage message, EnOceanHandler handler) throws EnOceanException {
        // TODO Auto-generated method stub
    }

    /**
     * @return last message
     */
    public EnOceanMessage getLastMessage() {
        return lastMessage;
    }

    /**
     * @param msg
     */
    public void setLastMessage(EnOceanMessage msg) {
        lastMessage = msg;
    }

    public Map getRPCs() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * @param profileName
     */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public int getSecurityLevelFormat() {
        return securityLevelFormat;
    }

    /**
     * @param securityLevelFormat
     */
    public void setSecurityLevelFormat(int securityLevelFormat) {
        this.securityLevelFormat = securityLevelFormat;
    }

    public int getRorg() {
        return getIntProperty(EnOceanDevice.RORG);
    }

    /**
     * @param rorg
     */
    public void setRorg(int rorg) {
        props.put(EnOceanDevice.RORG, String.valueOf(rorg));
    }

    public int getFunc() {
        return getIntProperty(EnOceanDevice.FUNC);
    }

    public void setFunc(int func) {
        props.put(EnOceanDevice.FUNC, String.valueOf(func));
        sReg.setProperties(props);
    }

    public int getType() {
        return getIntProperty(EnOceanDevice.TYPE);
    }

    public void setType(int type) {
        props.put(EnOceanDevice.TYPE, String.valueOf(type));
        sReg.setProperties(props);
    }

    public int getManufacturer() {
        return getIntProperty(EnOceanDevice.MANUFACTURER);
    }

    /**
     * @param manuf
     */
    public void setManufacturer(int manuf) {
        props.put(EnOceanDevice.MANUFACTURER, String.valueOf(manuf));
        sReg.setProperties(props);
    }

    public int getChipId() {
        return chip_id;
    }

    /**
     * Safe function to get an int property
     * 
     * @param key
     * @return the int-converted property, or -1
     */
    private int getIntProperty(String key) {
        try {
            String s = (String) props.get(key);
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }

    public void invoke(EnOceanRPC rpc, EnOceanHandler handler) throws IllegalArgumentException {

        if ("HARDCODED_UTE_APPAIR".equals(rpc.getName())) {
            Logger.d(TAG, "HARDCODED_UTE_APPAIR");
            // 55 00 0D 07 01 FD D4 D2 01 08 00 3E FF 91 00 00 00 00 00 03 01 85
            // 64 14 FF 00 E9
            // 0x ----------- : 55 00 0D 07 01 FD D4 D2 01 08 00 3E FF 91
            // 00 00 00 00 00 03 01 85 64 14 FF 00 E9

            // unsigned int - : 85 00 13 07 01 253 212 210 01 08 00 62 255 145
            // 00 00 00 00 00 03 01 133 100 20 255 00 233

            byte[] fullVldTurnOnPacket = { 85, 00, 13, 07, 01, (byte) 253, (byte) 212, (byte) 210, 01, 8, 00, 62,
                    (byte) 255, (byte) 145, 00, 00, 00, 00, 00, 03, 01, (byte) 133, 100, 20, (byte) 255, 00, (byte) 233 };

            Logger.d(TAG, "Utils.bytesToHexString(fullVldTurnOnPacket): " + Utils.bytesToHexString(fullVldTurnOnPacket));
            Logger.d(TAG, "BEFORE: driver.send(fullVldTurnOnPacket)");
            driver.send(fullVldTurnOnPacket);
            Logger.d(TAG, "AFTER: driver.send(fullVldTurnOnPacket)");

        } else if ("HARDCODED_VLD_TURN_ON".equals(rpc.getName())) {
            Logger.d(TAG, "HARDCODED_VLD_TURN_ON");

            // <Telegram Timestamp="2014-11-04 14:19:05.396"
            // Direction="Incoming" Port="COM4" RORG="F6" Data="50" Status="30"
            // ID="0029219F" dBm="-54" DestinationID="FFFFFFFF"
            // SecurityLevel="0" SubtelegramCount="1" Tickcount="0">
            // <Packet Timestamp="2014-11-04 14:19:05.396" Direction="Incoming"
            // Port="COM4" Type="01" Data="F6 50 00 29 21 9F 30"
            // OptionalData="01 FF FF FF FF 36 00" />
            // </Telegram>

            // 0x55000707017af6500029219f3001ffffffff39009f
            // 0x ----------- : 55 00 07 07 01 7a f6 50 00 29 21 9f 30 01 ff
            // ff ff ff 39 00 9f
            // unsigned int - : 85 00 07 07 01 112 246 80 00 41 33 159 48 01 255
            // 255 255 255 57 00 159
            // byte[] fullTurnOnPacket = { 85, 00, 07, 07, 01, 112, (byte) 246,
            // 80, 00, 41, 33, (byte) 159, 48, 01, (byte) 255, (byte) 255,
            // (byte) 255, (byte) 255, 57, 00, (byte) 159 };

            // 0x ----------- : 55 00 07 07 01 7A F6 50 00 29 21 9F 30 01
            // FF FF FF FF 2D 00 9C
            // unsigned int - : 85 00 07 07 01 122 246 80 00 41 33 159 48 01
            // 255 255 255 255 45 00 156

            // let's replace destinationId FFFFFFFF by the plug id:
            // 0x : 01 85 64 14
            // uint: 01 133 100 20
            // this requires to change the crc...

            // byte[] switchFullTurnOnPacket = { 85, 00, 07, 07, 01, 122,
            // (byte) 246, 80, 00, 41, 33, (byte) 159, 48, 01, (byte) 255,
            // (byte) 255, (byte) 255, (byte) 255, 45, 00, (byte) 156 };
            //
            // Logger.d(TAG, "Utils.bytesToHexString(switchFullTurnOnPacket): "
            // + Utils.bytesToHexString(switchFullTurnOnPacket));
            // Logger.d(TAG, "BEFORE: driver.send(switchFullTurnOnPacket)");
            // driver.send(switchFullTurnOnPacket);
            // Logger.d(TAG, "AFTER: driver.send(switchFullTurnOnPacket)");

            // <Telegram Timestamp="2014-11-04 15:20:16.122"
            // Direction="Outgoing" Port="COM4" RORG="D2" Data="01 00 01"
            // Status="00" ID="00000000" dBm="0" DestinationID="01856414"
            // SecurityLevel="0" SubtelegramCount="3" Tickcount="0">
            // <Packet Timestamp="2014-11-04 15:20:16.122" Direction="Outgoing"
            // Port="COM4" Type="01" Data="D2 01 00 01 00 00 00 00 00"
            // OptionalData="03 01 85 64 14 FF 00" />
            // </Telegram>

            // 0x ----------- : 55 00 09 07 01 56 D2 01 00 01 00 00 00 00 00 03
            // 01 85 64 14 FF 00 64
            // unsigned int - : 85 00 09 07 01 86 210 01 00 01 00 00 00 00 00 03
            // 01 133 100 20 255 00 100

            byte[] fullVldTurnOnPacket = { 85, 00, 9, 07, 01, 86, (byte) 210, 01, 00, 01, 00, 00, 00, 00, 00, 03, 01,
                    (byte) 133, 100, 20, (byte) 255, 00, 100 };

            Logger.d(TAG, "Utils.bytesToHexString(fullVldTurnOnPacket): " + Utils.bytesToHexString(fullVldTurnOnPacket));
            Logger.d(TAG, "BEFORE: driver.send(fullVldTurnOnPacket)");
            driver.send(fullVldTurnOnPacket);
            Logger.d(TAG, "AFTER: driver.send(fullVldTurnOnPacket)");

        } else if ("HARDCODED_VLD_TURN_OFF".equals(rpc.getName())) {
            Logger.d(TAG, "HARDCODED_VLD_TURN_OFF");

            // 0x ----------- : 55 00 09 07 01 56 D2 01 00 00 00 00 00 00 00 03
            // 01 85 64 14 FF 00 F0
            // unsigned int - : 85 00 09 07 01 86 210 01 00 00 00 00 00 00 00 03
            // 01 133 100 20 255 00 240

            byte[] fullVldTurnOffPacket = { 85, 00, 9, 07, 01, 86, (byte) 210, 01, 00, 00, 00, 00, 00, 00, 00, 03, 01,
                    (byte) 133, 100, 20, (byte) 255, 00, (byte) 240 };

            Logger.d(TAG,
                    "Utils.bytesToHexString(fullVldTurnOffPacket): " + Utils.bytesToHexString(fullVldTurnOffPacket));
            Logger.d(TAG, "BEFORE: driver.send(fullVldTurnOffPacket)");
            driver.send(fullVldTurnOffPacket);
            Logger.d(TAG, "AFTER: driver.send(fullVldTurnOffPacket)");

        } else if ("HARDCODED_APPAIR_TURN_ON".equals(rpc.getName())) {
            Logger.d(TAG, "HARDCODED_APPAIR_TURN_ON");

            // 0x ----------- : 55 00 07 07 01 7A F6 50 00 29 21 9F 30 01 FF FF
            // FF FF 31 00 37

            // unsigned int - : 85 00 07 07 01 122 246 80 00 41 33 159 48 01 255
            // 255 255 255 49 00 55

            byte[] fullSwitchAppairTurnOnPacket = { 85, 00, 07, 07, 01, 122, (byte) 246, 80, 00, 41, 33, (byte) 159,
                    48, 01, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 49, 00, 55 };

            Logger.d(
                    TAG,
                    "Utils.bytesToHexString(fullSwitchAppairTurnOnPacket): "
                            + Utils.bytesToHexString(fullSwitchAppairTurnOnPacket));
            Logger.d(TAG, "BEFORE: driver.send(fullSwitchAppairTurnOnPacket)");
            driver.send(fullSwitchAppairTurnOnPacket);
            Logger.d(TAG, "AFTER: driver.send(fullSwitchAppairTurnOnPacket)");

        } else if ("HARDCODED_TURN_OFF".equals(rpc.getName())) {
            Logger.d(TAG, "HARDCODED_TURN_OFF");

            // 0x ----------- : 55 00 07 07 01 7A F6 70 00 29 21 9F 30 01 FF FF
            // FF FF 2D 00 62

            // unsigned int - : 85 00 07 07 01 122 246 112 00 41 33 159 48 01
            // 255 255 255 255 45 00 98

            byte[] fullSwitchTurnOffPacket = { 85, 00, 07, 07, 01, 122, (byte) 246, 112, 00, 41, 33, (byte) 159, 48,
                    01, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 45, 00, 98 };

            Logger.d(
                    TAG,
                    "Utils.bytesToHexString(fullSwitchTurnOffPacket): "
                            + Utils.bytesToHexString(fullSwitchTurnOffPacket));
            Logger.d(TAG, "BEFORE: driver.send(fullSwitchTurnOffPacket)");
            driver.send(fullSwitchTurnOffPacket);
            Logger.d(TAG, "AFTER: driver.send(fullSwitchTurnOffPacket)");

        } else {
            // Generate the SYS_EX message relative to the RPC
            MessageSYS_EX msg = new MessageSYS_EX(rpc);
            for (int i = 0; i < msg.getSubTelNum(); i++) {
                byte[] telegram = (byte[]) msg.getTelegrams().get(i);
                driver.send(telegram);
            }
        }
    }

    public void remove() {
        try {
            sReg.unregister();
        } catch (IllegalStateException e) {
            Logger.e(TAG, "attempt to unregister a device twice : " + e.getMessage());
        }
    }

}
