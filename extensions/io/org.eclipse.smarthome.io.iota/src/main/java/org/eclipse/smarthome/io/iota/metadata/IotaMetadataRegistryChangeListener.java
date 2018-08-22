/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.io.iota.metadata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.io.iota.internal.Debouncer;
import org.eclipse.smarthome.io.iota.internal.IotaItemRegistryChangeListener;
import org.eclipse.smarthome.io.iota.internal.IotaItemStateChangeListener;
import org.eclipse.smarthome.io.iota.internal.IotaSeedGenerator;
import org.eclipse.smarthome.io.iota.internal.IotaSettings;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jota.IotaAPI;

/**
 * The {@link IotaMetadataRegistryChangeListener} listens for changes to the metadata registry and register the OSGi
 * service.
 *
 * @author Theo Giovanna - Initial Contribution
 */
@Component(service = RegistryChangeListener.class, configurationPid = "org.eclipse.smarthome.iota.metadata.IotaMetadataRegistryChangeListener", immediate = true)
public class IotaMetadataRegistryChangeListener implements RegistryChangeListener<Metadata> {

    private MetadataRegistry metadataRegistry;
    private final Logger logger = LoggerFactory.getLogger(IotaMetadataRegistryChangeListener.class);
    private IotaItemRegistryChangeListener itemRegistryChangeListener;
    private IotaSettings settings;
    private final IotaItemStateChangeListener itemStateChangeListener = new IotaItemStateChangeListener();
    private final IotaSeedGenerator seedGenerator = new IotaSeedGenerator();
    private IotaAPI bridge;

    @SuppressWarnings({ "unused", "null" })
    @Override
    public void added(Metadata metadata) {
        /**
         * Adds a state listener to the item if it contains the right metadata
         */
        Item item;
        try {
            itemStateChangeListener.setMetadataRegistryChangeListener(this);
            if (CollectionUtils.isNotEmpty(itemRegistryChangeListener.getItemRegistry().getAll())) {
                item = itemRegistryChangeListener.getItemRegistry().getItem(metadata.getUID().getItemName());
                if (item instanceof GenericItem) {
                    if (metadata.getValue() != null) {
                        if (metadata.getValue().equals("yes")) {
                            if (!metadata.getConfiguration().isEmpty()) {

                                /**
                                 * Adds a new entry in the hashmap: maps the item UID to a specific seed on
                                 * which messages will be broadcasted. Either a new one is created, or an
                                 * existing one is used. If a new seed is used, a corresponding debouncing instance
                                 * and utils instance are created.
                                 */
                                String seed;
                                if (metadata.getConfiguration().get("seed") == null) {
                                    logger.debug("A new seed will be generated for item {}", item.getUID());
                                    seed = seedGenerator.getNewSeed();
                                    addNewSeedToHashMaps(item, seed);
                                    updateMetadata(metadata, seed, null);
                                } else {

                                    /**
                                     * Uses an existing seed to publish this item states
                                     */
                                    seed = metadata.getConfiguration().get("seed").toString();
                                    if (seed != null && !seed.isEmpty() && seed.length() == 81) {
                                        logger.debug("An existing seed will be used for item {}", item.getUID());

                                        /**
                                         * Checks if some ESH instance is already publishing on this seed.
                                         * If so, associating the item UID to it, otherwise creating a new
                                         * instance of IOTA Utils for publishing.
                                         */
                                        if (itemStateChangeListener.getUtilsBySeed(seed) != null) {
                                            itemStateChangeListener.addSeedByUID(item.getUID(), seed);
                                        } else {
                                            addNewSeedToHashMaps(item, seed);

                                            /**
                                             * -1 indicates the JS script to recompute itself the depth
                                             * of the merkle root tree by fetching all the data from the
                                             * initial root.
                                             */
                                            itemStateChangeListener.getUtilsBySeed(seed).setStart(-1);
                                        }

                                    } else {
                                        logger.debug("Invalid seed for item {}. Generating a new one", item.getUID());
                                        seed = seedGenerator.getNewSeed();
                                        addNewSeedToHashMaps(item, seed);
                                        updateMetadata(metadata, seed, null);
                                    }
                                }

                                /**
                                 * If restricted mode was selected, the private key is saved, otherwise generated.
                                 */
                                if (metadata.getConfiguration().get("mode").equals("restricted") && !seed.isEmpty()) {
                                    if (metadata.getConfiguration().get("key") != null) {
                                        logger.debug("An existing key will be used for item {}", item.getUID());
                                        String inputKey = metadata.getConfiguration().get("key").toString();
                                        if (inputKey != null && !inputKey.isEmpty()) {
                                            itemStateChangeListener.addPrivateKeyBySeed(seed, inputKey);
                                        }
                                    } else {
                                        logger.debug("Invalid key for item {}. Generating a new one", item.getUID());
                                        String key = seedGenerator.getNewPrivateKey();
                                        itemStateChangeListener.addPrivateKeyBySeed(seed, key);
                                        updateMetadata(metadata, null, key);
                                    }
                                }

                                logger.debug("Iota state listener added for item: {}", item.getName());
                                ((GenericItem) item).addStateChangeListener(itemStateChangeListener);

                                /**
                                 * Publish the state
                                 */
                                itemStateChangeListener.stateChanged(item, item.getState(), item.getState());
                            }
                        }
                    }
                }
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Exception happened: {}", e);
        }
    }

    @Override
    public void removed(Metadata metadata) {
        // not needed
    }

    @Override
    public void updated(Metadata oldMetadata, Metadata metadata) {
        logger.debug("Iota metadata updated");
        if (metadata.getValue().equals("no")) {
            try {
                Item item = itemRegistryChangeListener.getItemRegistry().getItem(oldMetadata.getUID().getItemName());
                itemRegistryChangeListener.removed(item);
            } catch (ItemNotFoundException e) {
                logger.debug("Exception happened: {}", e);
            }
        }
    }

    /**
     * Updates the hashmaps in the {@link IotaStateListener} class.
     *
     * @param item the item
     * @param seed the seed
     */
    private void addNewSeedToHashMaps(Item item, String seed) {
        itemStateChangeListener.addSeedByUID(item.getUID(), seed);
        itemStateChangeListener.addDebouncerBySeed(seed, new Debouncer());
        itemStateChangeListener.addUtilsBySeed(seed,
                new IotaUtilsImpl(bridge.getProtocol(), bridge.getHost(), Integer.parseInt(bridge.getPort()), seed, 0));
    }

    /**
     * Update the given metadata with the new properties
     *
     * @param metadata the metadata to update
     * @param seed     the seed
     * @param key      the key
     */
    public void updateMetadata(Metadata metadata, @Nullable String seed, @Nullable String key) {
        Map<String, Object> newConfiguration = new HashMap<>();
        metadata.getConfiguration().forEach(newConfiguration::put);
        if (seed != null && key == null) {
            newConfiguration.put("seed", seed);
        }
        if (seed == null && key != null) {
            newConfiguration.put("key", key);
        }
        metadataRegistry.update(new Metadata(metadata.getUID(), "yes", newConfiguration));
    }

    public void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
        this.metadataRegistry.addRegistryChangeListener(this);
    }

    public void stop() {
        if (metadataRegistry != null) {
            metadataRegistry.getAll().forEach(this::removed);
            metadataRegistry.removeRegistryChangeListener(this);
        }
    }

    public IotaSettings getSettings() {
        return settings;
    }

    public IotaItemStateChangeListener getItemStateChangeListener() {
        return itemStateChangeListener;
    }

    public MetadataRegistry getMetadataRegistry() {
        return metadataRegistry;
    }

    public void setSettings(IotaSettings settings) {
        this.settings = settings;
    }

    public void setItemRegistryChangeListener(IotaItemRegistryChangeListener itemRegistryChangeListener) {
        this.itemRegistryChangeListener = itemRegistryChangeListener;
    }

    public void setBridge(IotaAPI bridge) {
        this.bridge = bridge;
    }

}
