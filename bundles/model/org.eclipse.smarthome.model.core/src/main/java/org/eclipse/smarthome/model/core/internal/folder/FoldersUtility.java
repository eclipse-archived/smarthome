/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core.internal.folder;

import java.io.File;

import org.eclipse.smarthome.config.core.ConfigDispatcher;

/**
 * This class provides utilities relevant for the FolderObserver service
 * 
 * @author Fabio Marini
 * 
 */
public class FoldersUtility {

	/**
	 * Returns the {@link File} object for a given filename It builds the file's
	 * full path
	 * 
	 * @param filename
	 *            the file name to get the {@link File} for
	 * @return the corresponding {@link File}
	 */
	public static File getFolder(String filename) {
		File folder = new File(ConfigDispatcher.getConfigFolder()
				+ File.separator + filename);
		return folder;
	}

	/**
	 * Returns the extension of the given file
	 * 
	 * @param filename
	 *            the file name to get the extension
	 * @return the file's extension
	 */
	public static String getExtension(String filename) {
		String fileExt = filename.substring(filename.lastIndexOf(".") + 1);
		return fileExt;
	}
}
