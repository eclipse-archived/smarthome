/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

/**
 * This class provides constants relevant for the configuration of openHAB
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ConfigConstants {

	/** The program argument name for setting the user data directory path */
	final static public String USERDATA_DIR_PROG_ARGUMENT = "smarthome.userdata";

	/** The program argument name for setting the main config directory path */
	final static public String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";
	
	/** The program argument name for setting the service config directory path */
	final static public String SERVICEDIR_PROG_ARGUMENT = "smarthome.servicedir";

	/** The program argument name for setting the service pid namespace */
	final static public String SERVICEPID_PROG_ARGUMENT = "smarthome.servicepid";

	/** The program argument name for setting the default services config file name */
	final static public String SERVICECFG_PROG_ARGUMENT = "smarthome.servicecfg";

	/** The main configuration directory name */
	final static public String MAIN_CONFIG_FOLDER = "conf"; 
	
	/** The default folder name of the configuration folder of services */
	final static public String SERVICES_FOLDER = "services";

	/** The default namespace for service pids */
	final static public String SERVICE_PID_NAMESPACE = "org.eclipse.smarthome";

	/** The default services configuration filename */
	final static public String SERVICE_CFG_FILE = "smarthome.cfg";
}
