/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.test;

import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP
import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.PROPERTY_MANUFACTURER
import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.PROPERTY_MODEL
import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.THING_TYPE_RADIO

import org.eclipse.smarthome.binding.fsinternetradio.internal.FSInternetRadioDiscoveryParticipant

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.test.OSGiTest
import org.junit.*
import org.jupnp.model.meta.DeviceDetails
import org.jupnp.model.meta.ManufacturerDetails
import org.jupnp.model.meta.ModelDetails
import org.jupnp.model.meta.RemoteDevice
import org.jupnp.model.meta.RemoteDeviceIdentity
import org.jupnp.model.types.*

/**
 * OSGi tests for the {@link FSInternetRadioDiscoveryParticipant}
 *
 * @author Mihaela Memova
 *
 */
public class FSInternetRadioDiscoveryParticipantOSGiTest extends OSGiTest {

	UpnpDiscoveryParticipant discoveryParticipant

	//default device variables used in the tests
	DeviceType DEFAULT_TYPE = new DeviceType("namespace", "type")
	String DEFAULT_UPC = "upc"
	URI DEFAULT_URI = null

	// default radio variables used in most of the tests
	RemoteDeviceIdentity DEFAULT_RADIO_IDENTITY = new RemoteDeviceIdentity(new UDN("radioUDN"), 60, new URL("http://radioDescriptiveURL"), null, null)
	URL DEFAULT_RADIO_BASE_URL = new URL("http://radioBaseURL")
	String DEFAULT_RADIO_NAME = "HamaRadio"
	/* 
	 * The default radio is chosen from the {@link FrontierSiliconRadioDiscoveryParticipant}'s 
	 * set of supported radios
	 */
	String DEFAULT_RADIO_MANIFACTURER = "HAMA"
	String DEFAULT_RADIO_MODEL_NAME = "IR"
	String DEFAULT_RADIO_MODEL_DESCRIPTION = "IR Radio"
	String DEFAULT_RADIO_MODEL_NUMBER = "IR100"
	String DEFAULT_RADIO_SERIAL_NUMBER = "serialNumber123"

	String RADIO_BINDING_ID = "fsinternetradio" // taken from the binding.xml file
	String RADIO_THING_TYPE_ID = "radio" // taken from the thing-types.xml file
	String DEFAULT_RADIO_THING_UID = "$RADIO_BINDING_ID:$RADIO_THING_TYPE_ID:$DEFAULT_RADIO_SERIAL_NUMBER"

	@Before
	void setUp() {
		discoveryParticipant = getService(UpnpDiscoveryParticipant, FSInternetRadioDiscoveryParticipant)
		assertThat ("The FSInternetRadioDiscoveryParticipant service cannot be found", discoveryParticipant, is(notNullValue()))
	}

	@Test
	void 'verify correct supported types'() {
		assertThat ("The number of thing types that FSInternetRadioDiscoveryParticipant can identify is wrong", discoveryParticipant.supportedThingTypeUIDs.size(), is(1))
		assertThat ("The FSInternetRadioDiscoveryParticipant cannot identify THING_TYPE_RADIO", discoveryParticipant.supportedThingTypeUIDs.first(), is(THING_TYPE_RADIO))
	}

	@Test
	void 'verify valid DiscoveryResult with completeFSInterntRadioDevice'() {
		RemoteDevice completeFSInternetRadioDevice = createDefaultFSInternetRadioDevice(DEFAULT_RADIO_BASE_URL)
		discoveryParticipant.createResult(completeFSInternetRadioDevice).with {
			assertThat ("A result with wrong thingUID was created for a valid FSInternetRadio device", thingUID, is(new ThingUID(DEFAULT_RADIO_THING_UID)))
			assertThat ("A result with wrong thingTypeUID was created for a valid FSInternetRadio device", thingTypeUID, is (THING_TYPE_RADIO))
			assertThat ("A result with wrong manifacturer was created for a completete FSInternetRadio device", properties.get(PROPERTY_MANUFACTURER), is(DEFAULT_RADIO_MANIFACTURER))
			assertThat ("A result with wrong model number was created for a completete FSInternetRadio device", properties.get(PROPERTY_MODEL), is(DEFAULT_RADIO_MODEL_NUMBER))
		}
	}

	@Test
	void 'verify no discovery result for FSInternetRadio device with null details'() {
		RemoteDevice fsInterntRadioDeviceWithNullDetails = new RemoteDevice(null)
		assertThat ("A discovery result was created for a device with null details", discoveryParticipant.createResult(fsInterntRadioDeviceWithNullDetails), is(nullValue()))
	}

	@Test
	void 'verify no discovery result for unknown device'() {

		RemoteDevice unknownRemoteDevice = createUnknownRemoteDevice()
		assertThat ("A discovery result was created for an unknown device", discoveryParticipant.createResult(unknownRemoteDevice), is(nullValue()))
	}

	@Test
	void 'verify valid DiscoveryResult with FSInterntRadio device without base URL'() {

		RemoteDevice fsInternetRadioDeviceWithoutUrl = createDefaultFSInternetRadioDevice(null)
		discoveryParticipant.createResult(fsInternetRadioDeviceWithoutUrl).with {
			assertThat ("A result with wrong thingUID was created for a FSInternetRadio device without base URL", thingUID, is(new ThingUID(DEFAULT_RADIO_THING_UID)))
			assertThat ("A result with wrong thingTypeUID was created for a FSInternetRadio device without base URL", thingTypeUID, is (THING_TYPE_RADIO))
			assertThat ("A result with wrong manifacturer was created for a FSInternetRadio device without base URL", properties.get(PROPERTY_MANUFACTURER), is(DEFAULT_RADIO_MANIFACTURER))
			assertThat ("A result with wrong model number was created for a FSInternetRadio device without base URL", properties.get(PROPERTY_MODEL), is(DEFAULT_RADIO_MODEL_NUMBER))
		}
	}

	private RemoteDevice createDefaultFSInternetRadioDevice(URL baseURL) {

		ManufacturerDetails manifacturerDetails = new ManufacturerDetails(DEFAULT_RADIO_MANIFACTURER)
		ModelDetails modelDetails = new ModelDetails(DEFAULT_RADIO_MODEL_NAME, DEFAULT_RADIO_MODEL_DESCRIPTION, DEFAULT_RADIO_MODEL_NUMBER)
		DeviceDetails deviceDetails = new DeviceDetails(baseURL, DEFAULT_RADIO_NAME, manifacturerDetails, modelDetails, DEFAULT_RADIO_SERIAL_NUMBER, DEFAULT_UPC, DEFAULT_URI)

		return new RemoteDevice(DEFAULT_RADIO_IDENTITY, DEFAULT_TYPE, deviceDetails)
	}
	
	private RemoteDevice createUnknownRemoteDevice() {
		def deviceIdentityMaxAgeSeconds = 60
		RemoteDeviceIdentity identity = new RemoteDeviceIdentity(new UDN("unknownUDN"), deviceIdentityMaxAgeSeconds, new URL("http://unknownDescriptorURL"), null, null)
		URL anotherBaseURL = new URL("http://unknownBaseUrl")
		String friendlyName = "Unknown remote device"
		ManufacturerDetails manifacturerDetails = new ManufacturerDetails("UnknownManifacturer")
		ModelDetails modelDetails = new ModelDetails("unknownModel")
		String serialNumber = "unknownSerialNumber"

		DeviceDetails deviceDetails = new DeviceDetails(anotherBaseURL, friendlyName, manifacturerDetails, modelDetails, serialNumber, DEFAULT_UPC, DEFAULT_URI)

		return new RemoteDevice(identity, DEFAULT_TYPE, deviceDetails)
	}
}
