package org.eclipse.smarthome.io.transport.upnp.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService
import org.eclipse.smarthome.io.transport.upnp.UpnpIOServiceImpl
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.jupnp.UpnpService
import org.jupnp.controlpoint.ControlPoint
import org.jupnp.controlpoint.SubscriptionCallback
import org.jupnp.model.meta.DeviceIdentity
import org.jupnp.model.meta.LocalDevice
import org.jupnp.model.meta.LocalService
import org.jupnp.model.types.DeviceType
import org.jupnp.model.types.ServiceId
import org.jupnp.model.types.ServiceType
import org.jupnp.model.types.UDAServiceId
import org.jupnp.model.types.UDN
import org.jupnp.registry.Registry

class UpnpIOServiceTest extends OSGiTest {

    def UDNString = "UDN"
    def UDN = new UDN(UDNString)
    def UDNString2 = "UDN2"
    def UDN_2 = new UDN(UDNString2)
    def SERVICE_ID = "serviceId"
    def SERVICE_ID_2 = "serviceId2"
    def ACTION_ID = "actionId"
    def DEVICE_TYPE = "deviceType"
    def SERVICE_TYPE = "serviceType"

    def upnpIoParticipant = [ getUDN: { UDNString } ] as UpnpIOParticipant
    def upnpIoParticipant2 = [ getUDN: { UDNString2 } ] as UpnpIOParticipant

    UpnpIOServiceImpl upnpIoService

    @Before
    void setup() {
        def deviceIdentity = new DeviceIdentity(UDN)
        def deviceType = new DeviceType(UDAServiceId.DEFAULT_NAMESPACE, DEVICE_TYPE, 1)
        def serviceType = new ServiceType(UDAServiceId.DEFAULT_NAMESPACE, SERVICE_TYPE)

        def serviceId = new ServiceId(UDAServiceId.DEFAULT_NAMESPACE, SERVICE_ID)
        def service = new LocalService(serviceType, serviceId, null, null)
        def device = new LocalDevice(deviceIdentity, deviceType, null, service)

        def serviceId2 = new ServiceId(UDAServiceId.DEFAULT_NAMESPACE, SERVICE_ID_2)
        def service2 = new LocalService(serviceType, serviceId2, null, null)
        def device2 = new LocalDevice(deviceIdentity, deviceType, null, service2)

        def upnpRegistry = [
            getDevice: { UDN udn, boolean rootOnly ->
                if (udn.equals(UDN)) {
                    device
                } else {
                    device2
                }
            }
        ] as Registry

        def controlPoint = [
            execute: { SubscriptionCallback callback ->
            }
        ] as ControlPoint

        def upnpServiceMock = [
            getRegistry: { upnpRegistry },
            getControlPoint: { controlPoint }
        ] as UpnpService
        registerService(upnpServiceMock)
        upnpIoService = getService(UpnpIOService.class)
    }

    @After
    void teardown() {
        unregisterMocks()
    }

    @Test
    void 'test participant is registered'() {
        assertThat upnpIoService.isRegistered(upnpIoParticipant), is(true)
    }

    @Test
    void 'test that internal maps are empty'() {
        assertThat upnpIoService.isRegistered(upnpIoParticipant), is(true)
        assertThatEveryThingIsEmpty()
    }

    @Test
    void 'test that participant is registered'() {
        upnpIoService.registerParticipant(upnpIoParticipant)
        assertThat upnpIoService.participants.keySet().size(), is(1)
        assertThat upnpIoService.participants.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.pollingJobs.keySet().isEmpty(), is(true)
        assertThat upnpIoService.currentStates.keySet().isEmpty(), is(true)
        assertThat upnpIoService.subscriptionCallbacks.keySet().isEmpty(), is(true)
    }

    @Test
    void 'test that a status listener is added'() {
        upnpIoService.addStatusListener(upnpIoParticipant, SERVICE_ID, ACTION_ID, 60)
        assertThat upnpIoService.participants.keySet().size(), is(1)
        assertThat upnpIoService.participants.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.pollingJobs.keySet().size(), is(1)
        assertThat upnpIoService.pollingJobs.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.currentStates.keySet().size(), is(1)
        assertThat upnpIoService.currentStates.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.subscriptionCallbacks.keySet().isEmpty(), is(true)

        upnpIoService.removeStatusListener(upnpIoParticipant)
        assertThatEveryThingIsEmpty()
    }

    @Test
    void 'test that Subscriptions are added'() {
        upnpIoService.addSubscription(upnpIoParticipant, SERVICE_ID, 60)
        assertThat upnpIoService.participants.keySet().size(), is(1)
        assertThat upnpIoService.participants.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.pollingJobs.keySet().isEmpty(), is(true)
        assertThat upnpIoService.currentStates.keySet().isEmpty(), is(true)
        assertThat upnpIoService.subscriptionCallbacks.size(), is(1)

        upnpIoService.addSubscription(upnpIoParticipant2, SERVICE_ID_2, 60)
        assertThat upnpIoService.participants.keySet().size(), is(2)
        assertThat upnpIoService.participants.containsKey(upnpIoParticipant), is(true)
        assertThat upnpIoService.pollingJobs.keySet().isEmpty(), is(true)
        assertThat upnpIoService.currentStates.keySet().isEmpty(), is(true)
        assertThat upnpIoService.subscriptionCallbacks.size(), is(2)

        upnpIoService.removeSubscription(upnpIoParticipant, SERVICE_ID)
        upnpIoService.unregisterParticipant(upnpIoParticipant)
        assertThat upnpIoService.participants.keySet().size(), is(1)
        assertThat upnpIoService.participants.containsKey(upnpIoParticipant2), is(true)
        assertThat upnpIoService.pollingJobs.keySet().isEmpty(), is(true)
        assertThat upnpIoService.currentStates.keySet().isEmpty(), is(true)
        assertThat upnpIoService.subscriptionCallbacks.size(), is(1)

        upnpIoService.removeSubscription(upnpIoParticipant2, SERVICE_ID_2)
        upnpIoService.unregisterParticipant(upnpIoParticipant2)
        assertThatEveryThingIsEmpty()
    }

    private assertThatEveryThingIsEmpty() {
        assertThat upnpIoService.participants.keySet().isEmpty(), is(true)
        assertThat upnpIoService.pollingJobs.keySet().isEmpty(), is(true)
        assertThat upnpIoService.currentStates.keySet().isEmpty(), is(true)
        assertThat upnpIoService.subscriptionCallbacks.keySet().isEmpty(), is(true)
    }
}
