/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for watch queue readers
 *
 * @author Fabio Marini
 *
 */
public abstract class AbstractWatchQueueReader implements Runnable {

    /**
     * Default logger for ESH Watch Services
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractWatchQueueReader.class);

    protected WatchService watchService;

    protected Path dir;

    /**
     * Perform a simple cast of given event to WatchEvent
     * 
     * @param event
     *            the event to cast
     * @return the casted event
     */
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Build the object with the given parameters
     * 
     * @param watchService
     *            the watch service
     * @param dir
     */
    public AbstractWatchQueueReader(WatchService watchService, Path dir) {
        this.watchService = watchService;
        this.dir = dir;
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

                    // Context for directory entry event is the file name of
                    // entry
                    WatchEvent<Path> ev = cast(event);
                    Path path = ev.context();

                    processWatchEvent(event, kind, path);
                }

                key.reset();
            }
        } catch (ClosedWatchServiceException ecx) {
            logger.debug("ClosedWatchServiceException catched! {}. \n{} Stopping ", ecx.getLocalizedMessage(), Thread
                    .currentThread().getName());

            return;
        }
    }

    /**
     * Processes the given watch event
     * 
     * @param event
     *            the watch event to perform
     * @param kind
     *            the event's kind
     * @param name
     *            the path of event
     */
    protected abstract void processWatchEvent(WatchEvent<?> event, WatchEvent.Kind<?> kind, Path path);
}