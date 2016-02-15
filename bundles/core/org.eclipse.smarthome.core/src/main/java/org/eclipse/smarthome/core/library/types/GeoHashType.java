/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;

/**
 * This type can be used for items that are dealing with GPS or
 * geofencing. Geohashes adds, by construct
 * a notion of precision depending on the length of the hash
 *
 * https://en.wikipedia.org/wiki/Geohash
 *
 * @author Gaël L'hopital
 *
 */
public class GeoHashType implements Command, State {

    private final GeoHash geoHash;

    /**
     * Default constructor creates a point at sea level where the equator
     * (0° latitude) and the prime meridian (0° longitude) intersect.
     * A nullary constructor is needed by
     * {@link org.eclipse.smarthome.core.internal.items.ItemUpdater#receiveUpdate})
     */
    public GeoHashType() {
        this.geoHash = GeoHash.withCharacterPrecision(0, 0, 12);
    }

    public GeoHashType(String geoHash) {
        if (geoHash == null) {
            throw new IllegalArgumentException("Constructor argument must not be null");
        }
        if (!geoHash.isEmpty()) {

            this.geoHash = GeoHash.fromGeohashString(geoHash);
        } else {
            throw new IllegalArgumentException("Constructor argument must not be blank");
        }
    }

    public GeoHashType(PointType point, DecimalType precision) {
        if (point == null || precision == null) {
            throw new IllegalArgumentException("Constructor arguments can not be null");
        }
        this.geoHash = GeoHash.withCharacterPrecision(point.getLatitude().doubleValue(),
                point.getLongitude().doubleValue(), precision.intValue());
    }

    public static GeoHashType valueOf(String value) {
        return new GeoHashType(value);
    }

    public DecimalType getLongitude() {
        return new DecimalType(geoHash.getPoint().getLongitude());
    }

    public DecimalType getLatitude() {
        return new DecimalType(geoHash.getPoint().getLatitude());
    }

    // Returns the radius of circle contained in the bounding box
    public DecimalType getIncircleRadius() {
        WGS84Point upperLeft = geoHash.getBoundingBox().getUpperLeft();
        WGS84Point lowerRight = geoHash.getBoundingBox().getLowerRight();
        WGS84Point lowerLeft = new WGS84Point(lowerRight.getLatitude(), upperLeft.getLongitude());
        double boxHeight = VincentyGeodesy.distanceInMeters(upperLeft, lowerLeft);
        return new DecimalType(boxHeight / 2);
    }

    // Returns the radius of circle containing in the bounding box
    public DecimalType getExcircleRadius() {
        WGS84Point upperLeft = geoHash.getBoundingBox().getUpperLeft();
        WGS84Point lowerRight = geoHash.getBoundingBox().getLowerRight();
        double boxWidth = VincentyGeodesy.distanceInMeters(upperLeft, lowerRight);
        return new DecimalType(boxWidth / 2);
    }

    public boolean contains(PointType pointType) {
        WGS84Point point = new WGS84Point(pointType.getLatitude().doubleValue(),
                pointType.getLongitude().doubleValue());
        return geoHash.contains(point);
    }

    public boolean contains(GeoHashType geoHashType) {
        String thisHash = this.toString();
        String otherHash = geoHashType.toString();

        return otherHash.startsWith(thisHash);
    }

    public PointType toPointType() {
        return new PointType(new DecimalType(geoHash.getBoundingBox().getCenterPoint().getLatitude()),
                new DecimalType(geoHash.getBoundingBox().getCenterPoint().getLongitude()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GeoHashType)) {
            return false;
        }
        GeoHashType other = (GeoHashType) obj;
        if (!geoHash.equals(other.geoHash)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return geoHash.toBase32();
    }

    /**
     * <p>
     * Formats the value of this type according to a pattern (@see {@link Formatter}).
     * </p>
     *
     * @param pattern the pattern to use containing indexes to reference the
     *            single elements of this type.
     */
    @Override
    public String format(String pattern) {
        return String.format(pattern, toString());
    }

    @Override
    public int hashCode() {
        return geoHash.hashCode();
    }

}
