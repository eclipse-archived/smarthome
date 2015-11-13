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

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;

/**
 * Manage the configuration file.
 */
public class ConfigurationFileManager {

    /** TAG */
    public static final String TAG = ConfigurationFileManager.class.getName();

    private static Properties config = new Properties();

    /**
     * Tests.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Logger.d(TAG, "---" + getRorgFuncTypeAndFriendlynameFromConfigFile("0x12345678"));

        Logger.d(TAG, "---" + getRorgFuncTypeAndFriendlynameFromConfigFile(null));

        Logger.d(TAG, "---" + getRorgFuncTypeAndFriendlynameFromConfigFile(""));

        Logger.d(TAG, "---" + getRorgFuncTypeAndFriendlynameFromConfigFile("0x66655544"));

        // 0x12345678 == int 305419896
        Logger.d(TAG, "---" + Utils.bytesToHexString(Utils.intTo4Bytes(305419896)));

        Logger.d(TAG, "---" + ConfigurationFileManager.getRorgFuncTypeAndFriendlynameFromConfigFile(
                "0x" + Utils.bytesToHexString(Utils.intTo4Bytes(305419896))));

        // 0x00123456 == int 1193046
        Logger.d(TAG, "---" + Utils.bytesToHexString(Utils.intTo4Bytes(1193046)));

        Logger.d(TAG, "---" + ConfigurationFileManager.getRorgFuncTypeAndFriendlynameFromConfigFile(
                "0x" + Utils.bytesToHexString(Utils.intTo4Bytes(1193046))));

    }

    /**
     * Get the RORG FUNC TYPE, FriendlyName, and Description from the
     * configuration file located at: "." + File.separator + "enocean_config" +
     * File.separator + "enocean_config.txt". This method (re)loads the
     * configuration file each time. The configuration file is expected to
     * contain lines that are in line with the following pattern:
     * 0x12345678_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION=A1##02##01##Bla
     * bla-bla friendly bla-bla name ;-)##a desc where RORG, FUNC, TYPE,
     * Friendlyname, and Description are separated by a "##". In the example,
     * the Rorg is: "A1", the Func is: "02", the Type is: "01", the Friendlyname
     * is: "Bla bla-bla friendly bla-bla name ;-)", and the Description is:
     * "a desc". Friendly name, and Description may contain spaces, but can not
     * contain "##".
     *
     * @param enOceanId
     *            as an hexa value written as follow, e.g. 0x12345678.
     * @return the associated RORG-FUNC-TYPE-FRIENDLYNAME-DESCRIPTION object if
     *         present in the configuration file, e.g. A0 02 01 Water
     *         Sensor_45-17-62. Return null if nothing is associated to the
     *         given enOceanId, or if no configuration file is available, or if
     *         there is another IOException when reading the file, for example.
     */
    public static RorgFuncTypeFriendlynameDescription getRorgFuncTypeAndFriendlynameFromConfigFile(String enOceanId) {
        RorgFuncTypeFriendlynameDescription result = null;

        // Location of the configuration file.
        String configFilePath = "." + File.separator + "enocean_config" + File.separator + "enocean_config.txt";
        File configFile = new File(configFilePath);
        Logger.d(TAG, "configFile: " + configFile);
        if (configFile.exists()) {
            Logger.d(TAG, "DEBUG: The conf file exists: configFile: " + configFile);
            // Load current conf file.
            try {
                FileInputStream fis = new FileInputStream(configFile);
                config.load(fis);
                Logger.d(TAG, "DEBUG: Conf file has properly been loaded: config: " + config);

                String enOceanIdKey = enOceanId + "_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION";
                if (config.containsKey(enOceanIdKey)) {
                    Logger.d(TAG, "DEBUG: The given enOceanId: " + enOceanId
                            + " appears in the conf file, via the expected: " + enOceanIdKey + " key.");
                    String valueAssociatedToEnOceanIdKey = config.getProperty(enOceanIdKey);
                    Logger.d(TAG, "DEBUG: Its associated value is: " + valueAssociatedToEnOceanIdKey);

                    // Check that valueAssociatedToEnOceanIdKey contains four
                    // "##".

                    // From String to RorgFuncTypeFriendlyname.
                    String rorg = null;
                    String func = null;
                    String type = null;
                    String friendlyname = null;
                    String description = null;

                    // Get rorg
                    int firstDashIndex = valueAssociatedToEnOceanIdKey.indexOf("##");
                    rorg = valueAssociatedToEnOceanIdKey.substring(0, firstDashIndex);
                    if ("".equals(rorg)) {
                        rorg = null;
                    }
                    valueAssociatedToEnOceanIdKey = valueAssociatedToEnOceanIdKey.substring(firstDashIndex + 2);
                    // Get func
                    int secondDashIndex = valueAssociatedToEnOceanIdKey.indexOf("##");
                    func = valueAssociatedToEnOceanIdKey.substring(0, secondDashIndex);
                    if ("".equals(func)) {
                        func = null;
                    }
                    valueAssociatedToEnOceanIdKey = valueAssociatedToEnOceanIdKey.substring(secondDashIndex + 2);
                    // Get type
                    int thirdDashIndex = valueAssociatedToEnOceanIdKey.indexOf("##");
                    type = valueAssociatedToEnOceanIdKey.substring(0, thirdDashIndex);
                    if ("".equals(type)) {
                        type = null;
                    }
                    valueAssociatedToEnOceanIdKey = valueAssociatedToEnOceanIdKey.substring(thirdDashIndex + 2);
                    // Get friendlyname
                    int fourthDashIndex = valueAssociatedToEnOceanIdKey.indexOf("##");
                    friendlyname = valueAssociatedToEnOceanIdKey.substring(0, fourthDashIndex);
                    if ("".equals(friendlyname)) {
                        friendlyname = null;
                    }
                    valueAssociatedToEnOceanIdKey = valueAssociatedToEnOceanIdKey.substring(fourthDashIndex + 2);
                    // Get description
                    description = valueAssociatedToEnOceanIdKey;
                    Logger.d(TAG, "description: " + description);
                    if ("".equals(description)) {
                        description = null;
                    }
                    result = new RorgFuncTypeFriendlynameDescription(rorg, func, type, friendlyname, description);

                    return result;
                } else {
                    Logger.d(TAG, "DEBUG: The given enOceanId: " + enOceanId + " does NOT appear in the conf file.");
                    return result;
                }
            } catch (IOException e) {
                Logger.d(TAG, "DEBUG: Conf file has NOT properly been loaded: config: " + config);
                e.printStackTrace();
                return result;
            }
        } else {
            try {
                throw new IOException("The configuration file is expected to be available at: " + configFilePath
                        + ", but it is not there.");
            } catch (IOException e) {
                e.printStackTrace();
                return result;
            }
        }
    }

}
