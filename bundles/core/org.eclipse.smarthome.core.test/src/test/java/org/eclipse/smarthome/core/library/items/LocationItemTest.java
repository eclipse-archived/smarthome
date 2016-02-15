/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.GeoHashType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.junit.Test;

/**
 * @author Gaël L'hopital
 */
public class LocationItemTest {

    @Test
    public void testDistance() {
        PointType pointParis = new PointType("48.8566140,2.3522219");
        PointType pointBerlin = new PointType("52.5200066,13.4049540");

        LocationItem locationParis = new LocationItem("paris");
        locationParis.setState(pointParis);
        LocationItem locationBerlin = new LocationItem("berlin");
        locationBerlin.setState(pointBerlin);

        DecimalType distance = locationParis.distanceFrom(locationParis);
        assertEquals(distance.intValue(), 0);

        double parisBerlin = locationParis.distanceFrom(locationBerlin).doubleValue();
        assertEquals(parisBerlin, 878400, 50);

        double distanceError = locationParis.distanceFrom(null).doubleValue();
        assertEquals(distanceError, -1, 0);
    }

    @Test
    public void testMixPointTypeGeoHashType() {
        GeoHashType geoHashParis = GeoHashType.valueOf("u09tvw0f6szy");
        PointType pointBerlin = PointType.valueOf("52.5200066,13.4049540");

        LocationItem locationParis = new LocationItem("paris");
        locationParis.setState(geoHashParis);
        LocationItem locationBerlin = new LocationItem("berlin");
        locationBerlin.setState(pointBerlin);

        DecimalType distance = locationParis.distanceFrom(locationParis);
        assertEquals(0, distance.intValue());

        double parisBerlin = locationParis.distanceFrom(locationBerlin).doubleValue();
        assertEquals(parisBerlin, 878400, 50);
    }

}
