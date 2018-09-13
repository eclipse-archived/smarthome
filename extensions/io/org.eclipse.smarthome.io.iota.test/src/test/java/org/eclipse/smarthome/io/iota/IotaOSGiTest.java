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
package org.eclipse.smarthome.io.iota;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.io.iota.internal.IotaIo;
import org.eclipse.smarthome.io.iota.internal.IotaSeedGenerator;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@link IotaOSGiTest} provides tests cases for OSGi services used in the iota bundle.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaOSGiTest extends JavaOSGiTest {

    private MetadataRegistry metadataRegistry;
    private ItemRegistry itemRegistry;
    private GenericItem item1;
    private GenericItem item2;
    private Metadata metadata1;
    private Metadata metadata2;
    private Metadata metadata3;

    private final IotaIo iota = new IotaIo();
    private final Map<String, Object> metadataConfiguration1 = new HashMap<>();
    private final Map<String, Object> metadataConfiguration2 = new HashMap<>();
    private final IotaSeedGenerator gen = new IotaSeedGenerator();
    private final String seed1 = gen.getNewSeed();
    private final String seed2 = gen.getNewSeed();

    @Before
    public void setUp() {
        registerVolatileStorageService();

        metadataRegistry = getService(MetadataRegistry.class);
        itemRegistry = getService(ItemRegistry.class);
        item1 = new NumberItem("item1");
        item1.setState(new QuantityType<Temperature>(Double.parseDouble("1"), SIUnits.CELSIUS));
        item2 = new NumberItem("item2");
        item2.setState(new QuantityType<Dimensionless>(Double.parseDouble("85"), SmartHomeUnits.PERCENT));

        metadataConfiguration1.put("seed", seed1);
        metadataConfiguration1.put("mode", "public");
        metadataConfiguration2.put("seed", seed2);
        metadataConfiguration2.put("mode", "restricted");

        metadata1 = new Metadata(new MetadataKey("iota", item1.getName()), "yes", metadataConfiguration1);
        metadata2 = new Metadata(new MetadataKey("iota", item2.getName()), "yes", metadataConfiguration1);
        metadata3 = new Metadata(new MetadataKey("iota", item1.getName()), "yes", metadataConfiguration2);

        iota.setItemRegistry(itemRegistry);
        iota.setMetadataRegistry(metadataRegistry);
        iota.modified(new HashMap<>());

    }

    @Test
    public void shouldUpdateJsonStructOnItemAddition() {
        /**
         * Tell the bundle to track updates on this item given iota metadata
         */
        itemRegistry.add(item1);
        metadataRegistry.add(metadata1);
        /**
         * Check that the state listener picked the update and prepared the json data for publishing
         */
        assertEquals(1, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
        /**
         * Add a new item with iota metadata
         */
        itemRegistry.add(item2);
        metadataRegistry.add(metadata2);
        /**
         * Since the metadata for this item is configured on the same seed as item 1, the json data should update its
         * size to 2
         */
        assertEquals(2, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
    }

    @Test
    public void shouldUpdateJsonStructOnItemDeletion() {
        /**
         * Tell the bundle to track updates on this item given iota metadata
         */
        itemRegistry.add(item1);
        metadataRegistry.add(metadata1);
        assertEquals(1, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
        /**
         * Remove the item and check that its state is not part of the json struct anymore
         */
        itemRegistry.remove(item1.getName(), true);
        assertEquals(0, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
    }

    @Test
    public void shouldUpdateJsonStructOnMetadataUpdate() {
        /**
         * Tell the bundle to track updates on this item given iota metadata
         */
        itemRegistry.add(item1);
        metadataRegistry.add(metadata1);
        assertEquals(1, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
        /**
         * Update metadata linked to an item from "yes" to "no" and check that its state is not part of the json struct
         * anymore
         */
        metadata1 = new Metadata(new MetadataKey("iota", item1.getName()), "no", metadataConfiguration1);
        metadataRegistry.update(metadata1);
        assertEquals(0, iota.getMetadataChangeListener().getItemStateChangeListener().getJsonObjectBySeed(seed1)
                .get("Items").getAsJsonArray().size());
    }

    @Test
    public void shouldAddKeyIfExisting() {
        /**
         * Verify that password for restricted MAM stream is picked up in metadata
         */
        itemRegistry.add(item1);
        metadataConfiguration2.put("key", "password");
        metadata3 = new Metadata(new MetadataKey("iota", item1.getName()), "yes", metadataConfiguration2);
        metadataRegistry.add(metadata3);
        assertFalse(iota.getMetadataChangeListener().getItemStateChangeListener().getPrivateKeyBySeed(seed2).isEmpty());
        assertEquals("password",
                iota.getMetadataChangeListener().getItemStateChangeListener().getPrivateKeyBySeed(seed2));
    }

    @SuppressWarnings("null")
    @Test
    public void shouldGenerateKeyIfNonExisting() {
        /**
         * Verify that a password is generated for restricted MAM stream if non is provided, and that it is added to the
         * metadata configuration
         */
        itemRegistry.add(item1);
        metadataRegistry.add(metadata3);
        assertFalse(iota.getMetadataChangeListener().getItemStateChangeListener().getPrivateKeyBySeed(seed2).isEmpty());
        assertTrue(metadataRegistry.get(metadata3.getUID()).getConfiguration().containsKey("key"));
    }

    @SuppressWarnings("null")
    @Test
    public void metadataShouldUpdate() {
        metadataRegistry.add(metadata1);
        iota.getMetadataChangeListener().updateMetadata(metadata1, "updatedSeed", null);
        assertTrue(metadataRegistry.get(metadata1.getUID()).getConfiguration().get("seed").equals("updatedSeed"));
        iota.getMetadataChangeListener().updateMetadata(metadata1, null, "newKey");
        assertTrue(metadataRegistry.get(metadata1.getUID()).getConfiguration().get("key").equals("newKey"));
    }

}
