package org.eclipse.smarthome.automation.module.script.rulesupport.internal.loader;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.automation.module.script.ScriptEngineContainer;
import org.eclipse.smarthome.automation.module.script.ScriptEngineManager;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.service.AbstractWatchService;

/**
 * The {@link ScriptFileWatcher} watches the jsr223 directory for files. If a new/modified file is detected, the script
 * is read and passed to the {@link ScriptEngineManager}.
 *
 * @author Simon Merschjohann - initial contribution
 *
 */
public class ScriptFileWatcher extends AbstractWatchService {
    private static final String FILE_DIRECTORY = "automation/jsr223";
    private static final String SCRIPT_FILE_WATCH_POOL = "SCRIPT_FILE_WATCH_POOL";
    private static final long INITIAL_DELAY = 25;
    private static final long RECHECK_INTERVAL = 20;

    private long earliestStart = System.currentTimeMillis() + INITIAL_DELAY * 1000;

    private ScriptEngineManager manager;

    private Map<String, Set<URL>> urlsByScriptExtension = new ConcurrentHashMap<>();
    private Set<URL> loaded = new HashSet<>();

    public ScriptFileWatcher() {
        super(FILE_DIRECTORY);
    }

    public void setScriptEngineManager(ScriptEngineManager manager) {
        this.manager = manager;
    }

    @Override
    public void activate() {
        super.activate();
        importResources(new File(FILE_DIRECTORY));
        startScheduler();
    }

    /**
     * Imports resources from the specified file or directory.
     *
     * @param file the file or directory to import resources from
     */
    private void importResources(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isHidden()) {
                        importResources(f);
                    }
                }
            } else {
                try {
                    URL url = file.toURI().toURL();
                    importFile(url);
                } catch (MalformedURLException e) {
                    // can't happen for the 'file' protocol handler with a correctly formatted URI
                    logger.debug("Can't create a URL", e);
                }
            }
        }
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected Kind<?>[] getWatchEventKinds(Path subDir) {
        return new Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
        File file = path.toFile();
        if (!file.isHidden()) {
            try {
                URL fileUrl = file.toURI().toURL();
                if (kind.equals(ENTRY_DELETE)) {
                    this.removeFile(fileUrl);
                }

                if (file.canRead() && (kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY))) {
                    this.importFile(fileUrl);
                }
            } catch (MalformedURLException e) {
                logger.error("malformed", e);
            }
        }
    }

    private void removeFile(URL url) {
        dequeueUrl(url);
        manager.removeEngine(getScriptIdentifier(url));
        loaded.remove(url);
    }

    private synchronized void importFile(URL url) {
        if (loaded.contains(url)) {
            this.removeFile(url); // if already loaded, remove first
        }

        String scriptType = getScriptType(url);
        if (scriptType != null) {
            if (System.currentTimeMillis() < earliestStart) {
                enqueueUrl(url, scriptType);
            } else {
                if (manager.isSupported(scriptType)) {
                    try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(url.openStream()))) {
                        logger.info("script loading: {}", url.toString());

                        ScriptEngineContainer container = manager.createScriptEngine(scriptType, url.toString());

                        if (container != null) {
                            manager.loadScript(container.getIdentifier(), reader);
                            loaded.add(url);
                            logger.debug("script successfully loaded: {}", url.toString());
                        } else {
                            logger.error("script ERROR, ignore file: {}", url);
                        }
                    } catch (IOException e) {
                        logger.error("url=" + url, e);
                    }
                } else {
                    enqueueUrl(url, scriptType);

                    logger.info("ScriptEngine for {} not available", scriptType);
                }
            }
        }
    }

    private void enqueueUrl(URL url, String scriptType) {
        synchronized (urlsByScriptExtension) {
            Set<URL> set = urlsByScriptExtension.get(scriptType);
            if (set == null) {
                set = new HashSet<URL>();
                urlsByScriptExtension.put(scriptType, set);
            }
            set.add(url);
            logger.debug("in queue: {}", urlsByScriptExtension);
        }
    }

    private void dequeueUrl(URL url) {
        String scriptType = getScriptType(url);

        if (scriptType != null) {
            synchronized (urlsByScriptExtension) {
                Set<URL> set = urlsByScriptExtension.get(scriptType);
                if (set != null) {
                    set.remove(url);
                    if (set.isEmpty()) {
                        urlsByScriptExtension.remove(scriptType);
                    }
                }
                logger.debug("in queue: {}", urlsByScriptExtension);
            }
        }
    }

    private String getScriptType(URL url) {
        String fileName = url.getPath();
        int idx = fileName.lastIndexOf(".");
        if (idx == -1) {
            return null;
        }
        String fileExtension = fileName.substring(idx + 1);

        // ignore known file extensions for "temp" files
        if (fileExtension.equals("txt") || fileExtension.endsWith("~") || fileExtension.endsWith("swp")) {
            return null;
        }
        return fileExtension;
    }

    private String getScriptIdentifier(URL url) {
        return url.toString();
    }

    private void startScheduler() {
        ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(SCRIPT_FILE_WATCH_POOL);
        scheduler.scheduleAtFixedRate(this::checkFiles, INITIAL_DELAY, RECHECK_INTERVAL, TimeUnit.SECONDS);
    }

    private void checkFiles() {
        SortedSet<URL> reimportUrls = new TreeSet<URL>(new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                String f1 = o1.getPath();
                String s1 = f1.substring(f1.lastIndexOf("/") + 1);
                String f2 = o2.getPath();
                String s2 = f2.substring(f2.lastIndexOf("/") + 1);

                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            }
        });

        synchronized (urlsByScriptExtension) {
            HashSet<String> newlySupported = new HashSet<>();
            for (String key : urlsByScriptExtension.keySet()) {
                if (manager.isSupported(key)) {
                    newlySupported.add(key);
                }
            }

            for (String key : newlySupported) {
                reimportUrls.addAll(urlsByScriptExtension.remove(key));
            }
        }

        for (URL url : reimportUrls) {
            importFile(url);
        }
    }
}
