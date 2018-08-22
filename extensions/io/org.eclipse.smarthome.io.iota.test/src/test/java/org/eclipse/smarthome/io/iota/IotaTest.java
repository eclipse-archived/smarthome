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

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.iota.internal.IotaItemStateChangeListener;
import org.eclipse.smarthome.io.iota.internal.IotaSeedGenerator;
import org.eclipse.smarthome.io.iota.utils.IotaUtilsImpl;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link IotaTest} provides tests cases for Iota IO classes. The tests provide mocks for supporting entities using
 * Mockito.
 *
 * @author Theo Giovanna - Initial contribution
 */
public class IotaTest {

    private IotaUtilsImpl utils;
    private IotaItemStateChangeListener itemStateChangeListener;
    private Item item1;
    private Item item2;
    private State state1;
    private State state2;

    @Before
    public void setUp() {
        itemStateChangeListener = new IotaItemStateChangeListener();
        utils = new IotaUtilsImpl();
        item1 = new NumberItem("item1");
        item2 = new NumberItem("item2");
        state1 = new QuantityType<Temperature>(Double.parseDouble("10"), SIUnits.CELSIUS);
        state2 = new QuantityType<Dimensionless>(Double.parseDouble("85"), SmartHomeUnits.PERCENT);
        ((GenericItem) item1).setState(state1);
        ((GenericItem) item2).setState(state2);
    }

    @Test
    public void generatedSeedShouldBeValid() {
        IotaSeedGenerator gen = new IotaSeedGenerator();
        assertTrue(utils.checkSeed(gen.getNewSeed()));
    }

    @Test
    public void newStateShouldAddToExistingStates() {
        JsonObject existingStates = new Gson().fromJson("{\"Items\":[]}", JsonObject.class);
        // No items have been added yet
        assertEquals(0, existingStates.get("Items").getAsJsonArray().size());
        // Adding state 1
        existingStates = itemStateChangeListener.addNewEntryToJson(item1, item1.getState(), existingStates);
        // Size of the array should now be 1
        assertEquals(1, existingStates.get("Items").getAsJsonArray().size());
        // Adding state 2
        existingStates = itemStateChangeListener.addNewEntryToJson(item2, item2.getState(), existingStates);
        // Size of the array should now be 2
        assertEquals(2, existingStates.get("Items").getAsJsonArray().size());
        // Adding the same state twice should not change the array size, but should update the element
        existingStates = itemStateChangeListener.addNewEntryToJson(item2, item2.getState(), existingStates);
        // Size of the array should still be 2
        assertEquals(2, existingStates.get("Items").getAsJsonArray().size());
    }

    @Test
    public void existingStateShouldDelete() {
        JsonObject existingStates = new Gson().fromJson("{\"Items\":[]}", JsonObject.class);
        // Adding state 1
        existingStates = itemStateChangeListener.addNewEntryToJson(item1, item1.getState(), existingStates);
        // Size of the array should now be 1
        assertEquals(1, existingStates.get("Items").getAsJsonArray().size());
        IotaSeedGenerator gen = new IotaSeedGenerator();
        String seed = gen.getNewSeed();
        itemStateChangeListener.addSeedByUID(item1.getUID(), seed);
        itemStateChangeListener.addJsonObjectBySeed(seed, existingStates);
        // Removing the state
        itemStateChangeListener.removeEntryFromJson(item1);
        // Size of the array should now be 0
        assertEquals(0, existingStates.get("Items").getAsJsonArray().size());
    }

}
