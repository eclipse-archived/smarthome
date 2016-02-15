/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class GeoHashTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        @SuppressWarnings("unused")
        GeoHashType errorGenerator = new GeoHashType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmpty() {
        @SuppressWarnings("unused")
        GeoHashType errorGenerator = new GeoHashType("");
    }

    @Test
    public void testGeoHashDefaultConstructor() {
        // Ensure effectiveness of default constructor
        GeoHashType middleOfTheOcean = new GeoHashType();
        assertEquals(0, middleOfTheOcean.getLongitude().doubleValue(), 0.0000001);
        assertEquals(0, middleOfTheOcean.getLatitude().doubleValue(), 0.0000001);
        assertEquals("s00000000000", middleOfTheOcean.toString());
        // https://en.wikipedia.org/wiki/Geohash
        // By construction, on the equator height size of 12 precision hashtag is 1.9 cm
        // then inscrite circle is half this value
        assertEquals(0.009, middleOfTheOcean.getIncircleRadius().doubleValue(), 0.001);
        // and width is 3.7 cm
        assertEquals(0.02, middleOfTheOcean.getExcircleRadius().doubleValue(), 0.001);

        assertTrue(middleOfTheOcean.equals(GeoHashType.valueOf("s00000000000")));
    }

    @Test
    public void geoHashAndPointType() {
        PointType pointParis = PointType.valueOf("48.8566140,2.3522219");
        GeoHashType zoneInParis = GeoHashType.valueOf("u09tvw0f6szy");
        GeoHashType parisSud = GeoHashType.valueOf("u09tv");

        assertTrue(zoneInParis.contains(pointParis));
        assertTrue(parisSud.contains(zoneInParis));

        GeoHashType hashOfPoint = new GeoHashType(pointParis, new DecimalType(9));
        assertTrue(hashOfPoint.contains(pointParis));
    }
}
