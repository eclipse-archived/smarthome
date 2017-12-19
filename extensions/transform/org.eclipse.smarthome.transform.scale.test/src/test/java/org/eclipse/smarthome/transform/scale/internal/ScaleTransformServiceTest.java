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
package org.eclipse.smarthome.transform.scale.internal;

import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class ScaleTransformServiceTest {
    private ScaleTransformationService processor;

    @Before
    public void init() {
        processor = new ScaleTransformationService() {
            @Override
            protected Locale getLocale() {
                return Locale.US;
            }
        };
    }

    @Test
    public void testTransformByScale() throws TransformationException {

        // need to be sure we'll have the german version
        String existingscale = "scale/humidex_de.scale";
        String source = "10";
        String transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("nicht wesentlich", transformedResponse);

        existingscale = "scale/limits.scale";
        source = "10";
        transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("middle", transformedResponse);
    }

    @Test
    public void testTransformByScaleLimits() throws TransformationException {
        String existingscale = "scale/limits.scale";

        // Testing upper bound opened range
        String source = "500";
        String transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("extreme", transformedResponse);

        // Testing lower bound opened range
        source = "-10";
        transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("low", transformedResponse);

        // Testing unfinite up and down range
        existingscale = "scale/catchall.scale";
        source = "-10";
        transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("catchall", transformedResponse);
    }

    @Test
    public void testTransformByScaleUndef() throws TransformationException {

        // check that for undefined/non numeric value we return empty string
        // Issue #1107
        String existingscale = "scale/humidex_fr.scale";
        String source = "-";
        String transformedResponse = processor.transform(existingscale, source);
        Assert.assertEquals("", transformedResponse);
    }

    @Test
    public void testTransformByScaleErrorInBounds() throws TransformationException {

        // the tested file contains inputs that generate a conversion error of the bounds
        // of range
        String existingscale = "scale/erroneous.scale";
        String source = "15";
        try {
            @SuppressWarnings("unused")
            String transformedResponse = processor.transform(existingscale, source);
            fail();
        } catch (TransformationException e) {
            // awaited result
        }
    }

    @Test
    public void testEvaluationOrder() throws TransformationException {
        // Ensures that only first matching scale as presented in the file is taken in account
        String evaluationOrder = "scale/evaluationorder.scale";
        // This value matches two lines of the scale file
        String source = "12";

        String transformedResponse = processor.transform(evaluationOrder, source);
        Assert.assertEquals("first", transformedResponse);
    }

}
