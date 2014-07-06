/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

/**
 * Interface for configuration dispatcher service
 * 
 * @author Fabio Marini
 */
public interface IConfigDispatcherService {

	/**
	 * Sets the configuration folder to use. Calling this method will automatically
	 * trigger the loading and dispatching of the contained configuration files.
	 * 
	 * @param configFolder the path name to the new configuration folder
	 */
	void setConfigFolder(String configFolder);
	
	
	/**
	 * Returns the configuration folder path name. The main config folder 
	 * <code>&lt;smarthome&gt;/configurations</code> could be overwritten by setting
	 * the System property <code>smarthome.configdir</code>.
	 * 
	 * @return the configuration folder path name
	 */
	String getConfigFolder();
}