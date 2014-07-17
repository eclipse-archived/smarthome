/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.mapdb;

import java.io.File;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Thomas.Eichstaedt-Engelen - Initial Contribution and API
 */
public class MapDbStorageService implements StorageService {

	private static final Logger logger = 
		LoggerFactory.getLogger(MapDbStorageService.class);

	private static final String DB_FOLDER_NAME = "etc/mapdb";
	private static final String DB_FILE_NAME = "storage.mapdb";
	
    private DB db;
    
    
	public void activate() {
		File folder = new File(DB_FOLDER_NAME);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File dbFile = new File(DB_FOLDER_NAME, DB_FILE_NAME);
		db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
		
		logger.debug("Open (or create) MapDB file at '{}'.", dbFile.getAbsolutePath());
	}
	
	public void deactivate() {
		logger.debug("Deactivated MapDB Storage Service.");
	}	


	@Override
	public <T> Storage<T> getStorage(BundleContext consumerContext, String name) {
		return new MapDbStorage<T>(db, consumerContext, name);
	}
	
}
