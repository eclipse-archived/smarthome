/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for watch queue readers
 *
 * @author Fabio Marini
 * @author Dimitar Ivanov - use relative path in watch events. Added option to watch directory events or not
 * @author Ana Dimova - reduce to a single watch thread for all class instances of {@link AbstractWatchService}
 */
public class WatchQueueReader implements Runnable {

    /**
     * Default logger for ESH Watch Services
     */
    protected final Logger logger = LoggerFactory.getLogger(WatchQueueReader.class);

    protected WatchService watchService;

    private Map<WatchKey, Path> registeredKeys;

    private Map<WatchKey, AbstractWatchService> keyToService;

    private Thread qr;

    private static final WatchQueueReader INSTANCE = new WatchQueueReader();

    /**
     * Perform a simple cast of given event to WatchEvent
     *
     * @param event the event to cast
     * @return the casted event
     */
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public static WatchQueueReader getInstance() {
        return INSTANCE;
    }

    /**
     * Builds the {@link WatchQueueReader} object that will monitor the directory changes if there is
     * an {@link AbstractWatchService} that requests its functionality.
     */
    private WatchQueueReader() {
        registeredKeys = new HashMap<>();
        keyToService = new HashMap<>();
    }

    /**
     * Customize the queue reader to process the watch events for the given directory, provided by the watch service
     *
     * @param watchService the watch service, requesting the watch events for the watched directory
     * @param toWatch the directory being watched by the watch service
     * @param watchSubDirectories a boolean flag that specifies if the child directories of the registered directory
     *            will being watched by the watch service
     */
    protected void customizeWatchQueueReader(AbstractWatchService watchService, Path toWatch,
            boolean watchSubDirectories) {
        try {
            if (watchSubDirectories) {
                // walk through all folders and follow symlinks
                registerWithSubDirectories(watchService, toWatch);
            } else {
                registerDirectoryInternal(watchService, watchService.getWatchEventKinds(toWatch), toWatch);
            }
        } catch (NoSuchFileException e) {
            logger.debug("Not watching folder '{}' as it does not exist.", toWatch);
        } catch (IOException e) {
            logger.warn("Cannot customize folder watcher for folder '{}'", toWatch, e);
        }
    }

    private void registerWithSubDirectories(AbstractWatchService watchService, Path toWatch) throws IOException {
        Files.walkFileTree(toWatch, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs)
                            throws IOException {
                        Kind<?>[] kinds = watchService.getWatchEventKinds(subDir);
                        if (kinds != null) {
                            registerDirectoryInternal(watchService, kinds, subDir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private synchronized void registerDirectoryInternal(AbstractWatchService service, Kind<?>[] kinds, Path directory) {
        if (watchService == null) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                qr = new Thread(this, "Dir Watcher");
                qr.start();
            } catch (IOException e) {
                logger.debug("The directory '{}' was not registered in the watch service", directory, e);
                return;
            }
        }
        WatchKey registrationKey = null;
        try {
            registrationKey = directory.register(this.watchService, kinds);
        } catch (IOException e) {
            logger.debug("The directory '{}' was not registered in the watch service: {}", directory, e.getMessage());
        }
        if (registrationKey != null) {
            registeredKeys.put(registrationKey, directory);
            keyToService.put(registrationKey, service);
        } else {
            logger.debug("The directory '{}' was not registered in the watch service", directory);
        }
    }

    public synchronized void stopWatchService(AbstractWatchService service) {
        if (watchService != null) {
            List<WatchKey> keys = new LinkedList<>();
            for (WatchKey key : keyToService.keySet()) {
                if (keyToService.get(key) == service) {
                    keys.add(key);
                }
            }
            if (keys.size() == keyToService.size()) {
                try {
                    watchService.close();
                } catch (IOException e) {
                    logger.warn("Cannot deactivate folder watcher", e);
                }
                watchService = null;
                keyToService.clear();
                registeredKeys.clear();
            } else {
                for (WatchKey key : keys) {
                    key.cancel();
                    keyToService.remove(key);
                    registeredKeys.remove(key);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            for (;;) {
                WatchKey key = null;
                try {
                    key = watchService.take();
                } catch (InterruptedException exc) {
                    logger.info("Caught InterruptedException: {}", exc.getLocalizedMessage());
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        logger.warn("Found an event of kind 'OVERFLOW': {}. File system changes might have been missed.",
                                event);
                        continue;
                    }

                    Path resolvedPath = resolvePath(key, event);
                    if (resolvedPath != null) {

                        // Process the event only when a relative path to it is resolved
                        AbstractWatchService service = null;
                        synchronized (this) {
                            service = keyToService.get(key);
                        }
                        if (service != null) {
                            File f = resolvedPath.toFile();
                            service.processWatchEvent(event, kind, resolvedPath);
                            if (kind == ENTRY_CREATE && f.isDirectory() && service.watchSubDirectories()
                                    && service.getWatchEventKinds(resolvedPath) != null) {
                                registerDirectoryInternal(service, service.getWatchEventKinds(resolvedPath),
                                        resolvedPath);
                            } else if (kind == ENTRY_DELETE) {
                                synchronized (this) {
                                    WatchKey toCancel = null;
                                    for (WatchKey k : registeredKeys.keySet()) {
                                        if (registeredKeys.get(k).equals(resolvedPath)) {
                                            toCancel = k;
                                            break;
                                        }
                                    }
                                    if (toCancel != null) {
                                        registeredKeys.remove(toCancel);
                                        keyToService.remove(toCancel);
                                        toCancel.cancel();
                                    }
                                }
                            }
                        }
                    }
                }

                key.reset();
            }
        } catch (Exception exc) {
            logger.debug("ClosedWatchServiceException caught! {}. \n{} Stopping ", exc.getLocalizedMessage(),
                    Thread.currentThread().getName());
            return;
        }
    }

    private Path resolvePath(WatchKey key, WatchEvent<?> event) {
        WatchEvent<Path> ev = cast(event);
        // Context for directory entry event is the file name of entry.
        Path contextPath = ev.context();
        Path baseWatchedDir = null;
        Path registeredPath = null;
        synchronized (this) {
            baseWatchedDir = keyToService.get(key).getSourcePath();
            registeredPath = registeredKeys.get(key);
        }
        if (registeredPath != null) {
            // If the path has been registered in the watch service it relative path can be resolved
            // The context path is resolved by its already registered parent path
            Path resolvedContextPath = registeredPath.resolve(contextPath);
            // Relativize the resolved context to the directory watched (Build the relative path)
            Path path = baseWatchedDir.relativize(resolvedContextPath);
            // As the modification of file in subdirectory is considered a modification on the subdirectory itself, we
            // will consider the defined behavior to watch the directory changes
            if (baseWatchedDir.resolve(path).toFile().isDirectory()
                    && !isWatchingDirectoryChanges(key, resolvedContextPath)) {
                // As we have found a directory event and do not want to track directory changes - we will skip it
                return null;
            }

            return resolvedContextPath;
        }

        logger.warn(
                "Detected invalid WatchEvent '{}' and key '{}' for entry '{}' in not registered file or directory of '{}'",
                event, key, contextPath, baseWatchedDir);
        return null;
    }

    /**
     * Tells to the queue reader if watching for the directory changes. All the watch events will be processed if the
     * method returns <code>true<code>. Otherwise the events for directories will be skipped.
     *
     * &#64;param key the WatchKey returned for directory, on its registration in the watch service.
     * &#64;param resolvedContextPath the path of the event (resolved to the {@link #baseWatchedDir})
     *
     * @return <code>true</code> if the directory events will be processed and <code>false</code> otherwise
     */
    private boolean isWatchingDirectoryChanges(WatchKey key, Path resolvedContextPath) {
        return keyToService.get(key).getWatchingDirectoryChanges(resolvedContextPath);
    }

}