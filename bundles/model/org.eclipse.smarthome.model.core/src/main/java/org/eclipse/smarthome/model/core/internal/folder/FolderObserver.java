/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core.internal.folder;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigDispatcher;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is able to observe multiple folders for changes and notifies the
 * model repository about every change, so that it can update itself.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Fabio Marini - Refactoring to use WatchService
 * 
 */
public class FolderObserver implements ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(FolderObserver.class);

	/* the watch service */
	private WatchService watchService;

	/* the model repository is provided as a service */
	private ModelRepository modelRepo = null;

	/* map that stores a list of valid file extensions for each folder */
	private final Map<String, String[]> folderFileExtMap = new ConcurrentHashMap<String, String[]>();

	public void setModelRepository(ModelRepository modelRepo) {
		this.modelRepo = modelRepo;
	}

	public void unsetModelRepository(ModelRepository modelRepo) {
		this.modelRepo = null;
	}

	public void activate() {
		//the initialization is to do when the service configuration has been read and update. See ManagedService#update(Dictionary)
		//initializeWatchService();
	}

	public void deactivate() {
		stopWatchService();
	}

	private void initializeWatchService() {
		if (watchService != null) {
			try {
				watchService.close();
			} catch (IOException e) {
				logger.warn("Cannot deactivate folder watcher", e);
			}
		}

		String pathToWatch = ConfigDispatcher.getConfigFolder();
		if (StringUtils.isNotBlank(pathToWatch)
				&& MapUtils.isNotEmpty(folderFileExtMap)) {
			try {
				watchService = FileSystems.getDefault().newWatchService();

				Set<String> folders = folderFileExtMap.keySet();
				Iterator<String> iterator = folders.iterator();
				while (iterator.hasNext()) {
					Path dir = Paths.get(ConfigDispatcher.getConfigFolder()
							+ File.separator + iterator.next());

					WatchKey key = dir.register(watchService, ENTRY_CREATE,
							ENTRY_DELETE, ENTRY_MODIFY);

					WatchQueueReader reader = new WatchQueueReader(key,
							folderFileExtMap, modelRepo);
					Thread qr = new Thread(reader, "Model Dir Watcher");
					qr.start();
				}

			} catch (IOException e) {
				logger.error("Cannot activate folder watcher for folder ", e);
			}
		}
	}

	private void stopWatchService() {
		try {
			watchService.close();
		} catch (IOException e) {
			logger.warn("Cannot deactivate folder watcher", e);
		}
		watchService = null;
	}

	private static class WatchQueueReader implements Runnable {

		private WatchKey key;

		private Map<String, String[]> folderFileExtMap = new ConcurrentHashMap<String, String[]>();

		private ModelRepository modelRepo = null;

		public WatchQueueReader(WatchKey key,
				Map<String, String[]> folderFileExtMap,
				ModelRepository modelRepo) {
			super();
			this.key = key;
			this.folderFileExtMap = folderFileExtMap;
			this.modelRepo = modelRepo;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			for (;;) {
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == OVERFLOW) {
						continue;
					}

					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path name = ev.context();

					File toCheck = getFileByFileExtMap(folderFileExtMap,
							name.toString());
					if (toCheck != null) {
						checkFile(modelRepo, toCheck, kind);
					}
				}

				key.reset();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public synchronized void updated(Dictionary config)
			throws ConfigurationException {
		if (config != null) {
			// make sure to clear the caches first
			folderFileExtMap.clear();

			Enumeration keys = config.keys();
			while (keys.hasMoreElements()) {

				String foldername = (String) keys.nextElement();
				if (foldername.equals("service.pid"))
					continue;

				String[] fileExts = ((String) config.get(foldername))
						.split(",");

				File folder = getFolder(foldername);
				if (folder.exists() && folder.isDirectory()) {
					folderFileExtMap.put(foldername, fileExts);
				} else {
					logger.warn(
							"Directory '{}' does not exist in '{}'. Please check your configuration settings!",
							foldername, ConfigDispatcher.getConfigFolder());
				}
			}

			notifyUpdateToModelRepo();
			initializeWatchService();
		}
	}

	private void notifyUpdateToModelRepo() {
		if (MapUtils.isNotEmpty(folderFileExtMap)) {
			Iterator<String> iterator = folderFileExtMap.keySet().iterator();
			while (iterator.hasNext()) {
				String folderName = iterator.next();

				final String[] validExtension = folderFileExtMap
						.get(folderName);
				if (validExtension != null && validExtension.length > 0) {
					File folder = getFolder(folderName);

					File[] files = folder.listFiles(new FileExtensionsFilter(
							validExtension));
					if (files != null && files.length > 0) {
						for (File file : files) {
							checkFile(modelRepo, file, ENTRY_CREATE);
						}
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
	private static void checkFile(ModelRepository modelRepo, File file,
			Kind kind) {
		if (modelRepo != null && file != null) {
			try {
				synchronized (FolderObserver.class) {
					if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
							&& file != null) {
						modelRepo.addOrRefreshModel(file.getName(),
								FileUtils.openInputStream(file));
					} else if (kind == ENTRY_DELETE) {
						modelRepo.removeModel(file.getName());
					}
				}
			} catch (IOException e) {
				logger.warn("Cannot open file '" + file.getAbsolutePath()
						+ "' for reading.", e);
			}
		}
	}

	private static File getFileByFileExtMap(
			Map<String, String[]> folderFileExtMap, String filename) {
		if (StringUtils.isNotBlank(filename)
				&& MapUtils.isNotEmpty(folderFileExtMap)) {

			String extension = getExtension(filename);

			if (StringUtils.isNotBlank(extension)) {
				Set<Entry<String, String[]>> entries = folderFileExtMap
						.entrySet();
				Iterator<Entry<String, String[]>> iterator = entries.iterator();
				while (iterator.hasNext()) {
					Entry<String, String[]> entry = iterator.next();

					if (ArrayUtils.contains(entry.getValue(), extension)) {
						return new File(getFolder(entry.getKey())
								+ File.separator + filename);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns the {@link File} object for a given filename It builds the file's
	 * full path
	 * 
	 * @param filename
	 *            the file name to get the {@link File} for
	 * @return the corresponding {@link File}
	 */
	public static File getFolder(String filename) {
		File folder = new File(ConfigDispatcher.getConfigFolder()
				+ File.separator + filename);

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
