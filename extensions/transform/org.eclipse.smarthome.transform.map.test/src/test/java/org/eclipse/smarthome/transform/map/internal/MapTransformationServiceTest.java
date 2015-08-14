/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.map.internal;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class MapTransformationServiceTest {

    private MapTransformationService processor;

    @Before
    public void init() {
        processor = new MapTransformationService();
    }

    @Test
    @Ignore
    public void testTransformByMap() throws TransformationException {

        String existingGermanFilename = "map/doorstatus_de.map";
        String shouldBeLocalizedFilename = "map/doorstatus.map";
        String inexistingFilename = "map/de.map";

        // Test that we find a translation in an existing file
        String source = "CLOSED";
        String transformedResponse = processor.transform(existingGermanFilename, source);
        Assert.assertEquals("zu", transformedResponse);

        String usedfile = "conf/transform/" + existingGermanFilename;

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(usedfile));
            properties.setProperty(source, "changevalue");
            properties.store(new FileWriter(usedfile), "");

            // This tests that the requested transformation file has been removed from
            // the cache
            transformedResponse = processor.transform(existingGermanFilename, source);
            Assert.assertEquals("changevalue", transformedResponse);

            properties.setProperty(source, "zu");
            properties.store(new FileWriter(usedfile), "");

            transformedResponse = processor.transform(existingGermanFilename, source);
            Assert.assertEquals("zu", transformedResponse);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Test that an unknown input in an existing file give the expected behaviour
        // transformed response shall be the same as source if not found in the file
        source = "UNKNOWN";
        transformedResponse = processor.transform(existingGermanFilename, source);
        Assert.assertEquals(source, transformedResponse);

        // Test that an inexisting file raises correct exception as expected
        source = "CLOSED";
        try {
            transformedResponse = processor.transform(inexistingFilename, source);
            fail();
        } catch (Exception e) {
            // That's what we expect.
        }

        // Test that we find a localized version of desired file
        source = "CLOSED";
        transformedResponse = processor.transform(shouldBeLocalizedFilename, source);
        // as we don't know the real locale at the moment the
        // test is run, we test that the string has actually been transformed
        Assert.assertNotEquals(source, transformedResponse);
        transformedResponse = processor.transform(shouldBeLocalizedFilename, source);
        Assert.assertNotEquals(source, transformedResponse);
    }

}
