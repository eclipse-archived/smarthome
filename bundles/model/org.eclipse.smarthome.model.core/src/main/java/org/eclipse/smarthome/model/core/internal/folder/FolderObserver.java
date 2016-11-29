/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core.internal.folder;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.service.AbstractWatchQueueReader;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.eclipse.smarthome.model.core.ModelParser;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This class is able to observe multiple folders for changes and notifies the
 * model repository about every change, so that it can update itself.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Fabio Marini - Refactoring to use WatchService
 *
 */
public class FolderObserver extends AbstractWatchService implements ManagedService {

    /* the model repository is provided as a service */
    private ModelRepository modelRepo = null;

    /* map that stores a list of valid file extensions for each folder */
    private final Map<String, String[]> folderFileExtMap = new ConcurrentHashMap<String, String[]>();

    /* set of file extensions for which we have parsers already registered */
    private static Set<String> parsers = new HashSet<>();

    /* set of files that have been ignored due to a missing parser */
    private static Set<File> ignoredFiles = new HashSet<>();

    public void setModelRepository(ModelRepository modelRepo) {
        this.modelRepo = modelRepo;
    }

    public void unsetModelRepository(ModelRepository modelRepo) {
        this.modelRepo = null;
    }

    protected void addModelParser(ModelParser modelParser) {
        parsers.add(modelParser.getExtension());
        processIgnoredFiles(modelParser.getExtension());
    }

    protected void removeModelParser(ModelParser modelParser) {
        parsers.remove(modelParser.getExtension());
    }

    @Override
    public void activate() {
    }

    private void processIgnoredFiles(String extension) {
        HashSet<File> clonedSet = new HashSet<>(ignoredFiles);
        for (File file : clonedSet) {
            if (extension.equals(getExtension(file.getPath()))) {
                checkFile(modelRepo, file, ENTRY_CREATE);
                ignoredFiles.remove(file);
            }
        }
    }

    @Override
    protected AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch,
            Map<WatchKey, Path> registeredKeys) {
        return new WatchQueueReader(watchService, toWatch, registeredKeys, folderFileExtMap, modelRepo);
    }

    @Override
    protected String getSourcePath() {
        return ConfigConstants.getConfigFolder();
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected WatchKey registerDirectory(Path subDir) throws IOException {
        if (subDir != null && MapUtils.isNotEmpty(folderFileExtMap)) {
            String folderName = subDir.getFileName().toString();
            if (folderFileExtMap.containsKey(folderName)) {
                return subDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            }
        }
        return null;
    }

    private static class WatchQueueReader extends AbstractWatchQueueReader {

        private Map<String, String[]> folderFileExtMap = new ConcurrentHashMap<String, String[]>();

        private ModelRepository modelRepo = null;

        public WatchQueueReader(WatchService watchService, Path dirToWatch, Map<WatchKey, Path> registeredKeys,
                Map<String, String[]> folderFileExtMap, ModelRepository modelRepo) {
            super(watchService, dirToWatch, registeredKeys);

            this.folderFileExtMap = folderFileExtMap;
            this.modelRepo = modelRepo;
        }

        @Override
        protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
            File toCheck = getFileByFileExtMap(folderFileExtMap, path.getFileName().toString());
            if (toCheck != null) {
                checkFile(modelRepo, toCheck, kind);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public synchronized void updated(Dictionary config) throws ConfigurationException {
        if (config != null) {
            // necessary to check removed models
            Map<String, String[]> previousFolderFileExtMap = new ConcurrentHashMap<String, String[]>(folderFileExtMap);

            // make sure to clear the caches first
            folderFileExtMap.clear();

            Enumeration keys = config.keys();
            while (keys.hasMoreElements()) {

                String foldername = (String) keys.nextElement();
                if (foldername.equals("service.pid")) {
                    continue;
                }

                String[] fileExts = ((String) config.get(foldername)).split(",");

                File folder = getFile(foldername);
                if (folder.exists() && folder.isDirectory()) {
                    folderFileExtMap.put(foldername, fileExts);
                } else {
                    logger.warn("Directory '{}' does not exist in '{}'. Please check your configuration settings!",
                            foldername, ConfigConstants.getConfigFolder());
                }
            }

            notifyUpdateToModelRepo(previousFolderFileExtMap);
            initializeWatchService();
        }
    }

    private void notifyUpdateToModelRepo(Map<String, String[]> previousFolderFileExtMap) {
        checkDeletedModels(previousFolderFileExtMap);
        if (MapUtils.isNotEmpty(folderFileExtMap)) {
            Iterator<String> iterator = folderFileExtMap.keySet().iterator();
            while (iterator.hasNext()) {
                String folderName = iterator.next();

                final String[] validExtension = folderFileExtMap.get(folderName);
                if (validExtension != null && validExtension.length > 0) {
                    File folder = getFile(folderName);

                    File[] files = folder.listFiles(new FileExtensionsFilter(validExtension));
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            checkFile(modelRepo, file, ENTRY_CREATE);
                        }
                    }
                }
            }
        }
    }

    private void checkDeletedModels(Map<String, String[]> previousFolderFileExtMap) {
        if (MapUtils.isNotEmpty(previousFolderFileExtMap)) {
            List<String> modelsToRemove = new LinkedList<String>();
            if (MapUtils.isNotEmpty(folderFileExtMap)) {
                Set<String> folders = previousFolderFileExtMap.keySet();
                for (String folder : folders) {
                    if (!folderFileExtMap.containsKey(folder)) {
                        Iterable<String> models = modelRepo.getAllModelNamesOfType(folder);
                        if (models != null) {
                            modelsToRemove.addAll(Lists.newLinkedList(models));
                        }
                    }
                }
            } else {
                Set<String> folders = previousFolderFileExtMap.keySet();
                for (String folder : folders) {
                    synchronized (FolderObserver.class) {
                        Iterable<String> models = modelRepo.getAllModelNamesOfType(folder);
                        if (models != null) {
                            modelsToRemove.addAll(Lists.newLinkedList(models));
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(modelsToRemove)) {
                for (String modelToRemove : modelsToRemove) {
                    synchronized (FolderObserver.class) {
                        modelRepo.removeModel(modelToRemove);
                    }
                }
            }
        }
    }

    protected class FileExtensionsFilter implements FilenameFilter {

        private String[] validExtensions;

        public FileExtensionsFilter(String[] validExtensions) {
            this.validExtensions = validExtensions;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (validExtensions != null && validExtensions.length > 0) {
                for (String extension : validExtensions) {
                    if (name.toLowerCase().endsWith("." + extension)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    private static void checkFile(ModelRepository modelRepo, final File file, Kind kind) {
        if (modelRepo != null && file != null) {
            try {
                synchronized (FolderObserver.class) {
                    if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && file != null) {
                        if (parsers.contains(getExtension(file.getName()))) {
                            modelRepo.addOrRefreshModel(file.getName(), FileUtils.openInputStream(file));
                        } else {
                            ignoredFiles.add(file);
                        }
                    } else if (kind == ENTRY_DELETE && file != null) {
                        modelRepo.removeModel(file.getName());
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(FolderObserver.class)
                        .warn("Cannot open file '" + file.getAbsolutePath() + "' for reading.", e);
            }
        }
    }

    private static File getFileByFileExtMap(Map<String, String[]> folderFileExtMap, String filename) {
        if (StringUtils.isNotBlank(filename) && MapUtils.isNotEmpty(folderFileExtMap)) {

            String extension = getExtension(filename);

            if (StringUtils.isNotBlank(extension)) {
                Set<Entry<String, String[]>> entries = folderFileExtMap.entrySet();
                Iterator<Entry<String, String[]>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Entry<String, String[]> entry = iterator.next();

                    if (ArrayUtils.contains(entry.getValue(), extension)) {
                        return new File(getFile(entry.getKey()) + File.separator + filename);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the {@link File} object for the given filename. <br />
     * It must be contained in the configuration folder
     *
     * @param filename
     *            the file name to get the {@link File} for
     * @return the corresponding {@link File}
     */
    private static File getFile(String filename) {
        File folder = new File(ConfigConstants.getConfigFolder() + File.separator + filename);

        return folder;
    }

    /**
     * Returns the extension of the given file
     *
     * @param filename
     *            the file name to get the extension
     * @return the file's extension
     */
    public static String getExtension(String filename) {
        String fileExt = filename.substring(filename.lastIndexOf(".") + 1);

        return fileExt;
    }
}
