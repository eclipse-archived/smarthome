/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.binding.firmware;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Test;

/**
 * Testing the {@link Firmware} domain object.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Henning Sudbrock - Migrated from Groovy to Java
 */
public class FirmwareTest extends JavaOSGiTest {

    private static final String FILE_NAME = "firmware.txt";

    private static final ThingTypeUID thingTypeUID = new ThingTypeUID("binding", "thingType");

    private static final Firmware valpha = new Firmware.Builder(new FirmwareUID(thingTypeUID, "alpha")).build();
    private static final Firmware valpha1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "alpha1")).build();
    private static final Firmware vbeta = new Firmware.Builder(new FirmwareUID(thingTypeUID, "beta"))
            .withPrerequisiteVersion(valpha1.getVersion()).build();
    private static final Firmware vbetafix = new Firmware.Builder(new FirmwareUID(thingTypeUID, "beta-fix")).build();
    private static final Firmware vgamma = new Firmware.Builder(new FirmwareUID(thingTypeUID, "gamma"))
            .withPrerequisiteVersion(vbetafix.getVersion()).build();
    private static final Firmware vdelta = new Firmware.Builder(new FirmwareUID(thingTypeUID, "delta")).build();

    private static final Firmware xyz = new Firmware.Builder(new FirmwareUID(thingTypeUID, "xyz_1")).build();
    private static final Firmware abc = new Firmware.Builder(new FirmwareUID(thingTypeUID, "abc.2")).build();

    private static final Firmware v0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "0")).build();
    private static final Firmware v0dot0dot9 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "0.0.9")).build();
    private static final Firmware v1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1")).build();
    private static final Firmware v1dot0dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.0")).build();
    private static final Firmware v1dot0dot1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.1")).build();
    private static final Firmware v1dot0dot2 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.2"))
            .withPrerequisiteVersion(v1dot0dot1.getVersion()).build();
    private static final Firmware v1dot0dot2dashfix = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.2-fix"))
            .build();
    private static final Firmware v1dot0dot3 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.0.3"))
            .withPrerequisiteVersion(v1dot0dot2dashfix.getVersion()).build();
    private static final Firmware v1dash1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1-1")).build();
    private static final Firmware v1dot1dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.1.0"))
            .withPrerequisiteVersion(v1dot0dot2dashfix.getVersion()).build();
    private static final Firmware v1dot2dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.0")).build();
    private static final Firmware v1dot10 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.10")).build();
    private static final Firmware v1dot10dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.10.0")).build();
    private static final Firmware v1dash11dot2_1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1-11.2_1"))
            .build();
    private static final Firmware v1dot11_2dasha = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.11_2-a"))
            .build();
    private static final Firmware v2dot0dot0 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "2.0.0"))
            .withPrerequisiteVersion(v1dot11_2dasha.getVersion()).build();

    private static final Firmware combined1 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.3-2.3.4"))
            .build();
    private static final Firmware combined2 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.2.3-2.4.1"))
            .build();
    private static final Firmware combined3 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.3.1-2.3.4"))
            .build();
    private static final Firmware combined4 = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1.3.1-2.4.1"))
            .build();

    @Test
    public void testBuilder() throws MalformedURLException {
        String version = "1.0.0";
        FirmwareUID uid = new FirmwareUID(thingTypeUID, version);
        String changelog = "changelog";
        InputStream inputStream = new BufferedInputStream(System.in);
        String description = "description";
        String model = "model";
        boolean modelRestricted = true;
        URL onlineChangelog = new URL("http://online.changelog");
        String prerequisiteVersion = "0.0.9";
        String md5hash = "123abc";
        String vendor = "vendor";

        Firmware firmware = new Firmware.Builder(uid).withChangelog(changelog).withInputStream(inputStream)
                .withDescription(description).withModel(model).withModelRestricted(modelRestricted)
                .withOnlineChangelog(onlineChangelog).withPrerequisiteVersion(prerequisiteVersion).withVendor(vendor)
                .withMd5Hash(md5hash).build();

        assertThat(firmware, is(notNullValue()));
        assertThat(firmware.getUID(), is(uid));
        assertThat(firmware.getChangelog(), is(changelog));
        assertThat(firmware.getInputStream(), is(inputStream));
        assertThat(firmware.getDescription(), is(description));
        assertThat(firmware.getModel(), is(model));
        assertThat(firmware.isModelRestricted(), is(modelRestricted));
        assertThat(firmware.getOnlineChangelog(), is(onlineChangelog));
        assertThat(firmware.getVersion(), is(version));
        assertThat(firmware.getPrerequisiteVersion(), is(prerequisiteVersion));
        assertThat(firmware.getVendor(), is(vendor));
        assertThat(firmware.getMd5Hash(), is(md5hash));
    }

    @Test
    public void testFirmwareSuccessorVersion() {
        assertThat(v2dot0dot0.isSuccessorVersion(v1dot11_2dasha.getVersion()), is(true));
        assertThat(v1dot11_2dasha.isSuccessorVersion(v2dot0dot0.getVersion()), is(false));
        assertThat(v1dot11_2dasha.isSuccessorVersion(v1dot11_2dasha.getVersion()), is(false));

        assertThat(v1dot11_2dasha.isSuccessorVersion(v1dash11dot2_1.getVersion()), is(true));
        assertThat(v1dash11dot2_1.isSuccessorVersion(v1dot10dot0.getVersion()), is(true));
        assertThat(v1dot10dot0.isSuccessorVersion(v1dot10.getVersion()), is(true));
        assertThat(v1dot10.isSuccessorVersion(v1dot2dot0.getVersion()), is(true));
        assertThat(v1dot2dot0.isSuccessorVersion(v1dot1dot0.getVersion()), is(true));
        assertThat(v1dot1dot0.isSuccessorVersion(v1dash1.getVersion()), is(true));
        assertThat(v1dash1.isSuccessorVersion(v1dot0dot3.getVersion()), is(true));
        assertThat(v1dot0dot3.isSuccessorVersion(v1dot0dot2dashfix.getVersion()), is(true));
        assertThat(v1dot0dot2dashfix.isSuccessorVersion(v1dot0dot2.getVersion()), is(true));
        assertThat(v1dot0dot2.isSuccessorVersion(v1dot0dot1.getVersion()), is(true));
        assertThat(v1dot0dot1.isSuccessorVersion(v1dot0dot0.getVersion()), is(true));
        assertThat(v1dot0dot1.isSuccessorVersion(v1.getVersion()), is(true));
        assertThat(v1.isSuccessorVersion(v0dot0dot9.getVersion()), is(true));
        assertThat(v0dot0dot9.isSuccessorVersion(v0.getVersion()), is(true));

        assertThat(vgamma.isSuccessorVersion(vbetafix.getVersion()), is(true));
        assertThat(vbetafix.isSuccessorVersion(vbeta.getVersion()), is(true));
        assertThat(vbeta.isSuccessorVersion(valpha1.getVersion()), is(true));
        assertThat(valpha1.isSuccessorVersion(valpha.getVersion()), is(true));

        assertThat(xyz.isSuccessorVersion(abc.getVersion()), is(true));
        assertThat(abc.isSuccessorVersion(v2dot0dot0.getVersion()), is(true));
        assertThat(abc.isSuccessorVersion(xyz.getVersion()), is(false));

        assertThat(vdelta.isSuccessorVersion(v0dot0dot9.getVersion()), is(true));
        assertThat(v0dot0dot9.isSuccessorVersion(vdelta.getVersion()), is(false));
        assertThat(vdelta.isSuccessorVersion(vgamma.getVersion()), is(false));

        assertThat(vdelta.isSuccessorVersion(null), is(false));

        assertThat(combined4.isSuccessorVersion(combined3.getVersion()), is(true));
        assertThat(combined3.isSuccessorVersion(combined4.getVersion()), is(false));

        assertThat(combined3.isSuccessorVersion(combined2.getVersion()), is(true));
        assertThat(combined2.isSuccessorVersion(combined3.getVersion()), is(false));

        assertThat(combined2.isSuccessorVersion(combined1.getVersion()), is(true));
        assertThat(combined1.isSuccessorVersion(combined2.getVersion()), is(false));
    }

    @Test
    public void testFirmwarePrerequisiteVersion() {
        assertThat(valpha.isPrerequisiteVersion(vbeta.getVersion()), is(false));
        assertThat(valpha.isPrerequisiteVersion(null), is(false));

        assertThat(vbeta.isPrerequisiteVersion(valpha1.getVersion()), is(true));
        assertThat(vbeta.isPrerequisiteVersion(valpha.getVersion()), is(false));

        assertThat(vgamma.isPrerequisiteVersion(vbetafix.getVersion()), is(true));
        assertThat(vgamma.isPrerequisiteVersion(vbeta.getVersion()), is(false));

        assertThat(vdelta.isPrerequisiteVersion(vgamma.getVersion()), is(false));

        assertThat(v1dot0dot2.isPrerequisiteVersion(v1dot0dot1.getVersion()), is(true));
        assertThat(v1dot0dot2.isPrerequisiteVersion(v1dot0dot0.getVersion()), is(false));
        assertThat(v1dot0dot2.isPrerequisiteVersion(v0dot0dot9.getVersion()), is(false));

        assertThat(v1dot1dot0.isPrerequisiteVersion(v1dash1.getVersion()), is(true));
        assertThat(v1dot1dot0.isPrerequisiteVersion(v1dot0dot3.getVersion()), is(true));
        assertThat(v1dot1dot0.isPrerequisiteVersion(v1dot0dot2dashfix.getVersion()), is(true));
        assertThat(v1dot1dot0.isPrerequisiteVersion(v1dot0dot2.getVersion()), is(false));
        assertThat(v1dot1dot0.isPrerequisiteVersion(v1dot0dot1.getVersion()), is(false));
        assertThat(v1dot1dot0.isPrerequisiteVersion(v0dot0dot9.getVersion()), is(false));

        assertThat(v2dot0dot0.isPrerequisiteVersion(v1dot11_2dasha.getVersion()), is(true));
        assertThat(v2dot0dot0.isPrerequisiteVersion(v1dash11dot2_1.getVersion()), is(false));
    }

    @Test
    public void testFirmwareIsNotSuitableForThingWithDifferentThingType() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(new ThingTypeUID("binding:thingTypeA"), "version"))
                .build();
        Thing thing = ThingBuilder.create(new ThingTypeUID("binding:thingTypeB"), "thing").build();

        assertThat(firmware.isSuitableFor(thing), is(false));
    }

    @Test
    public void testNotModelRestrictedFirmwareIsSuitableForThingWithSameThingType() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(new ThingTypeUID("binding:thingTypeA"), "version"))
                .build();
        Thing thing = ThingBuilder.create(new ThingTypeUID("binding:thingTypeA"), "thing").build();

        assertThat(firmware.isSuitableFor(thing), is(true));
    }

    @Test
    public void testModelRestrictedFirmwareIsSuitableForThingWithSameThingTypeAndSameModel() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(new ThingTypeUID("binding:thingTypeA"), "version"))
                .withModelRestricted(true).withModel("someModel").build();
        Thing thing = ThingBuilder.create(new ThingTypeUID("binding:thingTypeA"), "thing").build();
        thing.setProperty(Thing.PROPERTY_MODEL_ID, "someModel");

        assertThat(firmware.isSuitableFor(thing), is(true));
    }

    @Test
    public void testModelRestrictedFirmwareIsNotSuitableForThingWithSameThingTypeAndAnotherModel() {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(new ThingTypeUID("binding:thingTypeA"), "version"))
                .withModelRestricted(true).withModel("someModel").build();
        Thing thing = ThingBuilder.create(new ThingTypeUID("binding:thingTypeA"), "thing").build();
        thing.setProperty(Thing.PROPERTY_MODEL_ID, "someOtherModel");

        assertThat(firmware.isSuitableFor(thing), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatColonCannotBeUsedAsPartOfTheFirmwareVersion() {
        new Firmware.Builder(new FirmwareUID(new ThingTypeUID("test", "test"), "1.2:3"));
    }

    @Test
    public void assertThatFirmwareWithValidMD5HashValueDoesNotThrowExceptionForGetBytes() throws IOException {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1"))
                .withInputStream(bundleContext.getBundle().getResource(FILE_NAME).openStream())
                .withMd5Hash("78805a221a988e79ef3f42d7c5bfd418").build();

        byte[] bytes = firmware.getBytes();
        assertThat(bytes, is(notNullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void assertThatFirmwareWithInvalidMD5HashValueThrowsExceptionForGetBytes() throws IOException {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1"))
                .withInputStream(bundleContext.getBundle().getResource(FILE_NAME).openStream())
                .withMd5Hash("78805a221a988e79ef3f42d7c5bfd419").build();
        firmware.getBytes();
    }

    @Test
    public void assertThatFirmwareWithoutMD5HashValueDoesNotThrowExceptionForGetBytes() throws IOException {
        Firmware firmware = new Firmware.Builder(new FirmwareUID(thingTypeUID, "1"))
                .withInputStream(bundleContext.getBundle().getResource(FILE_NAME).openStream()).build();

        byte[] bytes = firmware.getBytes();
        assertThat(bytes, is(notNullValue()));
    }

}
