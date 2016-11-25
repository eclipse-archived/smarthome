/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.binding.hue.HueBindingConstants
import org.eclipse.smarthome.binding.hue.internal.discovery.HueBridgeNupnpDiscovery
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.config.discovery.inbox.Inbox
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Test

/**
 *
 * @author Christoph Knauf - Initial contribution
 *
 */
class HueBridgeNupnpDiscoveryOSGITest extends OSGiTest{

    HueBridgeNupnpDiscovery sut;
    VolatileStorageService volatileStorageService = new VolatileStorageService();
    DiscoveryListener discoveryListener
    Inbox inbox

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge")
    final String ip1 = "192.168.31.17"
    final String ip2 = "192.168.30.28"
    final String sn1 = "00178820057f"
    final String sn2 = "001788141b41"
    final ThingUID BRIDGE_THING_UID_1 = new ThingUID(BRIDGE_THING_TYPE_UID, sn1)
    final ThingUID BRIDGE_THING_UID_2 = new ThingUID(BRIDGE_THING_TYPE_UID, sn2)
    final InboxFilterCriteria inboxFilter = new InboxFilterCriteria(BRIDGE_THING_TYPE_UID,null)
    final String validBridgeDiscoveryResult = '[{"id":"001788fffe20057f","internalipaddress":'+ip1+'},{"id":"001788fffe141b41","internalipaddress":'+ip2+'}]'
    def discoveryResult
    def expBridgeDescription = '''
				<?xml version="1.0"?>
				<root xmlns="urn:schemas-upnp-org:device-1-0">
				  <specVersion>
				    <major>1</major>
				    <minor>0</minor>
				  </specVersion>
				  <URLBase>http://$IP:80/</URLBase>
				  <device>
				    <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>
				    <friendlyName>Philips hue ($IP)</friendlyName>
				    <manufacturer>Royal Philips Electronics</manufacturer>
				    <manufacturerURL>http://www.philips.com</manufacturerURL>
				    <modelDescription>Philips hue Personal Wireless Lighting</modelDescription>
				    <modelName>Philips hue bridge 2012</modelName>
				    <modelNumber>1000000000000</modelNumber>
				    <modelURL>http://www.meethue.com</modelURL>
				    <serialNumber>93eadbeef13</serialNumber>
				    <UDN>uuid:01234567-89ab-cdef-0123-456789abcdef</UDN>
				    <serviceList>
				      <service>
				        <serviceType>(null)</serviceType>
				        <serviceId>(null)</serviceId>
				        <controlURL>(null)</controlURL>
				        <eventSubURL>(null)</eventSubURL>
				        <SCPDURL>(null)</SCPDURL>
				      </service>
				    </serviceList>
				    <presentationURL>index.html</presentationURL>
				    <iconList>
				      <icon>
				        <mimetype>image/png</mimetype>
				        <height>48</height>
				        <width>48</width>
				        <depth>24</depth>
				        <url>hue_logo_0.png</url>
				      </icon>
				      <icon>
				        <mimetype>image/png</mimetype>
				        <height>120</height>
				        <width>120</width>
				        <depth>24</depth>
				        <url>hue_logo_3.png</url>
				      </icon>
				    </iconList>
				  </device>
				</root>
			'''

    @Before
    public void setUp(){
        registerService(volatileStorageService);

        sut = getService(DiscoveryService, HueBridgeNupnpDiscovery)
        assertThat (sut, is(notNullValue()));

        inbox = getService(Inbox)
        assertThat inbox, is(notNullValue())

        sut.stopBackgroundDiscovery()
        unregisterCurrentDiscoveryListener()
    }

    @Test
    public void 'assert that bridge thing type is supported'(){
        assertThat sut.getSupportedThingTypes().size(), is(1)
        assertThat sut.getSupportedThingTypes().getAt(0), is(HueBindingConstants.THING_TYPE_BRIDGE)
    }


    @Test
    public void 'assert that valid bridges are discovered'(){
        if (inbox.get(inboxFilter).size != 0) inbox.remove(BRIDGE_THING_TYPE_UID)
        sut = new ConfigurableBridgeNupnpDiscoveryMock()
        registerService(sut, DiscoveryService.class.name)
        discoveryResult = validBridgeDiscoveryResult
        def results = [:]
        registerDiscoveryListener( [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                results.put(result.getThingUID(),result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)

        sut.startScan()

        waitForAssert{
            assertThat results.size(), is(2)
            assertThat results.get(BRIDGE_THING_UID_1), is(notNullValue())
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_1), ip1, sn1)
            assertThat results.get(BRIDGE_THING_UID_2), is(notNullValue())
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_2), ip2, sn2)

            def inboxResults = inbox.get(inboxFilter)
            assertTrue inboxResults.size() >= 2
            assertThat inboxResults.find{it.getThingUID() == BRIDGE_THING_UID_1}, is(notNullValue())
            assertThat inboxResults.find{it.getThingUID() == BRIDGE_THING_UID_2}, is(notNullValue())
        }
    }

    @Test
    public void 'assert that invalid bridges are not discovered'(){
        if (inbox.get(inboxFilter).size != 0) inbox.remove(BRIDGE_THING_TYPE_UID)
        sut = new ConfigurableBridgeNupnpDiscoveryMock()
        registerService(sut, DiscoveryService.class.name)
        def results = [:]
        registerDiscoveryListener( [
            thingDiscovered: { DiscoveryService source, DiscoveryResult result ->
                results.put(result.getThingUID(),result)
            },
            thingRemoved: { DiscoveryService source, ThingUID thingId ->
            },
            discoveryFinished: { DiscoveryService source ->
            },
            discoveryErrorOccurred: { DiscoveryService source, Exception exception ->
            }
        ] as DiscoveryListener)

        //missing ip
        discoveryResult = '[{"id":"001788fffe20057f","internalipaddress":}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //missing id
        discoveryResult = '[{"id":"","internalipaddress":192.168.30.22}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //id < 10
        discoveryResult = '[{"id":"012345678","internalipaddress":192.168.30.22}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //bridge indicator not part of id
        discoveryResult = '[{"id":"0123456789","internalipaddress":192.168.30.22}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //bridge indicator at wrong position (-1)
        discoveryResult = '[{"id":"01234'+HueBridgeNupnpDiscovery.BRIDGE_INDICATOR+'7891","internalipaddress":192.168.30.22}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //bridge indicator at wrong position (+1)
        discoveryResult = '[{"id":"0123456'+HueBridgeNupnpDiscovery.BRIDGE_INDICATOR+'7891","internalipaddress":192.168.30.22}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //bridge not reachable
        discoveryResult = '[{"id":"001788fffe20057f","internalipaddress":192.168.30.1}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        //invalid bridge description
        expBridgeDescription = ""
        discoveryResult = '[{"id":"001788fffe20057f","internalipaddress":'+ip1+'}]'
        sut.startScan()
        waitForAssert{ assertThat results.size(), is(0)}

        waitForAssert {
            assertThat inbox.get(inboxFilter).size(), is(0)
        }
    }

    private void checkDiscoveryResult(DiscoveryResult result, String expIp, String expSn){
        assertThat result.getBridgeUID(), is(null)
        assertThat result.getLabel(), is(HueBridgeNupnpDiscovery.LABEL_PATTERN.replace("IP",expIp))
        assertThat result.getProperties().get("ipAddress"), is(expIp)
        assertThat result.getProperties().get("serialNumber"), is(expSn)
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener()
        this.discoveryListener = discoveryListener
        sut.addDiscoveryListener(this.discoveryListener)
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            sut.removeDiscoveryListener(this.discoveryListener)
        }
    }

    //Mock class which only overrides the doGetRequest method in order to make the class testable
    class ConfigurableBridgeNupnpDiscoveryMock extends HueBridgeNupnpDiscovery {
        @Override
        protected String doGetRequest(String url) throws IOException {
            if (url.contains("meethue")){
                return discoveryResult
            }else if (url.contains(ip1)){
                return  expBridgeDescription.replaceAll('$IP', ip1)
            }else if (url.contains(ip2)){
                return  expBridgeDescription.replaceAll('$IP', ip2)
            }
            throw new IOException()
        }
    }

}