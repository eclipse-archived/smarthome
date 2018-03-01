package org.eclipse.smarthome.config.discovery.usbserial.internal;

import static com.google.common.collect.Sets.newHashSet;
import static org.eclipse.smarthome.config.discovery.DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscovery;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Unit tests for the {@link UsbSerialDiscoveryService}.
 *
 * @author Henning Sudbrock - initial contribution
 */
public class UsbSerialDiscoveryServiceTest extends JavaOSGiTest {

    @Test
    public void testSettingUsbSerialDiscoveryStartsBackgroundDiscoveryIfEnabled()
            throws InterruptedException, IOException {
        setBackgroundDiscovery(true);

        UsbSerialDiscovery usbSerialDiscovery = mock(UsbSerialDiscovery.class);
        registerService(usbSerialDiscovery);

        verify(usbSerialDiscovery, times(1)).startBackgroundScanning();
    }

    @Test
    public void testSettingUsbSerialDiscoveryDoesNotStartBackgroundDiscoveryIfDisabled()
            throws IOException, InterruptedException {
        setBackgroundDiscovery(false);

        UsbSerialDiscovery usbSerialDiscovery = mock(UsbSerialDiscovery.class);
        registerService(usbSerialDiscovery);

        verify(usbSerialDiscovery, never()).startBackgroundScanning();
    }

    @Test
    public void testRegistersAsUsbserialDiscoveryListener() {
        UsbSerialDiscovery usbSerialDiscovery = mock(UsbSerialDiscovery.class);
        registerService(usbSerialDiscovery);

        verify(usbSerialDiscovery, times(1)).registerDiscoveryListener(getService(UsbSerialDiscoveryService.class));
    }

    @Test
    public void testUnregistersAsUsbserialDiscoveryListener() {
        UsbSerialDiscovery usbSerialDiscovery = mock(UsbSerialDiscovery.class);
        registerService(usbSerialDiscovery);

        unregisterService(usbSerialDiscovery);

        verify(usbSerialDiscovery, times(1)).unregisterDiscoveryListener(getService(UsbSerialDiscoveryService.class));
    }

    @Test
    public void testSupportedThingTypesAreRetrievedFromDiscoveryParticipants() {
        UsbSerialDiscoveryService service = getService(UsbSerialDiscoveryService.class);

        // with no discovery participants available, no thing types are supported.
        assertThat(service.getSupportedThingTypes(), is(empty()));

        // with two discovery participants available, the thing types supported by them are supported.
        ThingTypeUID thingTypeA = new ThingTypeUID("a:b:c");
        ThingTypeUID thingTypeB = new ThingTypeUID("d:e:f");
        ThingTypeUID thingTypeC = new ThingTypeUID("g:h:i");

        UsbSerialDiscoveryParticipant discoveryParticipantA = mock(UsbSerialDiscoveryParticipant.class);
        when(discoveryParticipantA.getSupportedThingTypeUIDs()).thenReturn(newHashSet(thingTypeA, thingTypeB));
        registerService(discoveryParticipantA);

        UsbSerialDiscoveryParticipant discoveryParticipantB = mock(UsbSerialDiscoveryParticipant.class);
        when(discoveryParticipantB.getSupportedThingTypeUIDs()).thenReturn(newHashSet(thingTypeB, thingTypeC));
        registerService(discoveryParticipantB);

        assertThat(service.getSupportedThingTypes(), containsInAnyOrder(thingTypeA, thingTypeB, thingTypeC));
    }

    @Test
    public void testThingsAreActuallyDiscovered() {
        UsbSerialDiscoveryService service = getService(UsbSerialDiscoveryService.class);

        // register one discovery listener
        DiscoveryListener discoveryListener = mock(DiscoveryListener.class);
        service.addDiscoveryListener(discoveryListener);

        // register two discovery participants
        UsbSerialDiscoveryParticipant discoveryParticipantA = mock(UsbSerialDiscoveryParticipant.class);
        registerService(discoveryParticipantA);

        UsbSerialDiscoveryParticipant discoveryParticipantB = mock(UsbSerialDiscoveryParticipant.class);
        registerService(discoveryParticipantB);

        // when no discovery participant supports a newly discovered device, no device is discovered
        when(discoveryParticipantA.createResult(any())).thenReturn(null);
        when(discoveryParticipantB.createResult(any())).thenReturn(null);
        service.usbSerialDeviceDiscovered(generateDeviceInfo());
        verify(discoveryListener, never()).thingDiscovered(any(), any());

        // when only the first discovery participant supports a newly discovered device, the device is discovered
        UsbSerialDeviceInformation deviceInfoA = generateDeviceInfo();
        DiscoveryResult discoveryResultA = mock(DiscoveryResult.class);
        when(discoveryParticipantA.createResult(deviceInfoA)).thenReturn(discoveryResultA);
        service.usbSerialDeviceDiscovered(deviceInfoA);
        verify(discoveryListener, times(1)).thingDiscovered(service, discoveryResultA);

        // when only the second discovery participant supports a newly discovered device, the device is also discovered
        UsbSerialDeviceInformation deviceInfoB = generateDeviceInfo();
        DiscoveryResult discoveryResultB = mock(DiscoveryResult.class);
        when(discoveryParticipantA.createResult(deviceInfoB)).thenReturn(discoveryResultB);
        service.usbSerialDeviceDiscovered(deviceInfoB);
        verify(discoveryListener, times(1)).thingDiscovered(service, discoveryResultB);
    }

    @Test
    public void testDiscoveredThingsAreRemoved() {
        UsbSerialDiscoveryService service = getService(UsbSerialDiscoveryService.class);

        // register one discovery listener
        DiscoveryListener discoveryListener = mock(DiscoveryListener.class);
        service.addDiscoveryListener(discoveryListener);

        // register one discovery participant
        UsbSerialDiscoveryParticipant discoveryParticipant = mock(UsbSerialDiscoveryParticipant.class);
        registerService(discoveryParticipant);

        // when the discovery participant does not support a removed device, no discovery result is removed
        when(discoveryParticipant.createResult(any())).thenReturn(null);
        service.usbSerialDeviceRemoved(generateDeviceInfo());
        verify(discoveryListener, never()).thingRemoved(any(), any());

        // when the first discovery participant supports a removed device, the discovery result is removed
        UsbSerialDeviceInformation deviceInfo = generateDeviceInfo();
        ThingUID thingUID = mock(ThingUID.class);
        when(discoveryParticipant.getThingUID(deviceInfo)).thenReturn(thingUID);
        service.usbSerialDeviceRemoved(deviceInfo);
        verify(discoveryListener, times(1)).thingRemoved(service, thingUID);
    }

    private void setBackgroundDiscovery(boolean status) throws IOException, InterruptedException {
        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
        Configuration configuration = configAdmin.getConfiguration("discovery.usbserial");
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.valueOf(status));
        configuration.update(properties);

        // wait until the configuration is actually set in the discovery service
        waitForAssert(() -> {
            UsbSerialDiscoveryService service = getService(UsbSerialDiscoveryService.class);
            assertThat(service, is(not(nullValue())));
            assertThat(service.isBackgroundDiscoveryEnabled(), is(status));
        }, 1000, 100);
    }

    private final AtomicInteger deviceInfoCounter = new AtomicInteger(0);

    public UsbSerialDeviceInformation generateDeviceInfo() {
        int i = deviceInfoCounter.getAndIncrement();
        return new UsbSerialDeviceInformation(i, i, "serialNumber-" + i, "manufacturer-" + i, "product-" + i,
                "ttyUSB" + i);
    }

}