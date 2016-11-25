/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.transform.map.internal;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class MapTransformationServiceTest {

    private static final String SOURCE_CLOSED = "CLOSED";
    private static final String SOURCE_UNKNOWN = "UNKNOWN";
    private static final String EXISTING_FILENAME_DE = "map/doorstatus_de.map";
    private static final String SHOULD_BE_LOCALIZED_FILENAME = "map/doorstatus.map";
    private static final String INEXISTING_FILENAME = "map/de.map";
    private static final String BASE_FOLDER = "target";
    private static final String SRC_FOLDER = "conf";
    private static final String CONFIG_FOLDER = BASE_FOLDER + File.separator + SRC_FOLDER;
    private static final String USED_FILENAME = CONFIG_FOLDER + File.separator + "transform/" + EXISTING_FILENAME_DE;

    private MapTransformationService processor;

    @Before
    public void init() throws IOException {
        processor = new MapTransformationService() {
            @Override
            protected String getSourcePath() {
                return BASE_FOLDER + File.separator + super.getSourcePath();
            }

            @Override
            protected Locale getLocale() {
                return Locale.US;
            }
        };
        FileUtils.deleteDirectory(new File(CONFIG_FOLDER));
        FileUtils.copyDirectory(new File(SRC_FOLDER), new File(CONFIG_FOLDER));
    }

    @Test
    public void testTransformByMap() throws Exception {

        // Test that we find a translation in an existing file
        String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
        Assert.assertEquals("zu", transformedResponse);

        Properties properties = new Properties();
        try (FileReader reader = new FileReader(USED_FILENAME); FileWriter writer = new FileWriter(USED_FILENAME)) {
            properties.load(reader);
            properties.setProperty(SOURCE_CLOSED, "changevalue");
            properties.store(writer, "");

            // This tests that the requested transformation file has been removed from
            // the cache
            waitForAssert(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
                    Assert.assertEquals("changevalue", transformedResponse);
                    return null;
                }
            }, 10000, 100);

            properties.setProperty(SOURCE_CLOSED, "zu");
            properties.store(writer, "");

            waitForAssert(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final String transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_CLOSED);
                    Assert.assertEquals("zu", transformedResponse);
                    return null;
                }
            }, 10000, 100);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Checks that an unknown input in an existing file give the expected
        // transformed response that shall be empty string (Issue #1107) if not found in the file
        transformedResponse = processor.transform(EXISTING_FILENAME_DE, SOURCE_UNKNOWN);
        Assert.assertEquals("", transformedResponse);

        // Test that an inexisting file raises correct exception as expected
        try {
            transformedResponse = processor.transform(INEXISTING_FILENAME, SOURCE_CLOSED);
            fail();
        } catch (Exception e) {
            // That's what we expect.
        }

        // Test that we find a localized version of desired file
        transformedResponse = processor.transform(SHOULD_BE_LOCALIZED_FILENAME, SOURCE_CLOSED);
        // as we don't know the real locale at the moment the
        // test is run, we test that the string has actually been transformed
        Assert.assertNotEquals(SOURCE_CLOSED, transformedResponse);
        transformedResponse = processor.transform(SHOULD_BE_LOCALIZED_FILENAME, SOURCE_CLOSED);
        Assert.assertNotEquals(SOURCE_CLOSED, transformedResponse);
    }

    protected void waitForAssert(Callable<Void> assertion, int timeout, int sleepTime) throws Exception {
        int waitingTime = 0;
        while (waitingTime < timeout) {
            try {
                assertion.call();
                return;
            } catch (AssertionError error) {
                waitingTime += sleepTime;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        assertion.call();
    }

}
