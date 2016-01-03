package org.eclipse.smarthome.automation.module.script.loader.internal;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches the given directory for file updates. It notifies the <code>ScriptManager</code>, if a change is detected.
 *
 * copied from Openhab 1
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptUpdateWatcher implements Runnable {
    static private final Logger logger = LoggerFactory.getLogger(ScriptUpdateWatcher.class);

    private ScriptManager scriptManager;
    private WatchService watcher;
    private File folder;

    private HashMap<File, Long> lastUpdate = new HashMap<File, Long>();

    public ScriptUpdateWatcher(ScriptManager scriptManager, File folder) {
        this.scriptManager = scriptManager;
        this.folder = folder;
    }

    public static String getScriptType(String fileName) {
        int fileExtesionStartIndex = fileName.lastIndexOf(".") + 1;
        if (fileExtesionStartIndex == -1) {
            return null;
        }

        String fileExtesion = fileName.substring(fileExtesionStartIndex);
        return fileExtesion;
    }

    @Override
    public void run() {
        try {
            watcher = FileSystems.getDefault().newWatchService();

            Path dir = Paths.get(folder.getAbsolutePath());
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            while (true) {
                WatchKey key;

                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    logger.info("ScriptUpdateWatcher interrupted");
                    return;
                }

                long currentTime = System.currentTimeMillis();

                for (WatchEvent<?> event : key.pollEvents()) {

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    WatchEvent.Kind<Path> kind = ev.kind();

                    Path fileName = ev.context();

                    File f = new File(folder, fileName.toString());

                    // skip files ending with ".script" (as these files are definitely no known scripting language)
                    if (f.getName().endsWith(".script")) {
                        continue;
                    }

                    Long lastTime = lastUpdate.get(f);

                    if (lastTime == null || currentTime - lastTime > 5000) {
                        logger.debug(kind.name() + ": " + fileName);
                        lastUpdate.put(f, currentTime);
                        if (kind == ENTRY_CREATE) {
                            scriptManager.loadScript(f.getAbsolutePath(), getScriptType(f.getName()),
                                    new InputStreamReader(new FileInputStream(f)));
                        } else if (kind == ENTRY_DELETE) {
                            scriptManager.unloadScript(f.getAbsolutePath());
                        } else if (kind == ENTRY_MODIFY) {
                            scriptManager.unloadScript(f.getAbsolutePath());
                            scriptManager.loadScript(f.getAbsolutePath(), getScriptType(f.getName()),
                                    new InputStreamReader(new FileInputStream(f)));
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

        } catch (IOException e1) {
            logger.error("WatchService could not be started", e1);
        }
    }

}
