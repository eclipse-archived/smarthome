/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.service;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for watch queue readers
 *
 * @author Fabio Marini
 * @author Dimitar Ivanov - use relative path in watch events. Added option to watch directory events or not
 */
public abstract class AbstractWatchQueueReader implements Runnable {

    /**
     * Default logger for ESH Watch Services
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractWatchQueueReader.class);

    protected WatchService watchService;

    protected Path baseWatchedDir;

    private Map<WatchKey, Path> registeredKeys;

    private boolean watchingDirectoryChanges;

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

    /**
     * Build the {@link AbstractWatchQueueReader} object with the given parameters. The directory changes will be
     * watched by default, e.g. watchingDirectoryChanges will be set to <code>true</code> (see
     * {@link #setWatchingDirectoryChanges(boolean)})
     *
     * @param watchService the watch service. Available to subclasses as {@link #watchService}
     * @param watchedDir the base directory, watched by the watch service. Available to subclasses as
     *            {@link #baseWatchedDir}
     * @param registeredKeys a mapping between the {@link WatchKey}s and their corresponding directories, registered
     *            in the watch service.
     */
    public AbstractWatchQueueReader(WatchService watchService, Path watchedDir, Map<WatchKey, Path> registeredKeys) {
        this(watchService, watchedDir, registeredKeys, true);
    }

    /**
     * Build the {@link AbstractWatchQueueReader} object with the given parameters. The directory changes will be
     * watched by default, e.g. watchingDirectoryChanges will be set to <code>true</code> (see
     * {@link #setWatchingDirectoryChanges(boolean)})
     *
     * @param watchService the watch service
     * @param watchedDir the base directory, watched by the watch service. Available to subclasses as
     *            {@link #baseWatchedDir}
     * @param registeredKeys a mapping between the {@link WatchKey}s and their corresponding directories, registered
     *            in the watch service.
     * @param watchingDirectoryChanges whether this queue reader will be watching the directory changes when the watch
     *            events are processed (for more information see
     *            {@link #setWatchingDirectoryChanges(boolean)}).
     */
    public AbstractWatchQueueReader(WatchService watchService, Path watchedDir, Map<WatchKey, Path> registeredKeys,
            boolean watchingDirectoryChanges) {
        this.watchService = watchService;
        this.baseWatchedDir = watchedDir;
        this.registeredKeys = registeredKeys;
        setWatchingDirectoryChanges(watchingDirectoryChanges);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.service.IWatchService#activate()
     */
    @Override
    public void run() {
        try {
            for (;;) {
                WatchKey key = null;
                try {
                    key = watchService.take();
                } catch (InterruptedException exc) {
                    logger.warn(MessageFormat.format("Catched InterruptedException : {0}", exc.getLocalizedMessage()));

                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        logger.warn(MessageFormat.format("Found event with overflow kind : {0}", event));

                        continue;
                    }

                    Path relativePath = resolveToRelativePath(key, event);
                    if (relativePath != null) {
                        // Process the event only when a relative path to it is resolved
                        processWatchEvent(event, kind, relativePath);
                    }
                }

                key.reset();
            }
        } catch (ClosedWatchServiceException ecx) {
            logger.debug("ClosedWatchServiceException catched! {}. \n{} Stopping ", ecx.getLocalizedMessage(),
                    Thread.currentThread().getName());

            return;
        }
    }

    private Path resolveToRelativePath(WatchKey key, WatchEvent<?> event) {
        WatchEvent<Path> ev = cast(event);
        // Context for directory entry event is the file name of
        // entry.
        Path contextPath = ev.context();

        Path registeredPath = registeredKeys.get(key);
        if (registeredPath != null) {
            // If the path has been registered in the watch service it relative path can be resolved

            // The context path is resolved by its already registered parent path
            Path resolvedContextPath = registeredPath.resolve(contextPath);

            // Relativize the resolved context to the directory watched (Build the relative path)
            Path path = baseWatchedDir.relativize(resolvedContextPath);

            // As the modification of file in subdirectory is considered a modification on the subdirectory itself, we
            // will consider the defined behavior to watch the directory changes
            if (!isWatchingDirectoryChanges() && baseWatchedDir.resolve(path).toFile().isDirectory()) {
                // As we have found a directory event and do not want to track directory changes - we will skip it
                return null;
            }
            return path;
        }

        logger.warn(
                "Detected invalid WatchEvent '{}' and key '{}' for entry '{}' in not registered file or directory of '{}'",
                event, key, contextPath, baseWatchedDir);
        return null;
    }

    /**
     * Processes the given watch event. Note that the kind and the number of the events for the watched directory is a
     * platform dependent (see the "Platform dependencies" sections of {@link WatchService}).
     *
     * @param event the watch event to be handled
     * @param kind the event's kind
     * @param path the path of the event (relative to the {@link #baseWatchedDir}
     */
    protected abstract void processWatchEvent(WatchEvent<?> event, WatchEvent.Kind<?> kind, Path path);

    /**
     * If the queue reader is watching the directory changes, all the watch events will be processed. Otherwise the
     * events for directories will be skipped.
     *
     * @return <code>true</code> if the directory events will be processed and <code>false</code> otherwise
     */
    public boolean isWatchingDirectoryChanges() {
        return watchingDirectoryChanges;
    }

    /**
     * If the queue reader is watching the directory changes, all the watch events will be processed. Otherwise the
     * events for changed directories will be skipped. For example, on some platforms an event for modified directory is
     * generated when a new file is created within the directory. However, this behavior could vary a lot, depending on
     * the platform (for more information see "Platform dependencies" section in the {@link WatchService} documentation)
     *
     * @param watchDirectoryChanges set to <code>true</code> if the directory events have to be processed and
     *            <code>false</code> otherwise
     */
    public final void setWatchingDirectoryChanges(boolean watchDirectoryChanges) {
        this.watchingDirectoryChanges = watchDirectoryChanges;
    }

}