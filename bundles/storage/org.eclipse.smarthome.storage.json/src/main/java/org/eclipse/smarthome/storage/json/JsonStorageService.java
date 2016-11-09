/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.storage.json;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link StorageService} provides a mechanism to store
 * data in Json files.
 *
 * @author Chris Jackson - Initial Contribution
 */
public class JsonStorageService implements StorageService {

    private final Logger logger = LoggerFactory.getLogger(JsonStorageService.class);

    /** the folder name to store database ({@code jsondb} by default) */
    private String dbFolderName = "jsondb";

    private final String CFG_MAX_BACKUP_FILES = "backup_files";
    private final String CFG_WRITE_DELAY = "write_delay";
    private final String CFG_MAX_DEFER_DELAY = "max_defer_delay";

    private int maxBackupFiles = 5;
    private int writeDelay = 500;
    private int maxDeferredPeriod = 60000;

    private final Map<String, JsonStorage<Object>> storageList = new HashMap<String, JsonStorage<Object>>();

    protected void activate(ComponentContext cContext, Map<String, Object> properties) {
        dbFolderName = ConfigConstants.getUserDataFolder() + File.separator + dbFolderName;
        File folder = new File(dbFolderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File backup = new File(dbFolderName, "backup");
        if (!backup.exists()) {
            backup.mkdirs();
        }
        logger.debug("Json Storage Service: Activated.");

        if (properties == null || properties.isEmpty()) {
            return;
        }

        try {
            if (properties.get(CFG_MAX_BACKUP_FILES) != null) {
                maxBackupFiles = Integer.parseInt((String) properties.get(CFG_MAX_BACKUP_FILES));
            }
        } catch (NumberFormatException nfe) {
            logger.error("Value {} for {} is invalid. Using {}.", properties.get(CFG_MAX_BACKUP_FILES),
                    CFG_MAX_BACKUP_FILES, maxBackupFiles);
        }
        try {
            if (properties.get(CFG_WRITE_DELAY) != null) {
                writeDelay = Integer.parseInt((String) properties.get(CFG_WRITE_DELAY));
            }
        } catch (NumberFormatException nfe) {
            logger.error("Value {} for {} is invalid. Using {}.", properties.get(CFG_WRITE_DELAY), CFG_WRITE_DELAY,
                    writeDelay);
        }
        try {
            if (properties.get(CFG_MAX_DEFER_DELAY) != null) {
                maxDeferredPeriod = Integer.parseInt((String) properties.get(CFG_MAX_DEFER_DELAY));
            }
        } catch (NumberFormatException nfe) {
            logger.error("Value {} for {} is invalid. Using {}.", properties.get(CFG_MAX_DEFER_DELAY),
                    CFG_MAX_DEFER_DELAY, maxDeferredPeriod);
        }
    }

    protected void deactivate() {
        // Since we're using a delayed commit, we need to write out any data
        for (JsonStorage<Object> storage : storageList.values()) {
            storage.commitDatabase();
        }
        logger.debug("Json Storage Service: Deactivated.");
    }

    @Override
    public <T> Storage<T> getStorage(String name, ClassLoader classLoader) {
        File file = new File(dbFolderName, name + ".json");

        if (!storageList.containsKey(name)) {
            storageList.put(name, (JsonStorage<Object>) new JsonStorage<T>(file, classLoader, maxBackupFiles,
                    writeDelay, maxDeferredPeriod));
        }
        return (Storage<T>) storageList.get(name);
    }

    @Override
    public <T> Storage<T> getStorage(String name) {
        return getStorage(name, null);
    }
}
