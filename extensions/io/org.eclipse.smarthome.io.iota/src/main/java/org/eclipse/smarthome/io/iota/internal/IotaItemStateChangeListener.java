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
package org.eclipse.smarthome.io.iota.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.iota.metadata.IotaMetadataRegistryChangeListener;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jota.IotaAPI;
import jota.dto.response.GetNodeInfoResponse;

/**
 * The {@link IotaItemStateChangeListener} listens for changes to the state of registered items.
 * Each item wishing to publish its state on the Tangle has a seed associated to its UID.
 * A custom seed may be given to some item. This way, any ESH instance is able to select
 * which item's state to share on which channel.
 *
 * @author Theo Giovanna - Initial Contribution
 */
public class IotaItemStateChangeListener implements StateChangeListener {

    private final Logger logger = LoggerFactory.getLogger(IotaItemStateChangeListener.class);
    private IotaAPI bridge;
    private final HashMap<String, JsonObject> jsonObjectBySeed = new HashMap<>();
    private final HashMap<String, String> seedByUID = new HashMap<>();
    private final HashMap<String, Debouncer> debouncerBySeed = new HashMap<>();
    private final HashMap<String, IotaUtilsImpl> utilsBySeed = new HashMap<>();
    private final HashMap<String, String> privateKeyBySeed = new HashMap<>();
    private IotaMetadataRegistryChangeListener metadataRegistryChangeListener;

    @Override
    public void stateChanged(@NonNull Item item, @NonNull State oldState, @NonNull State newState) {
        metadataRegistryChangeListener.getMetadataRegistry().getAll().forEach(metadata -> {
            if (metadata.getUID().getItemName().equals(item.getName())) {
                String seed = seedByUID.get(item.getUID());
                if (jsonObjectBySeed.containsKey(seed)) {
                    /**
                     * Entries already exists. Updating the value in the json array
                     */
                    JsonObject oldEntries = jsonObjectBySeed.get(seed);
                    JsonObject newEntries = addNewEntryToJson(item, newState, oldEntries);
                    jsonObjectBySeed.put(seed, newEntries);
                } else {
                    /**
                     * New entry, creating a new json object
                     */
                    jsonObjectBySeed.put(seed,
                            addNewEntryToJson(item, newState, new Gson().fromJson("{\"Items\":[]}", JsonObject.class)));
                }

                String mode = metadata.getConfiguration().get("mode").toString();
                Debouncer debouncer = debouncerBySeed.get(seed);

                if (debouncer != null) {
                    /**
                     * If several items publish on the same channel, the debounce mechanism bellow
                     * makes sure the data are published only once if no item has requested an update
                     * within 1 second
                     */
                    debouncer.debounce(IotaItemStateChangeListener.class, () -> {
                        String key = mode.equals("restricted") ? privateKeyBySeed.get(seed) : null;
                        utilsBySeed.get(seed).publishState(jsonObjectBySeed.get(seed).get("Items"), mode, key);
                    }, 1000, TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    @Override
    public void stateUpdated(@NonNull Item item, @NonNull State state) {
        /**
         * Not needed. State has been updated but is no different from the latest push on the Tangle.
         * Note: for testing purpose, you can call stateChanged(item, state, state) to re-publish
         * data to the Tangle.
         */
        // stateChanged(item, state, state);
    }

    public void setBridge(IotaAPI bridge) {
        this.bridge = bridge;
        if (this.bridge != null) {
            GetNodeInfoResponse getNodeInfoResponse = this.bridge.getNodeInfo();
            logger.debug("Iota connexion success: {}", getNodeInfoResponse);
        }
    }

    public void setMetadataRegistryChangeListener(IotaMetadataRegistryChangeListener metadataRegistryChangeListener) {
        this.metadataRegistryChangeListener = metadataRegistryChangeListener;
    }

    /**
     * Add entry to the current JsonObject json
     *
     * @param item  the item to add
     * @param state the state to add
     * @param json  the JsonObject
     * @return the new JsonObject
     */
    public JsonObject addNewEntryToJson(@NonNull Item item, @NonNull State state, JsonObject json) {
        if (json.get("Items").getAsJsonArray().size() == 0) {
            json.get("Items").getAsJsonArray().add(createNewJsonEntry(item, state));
        } else {
            for (Iterator<JsonElement> it = json.get("Items").getAsJsonArray().iterator(); it.hasNext();) {
                JsonElement el = it.next();
                String name = el.getAsJsonObject().get("Name").toString().replace("\"", "");
                if (name.equals(item.getName())) {
                    // Item already tracked. Removing it to update value
                    it.remove();
                }
            }
            json.get("Items").getAsJsonArray().add(createNewJsonEntry(item, state));
        }
        return json;
    }

    /**
     * Create a new json struct for a given item and a given state
     *
     * @param item  the item
     * @param state the state
     * @return the json struct
     */
    public JsonObject createNewJsonEntry(Item item, State state) {
        JsonObject newEntry = new JsonObject();
        JsonObject itemState = new JsonObject();
        String topic = item.getCategory() == null ? "" : item.getCategory();
        itemState.addProperty("Topic", topic);
        itemState.addProperty("State", state.toFullString());
        itemState.addProperty("Time", Instant.now().toString());
        newEntry.addProperty("Name", item.getName());
        newEntry.add("Status", itemState);
        return newEntry;
    }

    /**
     * Cleaning json struct: an item has been removed in the Paper UI, therefore its state
     * will not be published to the Tangle anymore
     *
     * @param item
     */
    public void removeEntryFromJson(@NonNull Item item) {
        String seed = seedByUID.get(item.getUID());
        if (seed != null && !seed.isEmpty()) {
            for (Iterator<JsonElement> it = jsonObjectBySeed.get(seed).get("Items").getAsJsonArray().iterator(); it
                    .hasNext();) {
                JsonElement el = it.next();
                String name = el.getAsJsonObject().get("Name").toString().replace("\"", "");
                if (name.equals(item.getName())) {
                    it.remove();
                }
            }
        }
    }

    public JsonObject getJsonObjectBySeed(String seed) {
        return jsonObjectBySeed.get(seed);
    }

    public void addJsonObjectBySeed(String seed, JsonObject json) {
        jsonObjectBySeed.put(seed, json);
    }

    public void addSeedByUID(String UID, String seed) {
        seedByUID.put(UID, seed);
    }

    public void addDebouncerBySeed(String seed, Debouncer debouncer) {
        debouncerBySeed.put(seed, debouncer);
    }

    public void addUtilsBySeed(String seed, IotaUtilsImpl utils) {
        utilsBySeed.put(seed, utils);
    }

    public IotaUtilsImpl getUtilsBySeed(String seed) {
        return utilsBySeed.get(seed);
    }

    public String getPrivateKeyBySeed(String seed) {
        return privateKeyBySeed.get(seed);
    }

    public void addPrivateKeyBySeed(String seed, String privateKey) {
        privateKeyBySeed.put(seed, privateKey);
    }

}
