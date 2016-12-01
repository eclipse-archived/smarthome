
/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
 * @author Dimitar Ivanov - added javadoc; introduced WatchKey to directory mapping for the queue reader
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
                final Map<WatchKey, Path> registeredWatchKeys = new HashMap<>();
                if (watchSubDirectories()) {
                    watchService = FileSystems.getDefault().newWatchService();

                    // walk through all folders and follow symlinks
                    Files.walkFileTree(toWatch, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                            new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs)
                                        throws IOException {
                                    registerDirectoryInternal(subDir, registeredWatchKeys);
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                } else {
                    watchService = toWatch.getFileSystem().newWatchService();
                    registerDirectoryInternal(toWatch, registeredWatchKeys);
                }

                AbstractWatchQueueReader reader = buildWatchQueueReader(watchService, toWatch, registeredWatchKeys);

                Thread qr = new Thread(reader, "Dir Watcher");
                qr.start();
            } catch (IOException e) {
                logger.error("Cannot activate folder watcher for folder '{}': {}", toWatch, e.getMessage());
            }
        }
    }

    private void registerDirectoryInternal(Path directory, Map<WatchKey, Path> registredWatchKeys) throws IOException {
        WatchKey registrationKey = registerDirectory(directory);
        if (registrationKey != null) {
            registredWatchKeys.put(registrationKey, directory);
        } else {
            logger.debug("The directory '{}' was not registered in the watch service", directory);
        }
    }

    /**
     * This method will close the {@link #watchService}.
     */
    protected void stopWatchService() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warn("Cannot deactivate folder watcher", e);
            }

            watchService = null;
        }
    }

    /**
     * Build a queue reader to process the watch events, provided by the watch service for the given directory
     *
     * @param watchService the watch service, providing the watch events for the watched directory
     * @param toWatch the directory being watched by the watch service
     * @param registredWatchKeys a mapping between the registered directories and their {@link WatchKey registration
     *            keys}.
     * @return the concrete queue reader
     */
    protected abstract AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch,
            Map<WatchKey, Path> registredWatchKeys);

    /**
     * @return the path to be watched as a {@link String}. The returned path should be applicable for creating a
     *         {@link Path} with the {@link Paths#get(String, String...)} method.
     */
    protected abstract String getSourcePath();

    /**
     * Determines whether the subdirectories of the source path (determined by the {@link #getSourcePath()}) will be
     * watched or not.
     *
     * @return <code>true</code> if the subdirectories will be watched and <code>false</code> if only the source path
     *         (determined by the {@link #getSourcePath()}) will be watched
     */
    protected abstract boolean watchSubDirectories();

    /**
     * Registers a directory to be watched by the watch service. The {@link WatchKey} of the registration should be
     * provided.
     *
     * @param directory the directory, which will be registered in the watch service
     * @return The {@link WatchKey} of the registration or <code>null</code> if no registration has been done.
     * @throws IOException if an error occurs while processing the given path
     */
    protected abstract WatchKey registerDirectory(Path directory) throws IOException;
}
