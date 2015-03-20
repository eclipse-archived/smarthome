/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import static org.junit.Assert.assertEquals;
import javax.measure.unit.SI;

import org.eclipse.smarthome.core.library.items.LocationItem;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class PointTypeTest {

    @Test
    public void testDistance() {
        PointType pointParis = new PointType("48.8566140,2.3522219");
        
        assertEquals(2.3522219, pointParis.getLongitude().doubleValue(), 0.0000001);
        assertEquals(48.856614, pointParis.getLatitude().doubleValue(), 0.0000001);
        
        PointType pointBerlin = new PointType("52.5200066,13.4049540");

        LocationItem locationParis = new LocationItem("paris");
        locationParis.setState(pointParis);
        LocationItem locationBerlin = new LocationItem("berlin");
        locationBerlin.setState(pointBerlin);

        MeasureType distance = locationParis.distanceFrom(pointParis);
        assertEquals(0, distance.intValue());

        MeasureType parisBerlin = locationParis.distanceFrom(pointBerlin);
        double distInKm = parisBerlin.toUnit(SI.KILOMETER).doubleValue();
        assertEquals(878.4, 0.5, distInKm);

        double gravParis = pointParis.getGravity().doubleValue();
        assertEquals(gravParis, 9.809, 0.001);

        // Check canonization of position
        PointType point3 = new PointType("-100,200");
        double lat3 = point3.getLatitude().doubleValue();
        double lon3 = point3.getLongitude().doubleValue();
        assertEquals(-80,lat3,0);
        assertEquals(20,lon3,0);

    }

}
