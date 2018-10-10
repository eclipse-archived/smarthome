package org.eclipse.smarthome.io.net.security.internal.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.eclipse.jdt.annotation.Nullable;

// TODO find out if core.service.AbstractWatchService needs to be extended
public class KeystoreFileWatcher implements Runnable {
    protected final WatchService watchService;
    protected @Nullable WatchKey watchKey;
    private @Nullable Runnable execRunnable;

    public KeystoreFileWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
    }

    public void setWatchFile(Path file, Runnable r) throws IOException {
        execRunnable = r;
        watchKey = file.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                // wait for key to be signalled
                WatchKey key = watchService.take();
                Runnable execRunnable = this.execRunnable;
                if (execRunnable == null) {
                    continue;
                }

                if (watchKey != key) {
                    System.err.println("WatchKey not recognized!");
                    continue;
                }

                // for (WatchEvent<?> event : key.pollEvents()) {
                // WatchEvent<Path> ev = cast(event);
                // ev.kind();
                // }
                execRunnable.run();

                // reset key
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException x) {
            return;
        }
    }
}