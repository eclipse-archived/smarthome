/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for OSGI services that access to file system by Java WatchService. <br />
 * See the WatchService <a href=
 * "http://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html"
 * >java docs</a> for more details
 *
 * @author Fabio Marini
 *
 */
public abstract class AbstractWatchService {

    /**
     * Default logger for ESH Watch Services
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The WatchService
     */
    protected WatchService watchService;

    /**
     * The queue reader
     */
    protected AbstractWatchQueueReader watchQueueReader;

    /**
     * Method to call on service activation
     */
    public void activate() {
        initializeWatchService();
    }

    /**
     * Method to call on service deactivation
     */
    public void deactivate() {
        stopWatchService();
    }

    protected void initializeWatchService() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warn("Cannot deactivate folder watcher", e);
            }
        }

        String pathToWatch = getSourcePath();
        if (StringUtils.isNotBlank(pathToWatch)) {
            Path toWatch = Paths.get(pathToWatch);
            try {
                if (watchSubDirectories()) {
                    watchService = FileSystems.getDefault().newWatchService();

                    Files.walkFileTree(toWatch, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs)
                                throws IOException {
                            registerDirectory(subDir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    watchService = toWatch.getFileSystem().newWatchService();
                    registerDirectory(toWatch);
                }

                AbstractWatchQueueReader reader = buildWatchQueueReader(watchService, toWatch);

                Thread qr = new Thread(reader, "Dir Watcher");
                qr.start();
            } catch (IOException e) {
                logger.error("Cannot activate folder watcher for folder '{}': {}", toWatch, e.getMessage());
            }
        }
    }

    protected void stopWatchService() {
        try {
            watchService.close();
        } catch (IOException e) {
            logger.warn("Cannot deactivate folder watcher", e);
        }

        watchService = null;
    }

    /**
     * 
     * @param watchService
     * @param toWatch
     * @return
     */
    protected abstract AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch);

    /**
     * 
     * @return
     */
    protected abstract String getSourcePath();

    /**
     * 
     * @return
     */
    protected abstract boolean watchSubDirectories();

    /**
     * @param subDir
     * @throws IOException
     */
    protected abstract void registerDirectory(Path subDir) throws IOException;
}
