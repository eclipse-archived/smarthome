/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.internal.ConfigActivator;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a mean to read any kind of configuration data from a shared config
 * file and dispatch it to the different bundles using the {@link ConfigurationAdmin} service.
 * 
 * <p>The name of the configuration file can be provided as a program argument "smarthome.configfile".
 * If this argument is not set, the default "configurations/smarthome.cfg" will be used.
 * In case the configuration file does not exist, a warning will be logged and no action
 * will be performed.</p>
 * 
 * <p>The format of the configuration file is similar to a standard property file, with the
 * exception that the property name must be prefixed by the service pid of the {@link ManagedService}:</p>
 * <p>&lt;service-pid&gt;:&lt;property&gt;=&lt;value&gt;</p>
 * <p>The prefix "org.eclipse.smarthome" can be omitted on the service pid, it is automatically added if
 * the pid does not contain any "."</p> 
 * 
 * <p>A quartz job can be scheduled to reinitialize the Configurations on a regular
 * basis (defaults to '1' minute)</p>
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Thomas.Eichstaedt-Engelen
 */
public class ConfigDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDispatcher.class);

	// by default, we use the "configurations" folder in the home directory, but this location
	// might be changed in certain situations (especially when setting a config folder in the
	// SmartHome Designer).
	private static String configFolder = ConfigConstants.MAIN_CONFIG_FOLDER;
			
	public void activate() {
		initializeBundleConfigurations();
	}
	
	public void deactivate() {
	}
	
	/**
	 * Returns the configuration folder path name. The main config folder 
	 * <code>&lt;smarthome&gt;/configurations</code> could be overwritten by setting
	 * the System property <code>smarthome.configdir</code>.
	 * 
	 * @return the configuration folder path name
	 */
	public static String getConfigFolder() {
		String progArg = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT);
		if (progArg != null) {
			return progArg;
		} else {
			return configFolder;
		}
	}

	/**
	 * Sets the configuration folder to use. Calling this method will automatically
	 * trigger the loading and dispatching of the contained configuration files.
	 * 
	 * @param configFolder the path name to the new configuration folder
	 */
	public static void setConfigFolder(String configFolder) {
		ConfigDispatcher.configFolder = configFolder;
		initializeBundleConfigurations();
	}

	public static void initializeBundleConfigurations() {
		initializeMainConfiguration();			
	}

	private static void initializeMainConfiguration() {
		String mainConfigFilePath = getMainConfigurationFilePath();
		File mainConfigFile = new File(mainConfigFilePath);
		
		try {
			logger.debug("Processing main configuration file '{}'.", mainConfigFile.getAbsolutePath());
			processConfigFile(mainConfigFile);
		} catch (FileNotFoundException e) {
			logger.warn("Main configuration file '{}' does not exist.", mainConfigFilePath);
		} catch (IOException e) {
			logger.error("Main configuration file '{}' cannot be read.", mainConfigFilePath, e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void processConfigFile(File configFile) throws IOException, FileNotFoundException {
		ConfigurationAdmin configurationAdmin = 
			(ConfigurationAdmin) ConfigActivator.configurationAdminTracker.getService();
		if (configurationAdmin != null) {
			// we need to remember which configuration needs to be updated because values have changed.
			Map<Configuration, Dictionary> configsToUpdate = new HashMap<Configuration, Dictionary>();
			
			// also cache the already retrieved configurations for each pid
			Map<Configuration, Dictionary> configMap = new HashMap<Configuration, Dictionary>();
			
			List<String> lines = IOUtils.readLines(new FileInputStream(configFile));
			for(String line : lines) {					
				String[] contents = parseLine(configFile.getPath(), line);
				// no valid configuration line, so continue
				if(contents==null) continue;
				String pid = contents[0];
				String property = contents[1];
				String value = contents[2];
				Configuration configuration = configurationAdmin.getConfiguration(pid, null);
				if(configuration!=null) {
					Dictionary configProperties = configMap.get(configuration);
					if(configProperties==null) {
						configProperties = new Properties();
						configMap.put(configuration, configProperties);
					}
					if(!value.equals(configProperties.get(property))) {
						configProperties.put(property, value);
						configsToUpdate.put(configuration, configProperties);
					}
				}
			}
			
			for(Entry<Configuration, Dictionary> entry : configsToUpdate.entrySet()) {
				entry.getKey().update(entry.getValue());
			}
		}
	}

	private static String[] parseLine(final String filePath, final String line) {
		String trimmedLine = line.trim();
		if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
			return null;
		}
		
		if (trimmedLine.substring(1).contains(":")) { 
			String pid = StringUtils.substringBefore(line, ":");
			String rest = line.substring(pid.length() + 1);
			if(!pid.contains(".")) {
				pid = "org.eclipse.smarthome." + pid;
			}
			if(!rest.isEmpty() && rest.substring(1).contains("=")) {
				String property = StringUtils.substringBefore(rest, "=");
				String value = rest.substring(property.length() + 1);
				return new String[] { pid.trim(), property.trim(), value.trim() };
			}
		}
		
		logger.warn("Cannot parse line '{}' of main configuration file '{}'.", line, filePath);
		return null;
	}

	private static String getMainConfigurationFilePath() {
		String progArg = System.getProperty(ConfigConstants.CONFIG_FILE_PROG_ARGUMENT);
		if (progArg != null) {
			return getConfigFolder() + "/" + progArg;
		} else {
			return getConfigFolder() + "/" + ConfigConstants.MAIN_CONFIG_FILENAME;
		}
	}
		
}
