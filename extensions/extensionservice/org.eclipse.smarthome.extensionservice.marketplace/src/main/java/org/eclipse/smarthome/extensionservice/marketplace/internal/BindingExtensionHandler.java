/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.internal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtension;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceExtensionHandler;
import org.eclipse.smarthome.extensionservice.marketplace.MarketplaceHandlerException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MarketplaceExtensionHandler} implementation, which handles bindings as jar files (OSGi bundles) and installs
 * them through the standard OSGi bundle installation mechanism.
 * The information, which installed bundle corresponds to which extension is written to a file in the bundle's data
 * store. It is therefore wiped together with the bundles upon an OSGi "clean".
 * We might want to move this class into a separate bundle in future, when we add support for further extension types.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class BindingExtensionHandler implements MarketplaceExtensionHandler {

    private static final String BINDING_FILE = "installedBindingsMap.csv";

    private final Logger logger = LoggerFactory.getLogger(BindingExtensionHandler.class);

    private Map<String, Long> installedBindings;

    private BundleContext bundleContext;

    protected void activate(BundleContext bundleContext, Map<String, Object> config) {
        this.bundleContext = bundleContext;
        installedBindings = loadInstalledBindingsMap();
    }

    protected void deactivate() {
        this.installedBindings = null;
        this.bundleContext = null;
    }

    @Override
    public boolean supports(MarketplaceExtension ext) {
        // we support only bindings as pure OSGi bundles
        return ext.getType().equals(MarketplaceExtension.EXT_TYPE_BINDING)
                && ext.getPackageFormat().equals(MarketplaceExtension.EXT_FORMAT_BUNDLE);
    }

    @Override
    public boolean isInstalled(MarketplaceExtension ext) {
        return installedBindings.containsKey(ext.getId());
    }

    @Override
    public void install(MarketplaceExtension ext) throws MarketplaceHandlerException {
        String url = ext.getDownloadUrl();
        try {
            Bundle bundle = bundleContext.installBundle(url);
            try {
                bundle.start();
            } catch (BundleException e) {
                logger.warn("Installed bundle, but failed to start it: {}", e.getMessage());
            }
            installedBindings.put(ext.getId(), bundle.getBundleId());
            persistInstalledBindingsMap(installedBindings);
        } catch (BundleException e) {
            throw new MarketplaceHandlerException("Binding cannot be installed: " + e.getMessage());
        }
    }

    @Override
    public void uninstall(MarketplaceExtension ext) throws MarketplaceHandlerException {
        Long id = installedBindings.get(ext.getId());
        if (id != null) {
            Bundle bundle = bundleContext.getBundle(id);
            if (bundle != null) {
                try {
                    bundle.stop();
                    bundle.uninstall();
                    installedBindings.remove(ext.getId());
                    persistInstalledBindingsMap(installedBindings);
                } catch (BundleException e) {
                    throw new MarketplaceHandlerException("Failed deinstalling binding: " + e.getMessage());
                }
            } else {
                // we do not have such a bundle, so let's remove it from our internal map
                installedBindings.remove(ext.getId());
                persistInstalledBindingsMap(installedBindings);
                throw new MarketplaceHandlerException("Id not known.");
            }
        } else {
            throw new MarketplaceHandlerException("Id not known.");
        }
    }

    private Map<String, Long> loadInstalledBindingsMap() {
        File dataFile = bundleContext.getDataFile(BINDING_FILE);
        if (dataFile != null && dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                LineIterator lineIterator = IOUtils.lineIterator(reader);
                Map<String, Long> map = new HashMap<>();
                while (lineIterator.hasNext()) {
                    String line = lineIterator.nextLine();
                    String[] parts = line.split(";");
                    if (parts.length == 2) {
                        try {
                            map.put(parts[0], Long.valueOf(parts[1]));
                        } catch (NumberFormatException e) {
                            logger.debug("Cannot parse '{}' as a number in file {} - ignoring it.", parts[1],
                                    dataFile.getName());
                        }
                    } else {
                        logger.debug("Invalid line in file {} - ignoring it:\n{}", dataFile.getName(), line);
                    }
                }
                return map;
            } catch (IOException e) {
                logger.debug("File '{}' for installed bindings does not exist.", dataFile.getName());
                // ignore and just return an empty map
            }
        }
        return new HashMap<>();
    }

    private synchronized void persistInstalledBindingsMap(Map<String, Long> map) {
        File dataFile = bundleContext.getDataFile(BINDING_FILE);
        if (dataFile != null) {
            try (FileWriter writer = new FileWriter(dataFile)) {
                for (Entry<String, Long> entry : map.entrySet()) {
                    writer.write(entry.getKey() + ";" + entry.getValue() + System.lineSeparator());
                }
            } catch (IOException e) {
                logger.warn("Failed writing file '{}': {}", dataFile.getName(), e.getMessage());
            }
        } else {
            logger.debug("System does not support bundle data files -> not persisting installed binding info");
        }
    }

}
