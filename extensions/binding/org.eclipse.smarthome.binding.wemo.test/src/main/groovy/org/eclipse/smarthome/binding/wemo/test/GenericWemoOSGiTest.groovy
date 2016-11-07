/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import groovy.xml.Namespace

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler
import org.eclipse.smarthome.binding.wemo.internal.WemoHandlerFactory
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService
import org.eclipse.smarthome.io.transport.upnp.UpnpIOServiceImpl
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.jupnp.UpnpService
import org.jupnp.mock.MockUpnpService
import org.jupnp.model.meta.DeviceDetails
import org.jupnp.model.meta.ManufacturerDetails
import org.jupnp.model.meta.ModelDetails
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.meta.RemoteDeviceIdentity
import org.jupnp.model.meta.RemoteService
import org.jupnp.model.types.DeviceType
import org.jupnp.model.types.ServiceId
import org.jupnp.model.types.ServiceType
import org.jupnp.model.types.UDN
import org.osgi.service.http.HttpService

import com.google.common.collect.ImmutableSet

/**
 * Generic test class for all Wemo related tests that contains methods and constants used across the different test classes
 *
 * @author Svilen Valkanov - Initial contribution
 */
public abstract class GenericWemoOSGiTest extends OSGiTest{

    static final def DEVICE_MANUFACTURER = "Belkin"

    //This port is included in the run configuration
    def ORG_OSGI_SERVICE_HTTP_PORT = 8080

    //Thing information
    def TEST_THING_ID = "TestThing"

    //UPnP Device information
    def DEVICE_UDN = "Test-1_0-22124"
    def DEVICE_TYPE =  "Test"
    def DEVICE_VERSION = 1
    def DEVICE_FRIENDLY_NAME = "WeMo Test"
    def DEVICE_URL = "http://127.0.0.1:${ORG_OSGI_SERVICE_HTTP_PORT}"
    def DEVICE_DESCRIPTION_PATH = "/setup.xml"
    def DEVICE_CONTROL_PATH = '/upnp/control/'

    ManagedThingProvider managedThingProvider
    static MockUpnpService mockUpnpService
    UpnpIOServiceImpl upnpIOService
    ThingRegistry thingRegistry
    ItemRegistry itemRegistry

    Thing thing
    Item testItem
    def DEFAULT_TEST_ITEM_NAME = "testItem"

    protected void setUpServices() {
        //StorageService is required from the ManagedThingProvider
        VolatileStorageService volatileStorageService = new VolatileStorageService()
        registerService(volatileStorageService)

        //Mock the UPnP Service, that is required from the UPnP IO Service
        mockUpnpService = new MockUpnpService(false, true)
        mockUpnpService.startup()
        registerService(mockUpnpService, UpnpService.class.getName())

        managedThingProvider = getService(ManagedThingProvider.class);
        assertThat managedThingProvider, is (notNullValue())

        thingRegistry = getService(ThingRegistry.class)
        assertThat(thingRegistry, is (notNullValue()))

        itemRegistry = getService(ItemRegistry.class)
        assertThat(itemRegistry, is (notNullValue()))

        //UPnP IO Service is required from the WemoDiscoveryService and WemoHandlerFactory
        upnpIOService = getService(UpnpIOService.class)
        assertThat(UpnpIOService, is(notNullValue()))
    }

    protected registerServlet(String ServletURL, HttpServlet servlet) {
        HttpService httpService = getService(HttpService.class)
        assertThat(httpService, is(notNullValue()))
        httpService.registerServlet(ServletURL, servlet, null, null)
    }

    protected void unregisterServlet(def servletURL) {
        HttpService httpService = getService(HttpService.class)
        httpService.unregister(servletURL)
    }

    protected void createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN)

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID)
        Channel channel = new Channel(channelUID, itemAcceptedType)

        thing = ThingBuilder.create(thingTypeUID, thingUID)
                .withConfiguration(configuration)
                .withChannel(channel)
                .build();

        managedThingProvider.add(thing)

        createItem(channelUID,DEFAULT_TEST_ITEM_NAME,itemAcceptedType)
    }

    protected void createItem (ChannelUID channelUID,String itemName, String acceptedItemType) {
        if(acceptedItemType.equals("Switch")) {
            testItem = new SwitchItem(itemName)
        }
        // If a new test is implemented with different Item Type testItem from this Type must be created here

        itemRegistry.add(testItem)

        def ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
        assertThat itemChannelLinkProvider, is(notNullValue())

        ThingUID thingUID = thing.getUID()
        itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID))
    }


    protected addUpnpDevice(def serviceTypeID, def serviceNumber, def modelName) {
        UDN udn = new UDN(DEVICE_UDN);
        URL deviceURL = new URL(DEVICE_URL + DEVICE_DESCRIPTION_PATH);

        RemoteDeviceIdentity identity = new RemoteDeviceIdentity(udn, WemoHandler.SUBSCRIPTION_DURATION, deviceURL, new byte[1], null)
        DeviceType type = new DeviceType(DEVICE_MANUFACTURER, DEVICE_TYPE, DEVICE_VERSION);

        ManufacturerDetails manufacturerDetails = new ManufacturerDetails(DEVICE_MANUFACTURER)
        ModelDetails modelDetails = new ModelDetails(modelName)
        DeviceDetails details = new DeviceDetails(DEVICE_FRIENDLY_NAME, manufacturerDetails, modelDetails)

        def serviceType = new ServiceType(DEVICE_MANUFACTURER, serviceTypeID)
        def serviceId = new ServiceId(DEVICE_MANUFACTURER, serviceNumber)

        // Use the same URI for control, event subscription and device description
        URI mockURI = new URI(DEVICE_URL + DEVICE_DESCRIPTION_PATH)
        URI descriptorURI = mockURI
        URI controlURI = mockURI
        URI eventSubscriptionURI = mockURI

        RemoteService service = new RemoteService(serviceType, serviceId, descriptorURI, controlURI, eventSubscriptionURI);

        RemoteDevice localDevice = new RemoteDevice(identity, type, details, service);
        mockUpnpService.getRegistry().addDevice(localDevice)
    }

    protected <T extends ThingHandler> T getThingHandler(Class<T> clazz){
        WemoHandlerFactory factory
        waitForAssert({
            factory = getService(ThingHandlerFactory, WemoHandlerFactory)
            assertThat factory, is(notNullValue())
        }, 10000)
        def handlers = getThingHandlers(factory)

        for(ThingHandler handler : handlers) {
            if(clazz.isInstance(handler)) {
                return handler
            }
        }
        return null
    }

    private Set<ThingHandler> getThingHandlers(ThingHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
        assertThat thingManager, not(null)
        ImmutableSet.copyOf(thingManager.thingHandlersByFactory.get(factory))
    }
}

abstract class GenericWemoHttpServlet extends HttpServlet{
    final static def parser = new XmlParser()
    final static def CONTENT_TYPE = "text/xml; charset=utf-8"

    def soapNamespace
    def uNamespace
    def responseStatus

    GenericWemoHttpServlet (String service, String serviceNumber) {
        soapNamespace = new Namespace("http://schemas.xmlsoap.org/soap/envelope/", 'soap')
        uNamespace = new Namespace("urn:${GenericWemoOSGiTest.DEVICE_MANUFACTURER}:service:${service}:${serviceNumber}", 'u')
        responseStatus = HttpServletResponse.SC_OK
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        def requestInputStream = request.getInputStream()

        def root
        // Xml Parser is not thread safe. SAXException will be thrown, if multiple threads try to use it simultaneously
        synchronized (parser){
            root = parser.parse(requestInputStream)
        }

        String responseContent  = handleRequest(root)

        response.setStatus(responseStatus);
        response.setContentType(CONTENT_TYPE)

        if(responseStatus == HttpServletResponse.SC_OK) {
            response.getOutputStream().print(responseContent)
        }
    }

    protected void setResponseStatus(int status) {
        responseStatus = status
    }

    abstract protected String handleRequest (Node root);
}

