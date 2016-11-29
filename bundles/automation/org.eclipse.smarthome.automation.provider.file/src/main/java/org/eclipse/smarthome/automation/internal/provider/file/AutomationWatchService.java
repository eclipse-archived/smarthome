/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;

import org.eclipse.smarthome.core.service.AbstractWatchQueueReader;
import org.eclipse.smarthome.core.service.AbstractWatchService;

/**
 * This class is an implementation of {@link AbstractWatchService} which is responsible for tracking changes in file
 * system by Java WatchService.
 * <p>
 * It provides functionality for tracking {@link #watchingDir} changes to import or remove the automation objects.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
@SuppressWarnings("rawtypes")
public class AutomationWatchService extends AbstractWatchService {

    private String watchingDir;
    private AbstractFileProvider provider;

    public AutomationWatchService(AbstractFileProvider provider, String watchingDir) {
        this.provider = provider;
        this.watchingDir = watchingDir;
        File file = new File(watchingDir);
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("Directory \"%s\" not exist!", file.getAbsolutePath()));
        }
    }

    @Override
    protected String getSourcePath() {
        return watchingDir;
    }

    @Override
    protected AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch,
            Map<WatchKey, Path> registeredWatchKeys) {
        return new WatchQueueReader(watchService, toWatch, provider, registeredWatchKeys);
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected WatchKey registerDirectory(Path subDir) throws IOException {
        return subDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    private static class WatchQueueReader extends AbstractWatchQueueReader {

        private AbstractFileProvider provider;

        WatchQueueReader(WatchService watchService, Path dirToWatch, AbstractFileProvider provider,
                Map<WatchKey, Path> registeredWatchKeys) {
            super(watchService, dirToWatch, registeredWatchKeys);
            this.provider = provider;
        }

        @Override
        protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
            if (!path.getFileName().startsWith(".")) {
                if (kind.equals(ENTRY_DELETE)) {
                    provider.removeResources(
                            new File(baseWatchedDir.toAbsolutePath() + File.separator + path.toString()));
                } else {
                    provider.importResources(
                            new File(baseWatchedDir.toAbsolutePath() + File.separator + path.toString()));
                }
            }
        }
    }

}