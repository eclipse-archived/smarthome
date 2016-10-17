/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.firmware

import static org.eclipse.smarthome.core.thing.firmware.Constants.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Test

/**
 * Testing the {@link Firmware} domain object.
 *
 * @author Thomas Höfer - Initial contribution
 */
final class FirmwareTest extends OSGiTest {

    def static final FILE_NAME = "firmware.txt"

    def thingTypeUID = new ThingTypeUID("binding", "thingType")

    Firmware valpha = new Firmware.Builder(new FirmwareUID(thingTypeUID, "alpha")).build()
    Firmware valpha1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "alpha1")).build()
    Firmware vbeta = new Firmware.Builder(new FirmwareUID(thingTypeUID, "beta")).withPrerequisiteVersion(valpha1.getVersion()).build()
    Firmware vbetafix = new Firmware.Builder(new FirmwareUID(thingTypeUID, "beta-fix")).build()
    Firmware vgamma = new Firmware.Builder(new FirmwareUID(thingTypeUID, "gamma")).withPrerequisiteVersion(vbetafix.getVersion()).build()
    Firmware vdelta = new Firmware.Builder(new FirmwareUID(thingTypeUID, "delta")).build()

    Firmware xyz = new Firmware.Builder(new FirmwareUID(thingTypeUID, "xyz_1")).build()
    Firmware abc = new Firmware.Builder(new FirmwareUID(thingTypeUID, "abc.2")).build()

    Firmware v0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "0")).build()
    Firmware v0dot0dot9 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "0.0.9")).build()
    Firmware v1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1")).build()
    Firmware v1dot0dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.0")).build()
    Firmware v1dot0dot1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.1")).build()
    Firmware v1dot0dot2 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.2")).withPrerequisiteVersion(v1dot0dot1.getVersion()).build()
    Firmware v1dot0dot2dashfix = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.2-fix")).build()
    Firmware v1dot0dot3 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.3")).withPrerequisiteVersion(v1dot0dot2dashfix.getVersion()).build()
    Firmware v1dash1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1-1")).build()
    Firmware v1dot1dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.1.0")).withPrerequisiteVersion(v1dot0dot2dashfix.getVersion()).build()
    Firmware v1dot2dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.0")).build()
    Firmware v1dot10 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.10")).build()
    Firmware v1dot10dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.10.0")).build()
    Firmware v1dash11dot2_1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1-11.2_1")).build()
    Firmware v1dot11_2dasha = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.11_2-a")).build()
    Firmware v2dot0dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "2.0.0")).withPrerequisiteVersion(v1dot11_2dasha.getVersion()).build()

    Firmware combined1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.3-2.3.4")).build()
    Firmware combined2 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.3-2.4.1")).build()
    Firmware combined3 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.3.1-2.3.4")).build()
    Firmware combined4 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.3.1-2.4.1")).build()

    @Test
    void 'test builder'() {
        def version = "1.0.0"
        def uid = new FirmwareUID(thingTypeUID, version)
        def changelog = "changelog"
        def inputStream = new BufferedInputStream(System.in)
        def description = "description"
        def model = "model"
        def onlineChangelog = new URL("http://online.changelog")
        def prerequisiteVersion = "0.0.9"
        def md5hash = "123abc"
        def vendor = "vendor"

        Firmware firmware = new Firmware.Builder(uid)
                .withChangelog(changelog)
                .withInputStream(inputStream)
                .withDescription(description)
                .withModel(model)
                .withOnlineChangelog(onlineChangelog)
                .withPrerequisiteVersion(prerequisiteVersion)
                .withVendor(vendor)
                .withMd5Hash(md5hash)
                .build();

        assertThat firmware, is(notNullValue())
        assertThat firmware.getUID(), is(uid)
        assertThat firmware.getChangelog(), is(changelog)
        assertThat firmware.getInputStream(), is(inputStream)
        assertThat firmware.getDescription(), is(description)
        assertThat firmware.getModel(), is(model)
        assertThat firmware.getOnlineChangelog(), is(onlineChangelog)
        assertThat firmware.getVersion(), is(version)
        assertThat firmware.getPrerequisiteVersion(), is(prerequisiteVersion)
        assertThat firmware.getVendor(), is(vendor)
        assertThat firmware.getMd5Hash(), is(md5hash)
    }

    @Test
    void 'test firmware successor version'() {
        assertThat v2dot0dot0.isSuccessorVersion(v1dot11_2dasha.getVersion()), is(true)
        assertThat v1dot11_2dasha.isSuccessorVersion(v2dot0dot0.getVersion()), is(false)
        assertThat v1dot11_2dasha.isSuccessorVersion(v1dot11_2dasha.getVersion()), is(false)

        assertThat v1dot11_2dasha.isSuccessorVersion(v1dash11dot2_1.getVersion()), is(true)
        assertThat v1dash11dot2_1.isSuccessorVersion(v1dot10dot0.getVersion()), is(true)
        assertThat v1dot10dot0.isSuccessorVersion(v1dot10.getVersion()), is(true)
        assertThat v1dot10.isSuccessorVersion(v1dot2dot0.getVersion()), is(true)
        assertThat v1dot2dot0.isSuccessorVersion(v1dot1dot0.getVersion()), is(true)
        assertThat v1dot1dot0.isSuccessorVersion(v1dash1.getVersion()), is(true)
        assertThat v1dash1.isSuccessorVersion(v1dot0dot3.getVersion()), is(true)
        assertThat v1dot0dot3.isSuccessorVersion(v1dot0dot2dashfix.getVersion()), is(true)
        assertThat v1dot0dot2dashfix.isSuccessorVersion(v1dot0dot2.getVersion()), is(true)
        assertThat v1dot0dot2.isSuccessorVersion(v1dot0dot1.getVersion()), is(true)
        assertThat v1dot0dot1.isSuccessorVersion(v1dot0dot0.getVersion()), is(true)
        assertThat v1dot0dot1.isSuccessorVersion(v1.getVersion()), is(true)
        assertThat v1.isSuccessorVersion(v0dot0dot9.getVersion()), is(true)
        assertThat v0dot0dot9.isSuccessorVersion(v0.getVersion()), is(true)

        assertThat vgamma.isSuccessorVersion(vbetafix.getVersion()), is(true)
        assertThat vbetafix.isSuccessorVersion(vbeta.getVersion()), is(true)
        assertThat vbeta.isSuccessorVersion(valpha1.getVersion()), is(true)
        assertThat valpha1.isSuccessorVersion(valpha.getVersion()), is(true)

        assertThat xyz.isSuccessorVersion(abc.getVersion()), is(true)
        assertThat abc.isSuccessorVersion(v2dot0dot0.getVersion()), is(true)
        assertThat abc.isSuccessorVersion(xyz.getVersion()), is(false)

        assertThat vdelta.isSuccessorVersion(v0dot0dot9.getVersion()), is(true)
        assertThat v0dot0dot9.isSuccessorVersion(vdelta.getVersion()), is(false)
        assertThat vdelta.isSuccessorVersion(vgamma.getVersion()), is(false)

        assertThat vdelta.isSuccessorVersion(null), is(false)

        assertThat combined4.isSuccessorVersion(combined3.getVersion()), is(true)
        assertThat combined3.isSuccessorVersion(combined4.getVersion()), is(false)

        assertThat combined3.isSuccessorVersion(combined2.getVersion()), is(true)
        assertThat combined2.isSuccessorVersion(combined3.getVersion()), is(false)

        assertThat combined2.isSuccessorVersion(combined1.getVersion()), is(true)
        assertThat combined1.isSuccessorVersion(combined2.getVersion()), is(false)
    }

    @Test
    void 'test firmware prerequisite version'() {
        assertThat valpha.isPrerequisiteVersion(vbeta.getVersion()), is(false)
        assertThat valpha.isPrerequisiteVersion(null), is(false)

        assertThat vbeta.isPrerequisiteVersion(valpha1.getVersion()), is(true)
        assertThat vbeta.isPrerequisiteVersion(valpha.getVersion()), is(false)

        assertThat vgamma.isPrerequisiteVersion(vbetafix.getVersion()), is(true)
        assertThat vgamma.isPrerequisiteVersion(vbeta.getVersion()), is(false)

        assertThat vdelta.isPrerequisiteVersion(vgamma.getVersion()), is(false)

        assertThat v1dot0dot2.isPrerequisiteVersion(v1dot0dot1.getVersion()), is(true)
        assertThat v1dot0dot2.isPrerequisiteVersion(v1dot0dot0.getVersion()), is(false)
        assertThat v1dot0dot2.isPrerequisiteVersion(v0dot0dot9.getVersion()), is(false)

        assertThat v1dot1dot0.isPrerequisiteVersion(v1dash1.getVersion()), is(true)
        assertThat v1dot1dot0.isPrerequisiteVersion(v1dot0dot3.getVersion()), is(true)
        assertThat v1dot1dot0.isPrerequisiteVersion(v1dot0dot2dashfix.getVersion()), is(true)
        assertThat v1dot1dot0.isPrerequisiteVersion(v1dot0dot2.getVersion()), is(false)
        assertThat v1dot1dot0.isPrerequisiteVersion(v1dot0dot1.getVersion()), is(false)
        assertThat v1dot1dot0.isPrerequisiteVersion(v0dot0dot9.getVersion()), is(false)

        assertThat v2dot0dot0.isPrerequisiteVersion(v1dot11_2dasha.getVersion()), is(true)
        assertThat v2dot0dot0.isPrerequisiteVersion(v1dash11dot2_1.getVersion()), is(false)
    }

    @Test(expected=IllegalArgumentException)
    void 'asssert that colon cannot be used as part of the firmware version'() {
        new Firmware.Builder(new FirmwareUID(new ThingTypeUID("test", "test"), "1.2:3"));
    }

    @Test
    void 'assert that firmware with valid MD5 hash value does not throw exception for getBytes'() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1")).withInputStream(getBundleContext().getBundle().getResource(FILE_NAME).openStream()).withMd5Hash("78805a221a988e79ef3f42d7c5bfd418").build()

        byte[] bytes = firmware.getBytes()
        assertThat bytes, is(notNullValue())
    }

    @Test(expected=IllegalStateException)
    void 'assert that firmware with invalid MD5 hash value throws exception for getBytes'() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1")).withInputStream(getBundleContext().getBundle().getResource(FILE_NAME).openStream()).withMd5Hash("78805a221a988e79ef3f42d7c5bfd419").build()
        firmware.getBytes()
    }

    @Test
    void 'assert that firmware without MD5 hash value does not throw exception for getBytes'() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1")).withInputStream(getBundleContext().getBundle().getResource(FILE_NAME).openStream()).build()

        byte[] bytes = firmware.getBytes()
        assertThat bytes, is(notNullValue())
    }
}
