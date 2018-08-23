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
package org.eclipse.smarthome.binding.homematic.test.util;

import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDatapoint;
import org.eclipse.smarthome.binding.homematic.internal.model.HmDevice;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;

/**
 * Class that contains static helper methods to create several kinds of objects
 * (e.g. {@link ThingUID}, {@link HmDevice}, {@link HmDatapoint}) related to a
 * homematic dimmer.
 * 
 * @author Florian Stolte - Initial Contribution
 * 
 */
public class DimmerHelper {

    public static HmDevice createDimmerHmDevice() {
        return createDimmerHmDevice("CCU2");
    }

    public static HmDevice createDimmerHmDevice(String gatewayType) {
        HmDevice hmDevice = new HmDevice("ABC12345678", null, "HM-LC-Dim1-Pl3", gatewayType, "", "1");
        hmDevice.setName("Homematic Dimmer");
        return hmDevice;
    }

    public static HmChannel createDimmerHmChannel() {
        HmChannel hmChannel = new HmChannel("HM-LC-Dim1-Pl3", 1);
        hmChannel.setDevice(createDimmerHmDevice());

        return hmChannel;
    }

    public static HmChannel createDimmerDummyChannel() {
        HmChannel hmChannel = new HmChannel("HM-LC-Dim1-Pl3", -1);
        hmChannel.setDevice(createDimmerHmDevice());

        return hmChannel;
    }

    public static HmDatapoint createDimmerHmDatapoint() {
        HmDatapoint hmDatapoint = new HmDatapoint();
        hmDatapoint.setName("DIMMER");
        hmDatapoint.setChannel(createDimmerHmChannel());

        return hmDatapoint;
    }

    public static ThingTypeUID createDimmerThingTypeUID() {
        return new ThingTypeUID("homematic:HM-LC-Dim1-Pl3");
    }

    public static ThingUID createDimmerThingUID() {
        return new ThingUID(createDimmerThingTypeUID(), "ABC12345678");
    }

    public static Thing createDimmerThing() {
        return ThingBuilder.create(createDimmerThingTypeUID(), createDimmerThingUID()).build();
    }
}
