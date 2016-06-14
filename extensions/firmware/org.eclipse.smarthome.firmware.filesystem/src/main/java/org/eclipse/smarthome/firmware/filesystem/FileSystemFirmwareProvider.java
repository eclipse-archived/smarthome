/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.firmware.filesystem;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.service.AbstractWatchQueueReader;
import org.eclipse.smarthome.core.service.AbstractWatchService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.firmware.Firmware;
import org.eclipse.smarthome.core.thing.firmware.FirmwareProvider;
import org.eclipse.smarthome.core.thing.firmware.FirmwareUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * <p>
 * A simple {@link FirmwareProvider}, that retrieves {@link Firmware} from {@code userdata/firmware}.
 * Each firmware consists of two files which must be named using the pattern shown below:
 * A properties file containing the firmware metadata and an image file with the firmware image.
 * Files are in sub folders according their bindingId/{@link ThingType}.
 * </p>
 * <p>
 * Each file name must follow the rules for a Java class name. For a specific firmware, all files must
 * have the same name. At least the following files are required (called FW1 in this example):
 * </p>
 * <ul>
 * <li>An image file containing the firmware image; the name of the image file is contained in the properties file</li>
 * <li>{@code FW1.properties} - containing the complete firmware meta data with locale specific properties in English
 * </li>
 * </ul>
 * <p>
 * The property file can contain the following values (version and image are mandatory):
 *
 * <pre>
 * version = V1.2-b18
 * image = imageV12b18.bin
 * md5hash = <image MD5 hash code>
 * model = Model A
 * vendor = Company Ltd.
 * description = This is a camera.
 * changelog = This is the change log
 * onlineChangelog = http://www.company.com
 * </pre>
 * </p>
 * <p>
 * There could be locale specific property files overwriting the locale specific properties, e.g.
 * {@code FW1_de.properties}:
 *
 * <pre>
 * description = Das ist eine Kamera.
 * changelog = Das ist das Changelog.
 * onlineChangelog = http://www.company.de
 * </pre>
 * </p>
 *
 * @author Andre Fuechsel - Initial contribution
 * @author Thomas Höfer - Changes due to renaming withContent to withInputStream of firmware builder
 * @author Dimitar Ivanov - The firmware file mapping is updated according to the watched directory events
 */
public class FileSystemFirmwareProvider extends AbstractWatchService implements FirmwareProvider {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String FIRMWARE_DIR = "/firmware";
    private static final PathMatcher PROPERTY_FILE_MATCHER = FileSystems.getDefault()
            .getPathMatcher("glob:**.properties");

    private String rootPath;

    /**
     * Concurrent mapping between FirmwareUid and the simple (not relative and not fully qualified) name of the file in
     * the watched directory
     */
    private final Map<FirmwareUID, String> firmwareFiles = new ConcurrentHashMap<>();

    @Override
    public void activate() {
        this.rootPath = getFirmwareRootFolder();
        scanRootDirectory();
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        firmwareFiles.clear();
    }

    /**
     * @return the root folder, where all the firmware files are stored.
     */
    protected static String getFirmwareRootFolder() {
        return ConfigConstants.getUserDataFolder() + FIRMWARE_DIR;
    }

    /**
     * Scans the root directory and creates a map of filenames for each {@link FirmwareUID}. All the operations on files
     * in the watched directory will be handled accordingly in the mapping while the provider is active.
     */
    protected void scanRootDirectory() {
        firmwareFiles.clear();
        try {
            final Path root = Paths.get(rootPath);
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path pathToFile, BasicFileAttributes attrs) throws IOException {
                    // In the beginning all the files are considered newly created
                    processCreatedFile(root.relativize(pathToFile));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("Error reading directory {}", rootPath);
        }
    }

    @Override
    public Firmware getFirmware(FirmwareUID firmwareUID) {
        return getFirmware(firmwareUID, Locale.getDefault());
    }

    @Override
    public Firmware getFirmware(FirmwareUID firmwareUID, Locale locale) {
        Preconditions.checkArgument(firmwareUID != null);
        if (locale == null) {
            locale = Locale.getDefault();
        }

        String filename = firmwareFiles.get(firmwareUID);
        if (filename == null) {
            logger.debug("Cannot find firmware {}", firmwareUID);
            return null;
        }

        try {
            return getFirmware(firmwareUID, filename, locale);
        } catch (IOException e) {
            logger.debug("Cannot find firmware " + firmwareUID);
        }

        return null;
    }

    private Firmware getFirmware(FirmwareUID firmwareUID, String filename, Locale locale) throws IOException {
        ThingTypeUID thingTypeUID = firmwareUID.getThingTypeUID();
        URL[] urls = { Paths.get(rootPath, thingTypeUID.getBindingId(), thingTypeUID.getId()).toUri().toURL() };
        ResourceBundle rb = ResourceBundle.getBundle(filename, locale, new URLClassLoader(urls));

        InputStream firmwareImage = getFirmwareImage(firmwareUID, rb.getString("image"));

        Firmware.Builder builder = new Firmware.Builder(firmwareUID)
                .withDescription(getResourceAsString(rb, "description")).withModel(getResourceAsString(rb, "model"))
                .withVendor(getResourceAsString(rb, "vendor")).withChangelog(getResourceAsString(rb, "changelog"))
                .withPrerequisiteVersion(getResourceAsString(rb, "prerequisiteVersion")).withInputStream(firmwareImage)
                .withMd5Hash(getResourceAsString(rb, "md5hash"));

        String onlineChangeLog = getResourceAsString(rb, "onlineChangelog");
        if (onlineChangeLog != null) {
            builder.withOnlineChangelog(new URL(onlineChangeLog));
        }

        return builder.build();
    }

    private InputStream getFirmwareImage(FirmwareUID firmwareUID, String filename) throws IOException {
        ThingTypeUID thingTypeUID = firmwareUID.getThingTypeUID();
        Path imageFile = Paths.get(rootPath, thingTypeUID.getBindingId(), thingTypeUID.getId(), filename);

        if (Files.notExists(imageFile)) {
            throw new IOException("File " + imageFile.getFileName().toString() + " cannot be found");
        }

        return FileUtils.openInputStream(imageFile.toFile());
    }

    private String getResourceAsString(ResourceBundle rb, String key) {
        return rb.containsKey(key) ? rb.getString(key) : null;
    }

    @Override
    public Set<Firmware> getFirmwares(ThingTypeUID thingTypeUID) {
        return getFirmwares(thingTypeUID, Locale.getDefault());
    }

    @Override
    public Set<Firmware> getFirmwares(final ThingTypeUID thingTypeUID, Locale locale) {
        Preconditions.checkArgument(thingTypeUID != null);
        if (locale == null) {
            locale = Locale.getDefault();
        }

        final Locale myLocale = locale;

        Path thingTypePath = Paths.get(rootPath, thingTypeUID.getBindingId(), thingTypeUID.getId());
        final Set<Firmware> firmwareSet = new HashSet<>();

        try {
            Files.walkFileTree(thingTypePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (PROPERTY_FILE_MATCHER.matches(file)) {
                        FirmwareUID firmwareUID = getFirmwareUIDFromFileName(thingTypeUID,
                                getBaseName(file.getFileName().toString()));
                        if (firmwareUID != null) {
                            Firmware firmware = getFirmware(firmwareUID, myLocale);
                            if (firmware != null) {
                                firmwareSet.add(firmware);
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.debug("Cannot find firmwares for thing type " + thingTypeUID);
        }

        return firmwareSet;
    }

    private FirmwareUID getFirmwareUIDFromFileName(ThingTypeUID thingTypeUID, String filename) {
        for (FirmwareUID firmwareUID : firmwareFiles.keySet()) {
            boolean isSameThingTypeUid = firmwareUID.getThingTypeUID().equals(thingTypeUID);
            boolean isSameFile = firmwareFiles.get(firmwareUID).equals(filename);
            if (isSameThingTypeUid && isSameFile) {
                return firmwareUID;
            }
        }

        logger.trace("Cannot find a FirmwareUID for file {} and thing type {}", filename, thingTypeUID.getId());
        return null;
    }

    private String getBaseName(String filename) {
        return filename.substring(0, filename.lastIndexOf("."));
    }

    @Override
    protected AbstractWatchQueueReader buildWatchQueueReader(WatchService watchService, Path toWatch,
            Map<WatchKey, Path> registeredKeys) {
        return new AbstractWatchQueueReader(watchService, toWatch, registeredKeys, false) {

            @Override
            protected void processWatchEvent(WatchEvent<?> event, Kind<?> kind, Path path) {
                logger.trace("FileSystemWatchEvent: {} for file {}", kind.name(), path.toString());
                try {
                    if (kind.equals(ENTRY_CREATE)) {
                        processCreatedFile(path);
                    } else if (kind.equals(ENTRY_MODIFY)) {
                        processModifiedFile(path);
                    } else if (kind.equals(ENTRY_DELETE)) {
                        logger.debug(
                                "The firmware meta information file '{}' has been deleted. Cleaning up old mappings",
                                path.toString());
                        removeFromFirmwareFiles(path);
                    } else {
                        logger.debug("Unknown message of watch kind {} received for file {}", kind.name(),
                                path.toString());
                    }
                } catch (IOException e) {
                    logger.warn("Error while processing firmware meta-information file " + path.toString(), e);
                }
            }
        };
    }

    private void processCreatedFile(Path path) throws IOException {
        FirmwareUID firmwareUID = processFile(path);
        if (firmwareUID != null) {
            if (firmwareFiles.containsKey(firmwareUID)) {
                logger.warn(
                        "Conflict detected: newly created file '{}' tries to override the meta information for firmware {} , mapped to file '{}'",
                        path.toString(), firmwareUID, firmwareFiles.get(firmwareUID));
            } else {
                String fileName = getBaseName(path.getFileName().toString());
                firmwareFiles.put(firmwareUID, fileName);
                logger.info("New firmware meta informaton file detected in '{}' for firmware {}", path.toString(),
                        firmwareUID);
            }
        }
    }

    private void processModifiedFile(Path path) throws IOException {
        FirmwareUID firmwareUID = processFile(path);
        if (firmwareUID != null) {
            String currentFileName = getBaseName(path.getFileName().toString());
            if (firmwareFiles.containsKey(firmwareUID)) {
                // The information for the current firmware was updated
                String mappedFileName = firmwareFiles.get(firmwareUID);
                if (mappedFileName.equals(currentFileName)) {
                    logger.debug("The firmware meta information for {} was updated within '{}'", firmwareUID,
                            currentFileName);
                } else {
                    logger.warn(
                            "Conflict detected: the modified file '{}' tries to override the meta information for firmware {} , mapped to file '{}'",
                            path.toString(), firmwareUID, mappedFileName);
                }
            } else {
                // New firmware version has been detected from a modified file
                boolean isOldMappingRemoved = removeFromFirmwareFiles(path);
                if (isOldMappingRemoved) {
                    // Already mapped file has its firmware version modified
                    logger.info(
                            "The firmware meta information file '{}' has its firmware version modified. Created new mapping from {} to file '{}'. Cleaning up the old mappings.",
                            path.toString(), firmwareUID, path.toString());
                } else {
                    // Existing file has been modified to provide new firmware meta information
                    logger.info("The file '{}' is currently providing firmware meta information for firmware '{}'",
                            path.toString(), firmwareUID);
                }
                firmwareFiles.put(firmwareUID, currentFileName);
            }
        }
    }

    /**
     * Removes the firmware mapping to the file, denoted by the path.
     * 
     * @param path - the path to the file to be removed from the firmwares mapping
     * 
     * @return <code>true</code> if any mapping has been removed and <code>false</code> otherwise
     */
    private boolean removeFromFirmwareFiles(Path path) {
        FirmwareUID uid = null;

        String fileName = getBaseName(path.getFileName().toString());
        String thingTypeUid = getThingTypeUid(path);

        for (Map.Entry<FirmwareUID, String> entry : firmwareFiles.entrySet()) {
            if (entry.getValue().equals(fileName) && entry.getKey().getThingTypeUID().getId().equals(thingTypeUid)) {
                uid = entry.getKey();
                break;
            }
        }

        if (uid != null) {
            logger.debug("Removing mapping from firmware {} to file '{}'", uid, fileName);
            firmwareFiles.remove(uid);
            return true;
        }

        return false;
    }

    /**
     * Processes the file on the given path. If it matches the {@link #PROPERTY_FILE_MATCHER} and has a 'version'
     * property, a corresponding {@link FirmwareUID} will be created for it
     * 
     * @param path - the path to the file to be processed
     * @return the created {@link FirmwareUID} and <code>null</code> if the file cannot be processed
     * @throws IOException if a problem occurs while loading the properties of the file on the given path
     * @throws FileNotFoundException if the provided path is pointing to not existing file
     */
    private FirmwareUID processFile(Path path) throws IOException, FileNotFoundException {
        String version = getFirmwareVersion(path);
        if (version != null) {
            // Extract firmware information from the current file as it contains version property
            ThingTypeUID thingTypeUID = new ThingTypeUID(getBindingId(path), getThingTypeUid(path));
            FirmwareUID firmwareUID = new FirmwareUID(thingTypeUID, version);
            return firmwareUID;
        } else {
            logger.trace("File {} does not contain a version entry", path.getFileName().toString());
            return null;
        }
    }

    private String getBindingId(Path path) {
        return path.getParent().getParent().getFileName().toString();
    }

    private String getThingTypeUid(Path path) {
        return path.getParent().getFileName().toString();
    }

    /**
     * Retrieves the firmware version from the file, referenced with given path. If the version is not present within
     * the file, a null value will be returned. This way, a non-null value will indicate that the file is a firmware
     * meta-information file.
     * 
     * @param path - path to file to be processed.
     * @return the version of the file if it is present, <code>null</code> otherwise.
     * @throws IOException if a problem occurs while loading the properties of the file on the given path
     * @throws FileNotFoundException if the provided path is pointing to not existing file
     */
    private String getFirmwareVersion(Path path) throws FileNotFoundException, IOException {
        if (PROPERTY_FILE_MATCHER.matches(path)) {
            Properties properties = new Properties();
            // Open load and close
            FileInputStream fis = new FileInputStream(rootPath + File.separatorChar + path.toString());
            properties.load(fis);
            fis.close();
            String version = properties.getProperty("version");
            return version;
        }
        return null;
    }

    @Override
    protected String getSourcePath() {
        return FileSystemFirmwareProvider.getFirmwareRootFolder();
    }

    @Override
    protected boolean watchSubDirectories() {
        return true;
    }

    @Override
    protected WatchKey registerDirectory(Path subDir) throws IOException {
        WatchKey registrationKey = subDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        return registrationKey;
    }
}
