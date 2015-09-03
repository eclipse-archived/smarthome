/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.smarthome.core.library.items.LocationItem;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class PointTypeTest {

    @Test
    public void testDistance() {
        // Ensure presence of default constructor
        PointType midleOfTheOcean = new PointType();
        assertEquals(0, midleOfTheOcean.getLongitude().doubleValue(), 0.0000001);
        assertEquals(0, midleOfTheOcean.getLatitude().doubleValue(), 0.0000001);
        assertEquals(0, midleOfTheOcean.getAltitude().doubleValue(), 0.0000001);
        
        PointType pointParis = new PointType("48.8566140,2.3522219");

        assertEquals(2.3522219, pointParis.getLongitude().doubleValue(), 0.0000001);
        assertEquals(48.856614, pointParis.getLatitude().doubleValue(), 0.0000001);

        PointType pointBerlin = new PointType("52.5200066,13.4049540");

        LocationItem locationParis = new LocationItem("paris");
        locationParis.setState(pointParis);
        LocationItem locationBerlin = new LocationItem("berlin");
        locationBerlin.setState(pointBerlin);

        DecimalType distance = locationParis.distanceFrom(pointParis);
        assertEquals(0, distance.intValue());

        double parisBerlin = locationParis.distanceFrom(pointBerlin).doubleValue();
        assertEquals(878400, 50, parisBerlin);

        double gravParis = pointParis.getGravity().doubleValue();
        assertEquals(gravParis, 9.809, 0.001);

        // Check canonization of position
        PointType point3 = new PointType("-100,200");
        double lat3 = point3.getLatitude().doubleValue();
        double lon3 = point3.getLongitude().doubleValue();
        assertTrue(lat3 > -90);
        assertTrue(lat3 < 90);
        assertTrue(lon3 < 180);
        assertTrue(lon3 > -180);

        PointType pointTest1 = new PointType("48.8566140,2.3522219,118");
        PointType pointTest2 = new PointType(pointTest1.toString());
        assertEquals(pointTest1.getAltitude().longValue(), pointTest2.getAltitude().longValue());
        assertEquals(pointTest1.getLatitude().longValue(), pointTest2.getLatitude().longValue());
        assertEquals(pointTest1.getLongitude().longValue(), pointTest2.getLongitude().longValue());
    }

}
