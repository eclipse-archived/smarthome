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
import java.io.IOException;
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
 * This class is able to observe multiple folders for changes and notifies the model repository
 * about every change, so that it can update itself.
 * 
 * @author Kai Kreuzer - Initial contribution and API, Fabio Marini
 *
 */
public class FolderObserver implements ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(FolderObserver.class);

	/* the watch service  */
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
		initializeWatchService();
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

		if (MapUtils.isNotEmpty(folderFileExtMap)) {
			for (String path : folderFileExtMap.keySet()) {
				String pathToWatch = ConfigDispatcher.getConfigFolder() + File.separator + path;

				Path toWatch = Paths.get(pathToWatch);
				try {
					watchService = toWatch.getFileSystem().newWatchService();
					WatchQueueReader reader = new WatchQueueReader(
							watchService, folderFileExtMap, modelRepo);

					Thread qr = new Thread(reader, "Model Dir Watcher");
					qr.start();

					toWatch.register(watchService, ENTRY_CREATE, ENTRY_MODIFY,
							ENTRY_DELETE);
				} catch (IOException e) {
					logger.error(
							"Cannot activate folder watcher for folder '{}': ",
							toWatch, e);
				}
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

		private WatchService watchService;

		private Map<String, String[]> folderFileExtMap = new ConcurrentHashMap<String, String[]>();

		private ModelRepository modelRepo = null;

		public WatchQueueReader(WatchService watchService,
				Map<String, String[]> folderFileExtMap,
				ModelRepository modelRepo) {
			super();
			this.watchService = watchService;
			this.folderFileExtMap = folderFileExtMap;
			this.modelRepo = modelRepo;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			for (;;) {
				WatchKey key = null;
				try {
					key = watchService.take();
				} catch (InterruptedException e) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == OVERFLOW) {
						continue;
					}

					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path name = ev.context();

					checkFolder(name.toString(), kind);
				}

				key.reset();
			}
		}

		@SuppressWarnings("rawtypes")
		private void checkFolder(String filename, Kind kind) {
			String folder = getFolderByExtension(filename);

			if (folder != null) {
				if (modelRepo != null) {
					File file = new File(FoldersUtility.getFolder(folder) + File.separator
							+ filename);
					try {
						if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
							modelRepo.addOrRefreshModel(file.getName(),
									FileUtils.openInputStream(file));
						} else if (kind == ENTRY_DELETE) {
							modelRepo.removeModel(filename);
						}
					} catch (IOException e) {
						logger.warn(
								"Cannot open file '" + file.getAbsolutePath()
										+ "' for reading.", e);
					}
				}
			}
		}

		private String getFolderByExtension(String filename) {
			if (StringUtils.isNotBlank(filename)  && MapUtils.isNotEmpty(folderFileExtMap)) {
				Set<Entry<String, String[]>> entries = folderFileExtMap
						.entrySet();
				Iterator<Entry<String, String[]>> iterator = entries.iterator();
				String extension = FoldersUtility.getExtension(filename);

				while (iterator.hasNext()) {
					Entry<String, String[]> entry = iterator.next();

					if (ArrayUtils.contains(entry.getValue(), extension)) {
						return entry.getKey();
					}
				}
			}

			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
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

				File folder = FoldersUtility.getFolder(foldername);
				if (folder.exists() && folder.isDirectory()) {
					folderFileExtMap.put(foldername, fileExts);
				} else {
					logger.warn(
							"Directory '{}' does not exist in '{}'. Please check your configuration settings!",
							foldername, ConfigDispatcher.getConfigFolder());
				}
			}

			initializeWatchService();
		}
	}
}
