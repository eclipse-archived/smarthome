/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.tradfri.discovery;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.*;
import static org.eclipse.smarthome.binding.tradfri.internal.config.TradfriGatewayConfig.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.binding.tradfri.internal.discovery.TradfriDiscoveryParticipant;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Tests for {@link TradfriDiscoveryParticipant}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class TradfriDiscoveryParticipantOSGITest extends JavaOSGiTest {

    private MDNSDiscoveryParticipant discoveryParticipant;

    @Mock
    private ServiceInfo tradfriGateway;

    @Mock
    private ServiceInfo otherDevice;

    @Before
    public void setUp() {
        initMocks(this);
        discoveryParticipant = getService(MDNSDiscoveryParticipant.class, TradfriDiscoveryParticipant.class);

        when(tradfriGateway.getType()).thenReturn("_coap._udp.local.");
        when(tradfriGateway.getName()).thenReturn("gw:12-34-56-78-90-ab");
        when(tradfriGateway.getHostAddresses()).thenReturn(new String[] { "192.168.0.5" });
        when(tradfriGateway.getPort()).thenReturn(1234);
        when(tradfriGateway.getPropertyString("version")).thenReturn("1.1");

        when(otherDevice.getType()).thenReturn("_coap._udp.local.");
        when(otherDevice.getName()).thenReturn("something");
        when(otherDevice.getHostAddresses()).thenReturn(new String[] { "192.168.0.5" });
        when(otherDevice.getPort()).thenReturn(1234);
        when(otherDevice.getPropertyString("version")).thenReturn("1.1");
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().size(), is(1));
        assertThat(discoveryParticipant.getSupportedThingTypeUIDs().iterator().next(), is(GATEWAY_TYPE_UID));
    }

    @Test
    public void correctThingUID() {
        assertThat(discoveryParticipant.getThingUID(tradfriGateway),
                is(new ThingUID("tradfri:gateway:gw1234567890ab")));
    }

    @Test
    public void validDiscoveryResult() {
        DiscoveryResult result = discoveryParticipant.createResult(tradfriGateway);

        assertNotNull(result);
        assertThat(result.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("1.1"));
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID(), is(new ThingUID("tradfri:gateway:gw1234567890ab")));
        assertThat(result.getThingTypeUID(), is(GATEWAY_TYPE_UID));
        assertThat(result.getBridgeUID(), is(nullValue()));
        assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("IKEA of Sweden"));
        assertThat(result.getProperties().get(CONFIG_HOST), is("192.168.0.5"));
        assertThat(result.getProperties().get(CONFIG_PORT), is(1234));
        assertThat(result.getRepresentationProperty(), is(CONFIG_HOST));
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
