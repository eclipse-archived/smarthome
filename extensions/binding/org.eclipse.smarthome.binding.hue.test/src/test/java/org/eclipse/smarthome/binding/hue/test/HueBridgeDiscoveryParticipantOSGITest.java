/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.smarthome.binding.hue.internal.discovery.HueBridgeDiscoveryParticipant;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;

/**
 * Tests for {@link HueBridgeDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Thomas Höfer - Added representation
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueBridgeDiscoveryParticipantOSGITest extends JavaOSGiTest {

    UpnpDiscoveryParticipant discoveryParticipant;

    RemoteDevice hueDevice;
    RemoteDevice otherDevice;

    @Before
    public void setUp() {
        discoveryParticipant = getService(UpnpDiscoveryParticipant.class, HueBridgeDiscoveryParticipant.class);
        assertThat(discoveryParticipant, is(notNullValue()));

        try {
            final RemoteService remoteService = null;

            hueDevice = new RemoteDevice(
                    new RemoteDeviceIdentity(new UDN("123"), 60, new URL("http://hue"), null, null),
                    new DeviceType("namespace", "type"),
                    new DeviceDetails(new URL("http://1.2.3.4/"), "Hue Bridge", new ManufacturerDetails("Philips"),
                            new ModelDetails("Philips hue bridge"), "serial123", "upc", null),
                    remoteService);

            otherDevice = new RemoteDevice(
                    new RemoteDeviceIdentity(new UDN("567"), 60, new URL("http://acme"), null, null),
                    new DeviceType("namespace", "type"), new DeviceDetails("Some Device",
                            new ManufacturerDetails("Taiwan"), new ModelDetails("$%&/"), "serial567", "upc"),
                    remoteService);
        } catch (final ValidationException | MalformedURLException ex) {
            Assert.fail("Internal test error.");
        }
    }

    @After
    public void cleanUp() {
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().size(), is(1));
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().iterator().next(), is(THING_TYPE_BRIDGE));
    }

    @Test
    public void correctThingUID() {
        assertThat(discoveryParticipant.getThingUID(hueDevice), is(new ThingUID("hue:bridge:serial123")));
    }

    @Test
    public void validDiscoveryResult() {
        final DiscoveryResult result = discoveryParticipant.createResult(hueDevice);
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID(), is(new ThingUID("hue:bridge:serial123")));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_BRIDGE));
        assertThat(result.getBridgeUID(), is(nullValue()));
        assertThat(result.getProperties().get(HOST), is("1.2.3.4"));
        assertThat(result.getProperties().get(SERIAL_NUMBER), is("serial123"));
        assertThat(result.getRepresentationProperty(), is(SERIAL_NUMBER));
    }

    @Test
    public void noThingUIDForUnknownDevice() {
        assertThat(discoveryParticipant.getThingUID(otherDevice), is(nullValue()));
    }

    @Test
    public void noDiscoveryResultForUnknownDevice() {
        assertThat(discoveryParticipant.createResult(otherDevice), is(nullValue()));
    }
}
