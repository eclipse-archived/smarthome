/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.map.internal;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which simply maps strings to other strings
 * </p>
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class MapTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(MapTransformationService.class);

    protected WatchService watchService = null;

    protected final Map<String, Properties> cachedProperties = new ConcurrentHashMap<>();

    protected void deactivate() {
        cachedProperties.clear();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.debug("Cannot cancel watch service for folder '{}'", getSourcePath());
            }
            watchService = null;
        }
    }

    /**
     * <p>
     * Transforms the input <code>source</code> by mapping it to another string. It expects the mappings to be read from
     * a file which is stored under the 'configurations/transform' folder. This file should be in property syntax, i.e.
     * simple lines with "key=value" pairs. To organize the various transformations one might use subfolders.
     * </p>
     * 
     * @param filename
     *            the name of the file which contains the key value pairs for the mapping. The name may contain
     *            subfoldernames
     *            as well
     * @param source
     *            the input to transform
     * 
     * @{inheritDoc
     * 
     */
    @Override
    public String transform(String filename, String source) throws TransformationException {

        if (filename == null || source == null) {
            throw new TransformationException("the given parameters 'filename' and 'source' must not be null");
        }

        if (watchService == null) {
            try {
                initializeWatchService();
            } catch (IOException e) {
                // we cannot watch the folder, so let's at least clear the cache
                cachedProperties.clear();
            }
        } else {
            processFolderEvents();
        }
        Properties properties = cachedProperties.get(filename);
        if (properties == null) {
            String path = getSourcePath() + File.separator + filename;
            try (Reader reader = new FileReader(path)) {
                properties = new Properties();
                properties.load(reader);
                cachedProperties.put(filename, properties);
            } catch (IOException e) {
                String message = "opening file '" + filename + "' throws exception";
                logger.error(message, e);
                throw new TransformationException(message, e);
            }
        }
        String target = properties.getProperty(source);
        if (target != null) {
            logger.debug("transformation resulted in '{}'", target);
            return target;
        } else {
            logger.warn("Could not find a mapping for '{}' in the file '{}'.", source, filename);
            return "";
        }
    }

    private void processFolderEvents() {
        WatchKey key = watchService.poll();
        if (key != null) {
            for (WatchEvent<?> e : key.pollEvents()) {
                if (e.kind() == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) e;
                Path path = ev.context();
                logger.debug("Refreshing transformation file '{}'", path);
                cachedProperties.remove(path.getFileName().toString());
            }
            key.reset();
        }
    }

    private void initializeWatchService() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        Path transformFilePath = Paths.get(getSourcePath());
        transformFilePath.register(watchService, ENTRY_DELETE, ENTRY_MODIFY);
    }

    protected String getSourcePath() {
        return ConfigConstants.getConfigFolder() + File.separator + TransformationService.TRANSFORM_FOLDER_NAME;
    }

}
