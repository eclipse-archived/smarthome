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

	/** The program argument name for setting the main config directory path */
	final static public String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";
	
	/** The program argument name for setting the main config file name */
	final static public String CONFIG_FILE_PROG_ARGUMENT = "smarthome.configfile";
	
	/** The main configuration directory name of openHAB */
	final static public String MAIN_CONFIG_FOLDER = "configurations"; 
	
	/** The default filename of the main openHAB configuration file */
	final static public String MAIN_CONFIG_FILENAME = "smarthome.cfg";
}
