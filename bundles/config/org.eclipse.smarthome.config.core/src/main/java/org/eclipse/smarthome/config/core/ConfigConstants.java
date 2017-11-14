/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

/**
 * This class provides constants relevant for the configuration of Eclipse SmartHome
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ConfigConstants {

    /** The program argument name for setting the user data directory path */
    final static public String USERDATA_DIR_PROG_ARGUMENT = "smarthome.userdata";

    /** The program argument name for setting the main config directory path */
    final static public String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static public String DEFAULT_CONFIG_FOLDER = "conf";

    /** The default user data directory name */
    final static public String DEFAULT_USERDATA_FOLDER = "userdata";

    /**
     * Returns the configuration folder path name. The main config folder <code>&lt;smarthome&gt;/config</code> can be
     * overwritten by setting
     * the System property <code>smarthome.configdir</code>.
     * 
     * @return the configuration folder path name
     */
    static public String getConfigFolder() {
        String progArg = System.getProperty(CONFIG_DIR_PROG_ARGUMENT);
        if (progArg != null) {
            return progArg;
        } else {
            return DEFAULT_CONFIG_FOLDER;
        }
    }

    /**
     * Returns the user data folder path name. The main user data folder <code>&lt;smarthome&gt;/userdata</code> can be
     * overwritten by setting
     * the System property <code>smarthome.userdata</code>.
     * 
     * @return the user data folder path name
     */
    static public String getUserDataFolder() {
        String progArg = System.getProperty(USERDATA_DIR_PROG_ARGUMENT);
        if (progArg != null) {
            return progArg;
        } else {
            return DEFAULT_USERDATA_FOLDER;
        }
    }
}
