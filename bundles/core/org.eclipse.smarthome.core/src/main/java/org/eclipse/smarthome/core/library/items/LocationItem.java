/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * A LocationItem can be used to store GPS related informations, addresses...
 * This is useful for location awareness related functions
 *
 * @author Gaël L'hopital
 *
 */
public class LocationItem extends GenericItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();

    static {
        acceptedDataTypes.add(PointType.class);
        acceptedDataTypes.add(UnDefType.class);

        acceptedCommandTypes.add(RefreshType.class);
        acceptedCommandTypes.add(PointType.class);
    }

    public LocationItem(String name) {
        super(CoreItemFactory.LOCATION, name);
    }

    public void send(PointType command) {
        internalSend(command);
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return Collections.unmodifiableList(acceptedDataTypes);
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return Collections.unmodifiableList(acceptedCommandTypes);
    }

    /**
     * Compute the distance with another Point type,
     * http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-
     * java
     *
     * @param away : the point to calculate the distance with
     * @return distance between the two points in meters
     */
    public DecimalType distanceFrom(LocationItem awayItem) {
        if (awayItem != null && awayItem.state instanceof PointType && this.state instanceof PointType) {
            PointType thisPoint = (PointType) this.state;
            PointType awayPoint = (PointType) awayItem.state;
            return thisPoint.distanceFrom(awayPoint);
        }
        return new DecimalType(-1);
    }
}
