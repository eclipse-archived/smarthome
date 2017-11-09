/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.Dimension;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * A NumberItem has a decimal value and is usually used for all kinds
 * of sensors, like temperature, brightness, wind, etc.
 * It can also be used as a counter or as any other thing that can be expressed
 * as a number.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class NumberItem extends GenericItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();
    private Dimension dimension;

    static {
        acceptedDataTypes.add(DecimalType.class);
        acceptedDataTypes.add(UnDefType.class);
        acceptedDataTypes.add(QuantityType.class);

        acceptedCommandTypes.add(DecimalType.class);
        acceptedCommandTypes.add(RefreshType.class);
        acceptedCommandTypes.add(QuantityType.class);
    }

    public NumberItem(@NonNull String name) {
        super(CoreItemFactory.NUMBER, name);
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return Collections.unmodifiableList(acceptedDataTypes);
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return Collections.unmodifiableList(acceptedCommandTypes);
    }

    public void send(DecimalType command) {
        internalSend(command);
    }

    /**
     * Returns the {@link Dimension} associated with this {@link NumberItem}.
     * May be null.
     *
     * @return the {@link Dimension} associated with this {@link NumberItem}. May be null.
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Set a {@link Dimension} for this {@link NumberItem}. This enables automatic {@link Unit} conversion from
     * {@link QuantityType}s from channel-types with {@link Dimension} support.
     *
     */
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setState(State state) {
        // DecimalType update for a NumberItem with dimension, convert to QuantityType:
        if (state instanceof DecimalType && dimension != null) {
            Unit<?> unit = getUnit();
            if (unit != null) {
                super.setState(new QuantityType(((DecimalType) state).doubleValue(), unit));
                return;
            }
        }

        // QuantityType update, check unit and convert if necessary:
        if (state instanceof QuantityType) {
            Unit<?> unit = getUnit();
            if (unit != null && !((QuantityType) state).getUnit().equals(unit)) {
                super.setState(((QuantityType) state).toUnit(unit));
                return;
            }
        }

        if (isAcceptedState(acceptedDataTypes, state)) {
            super.setState(state);
        } else {
            logSetTypeError(state);
        }
    }

    /**
     * Returns the optional unit symbol for this {@link NumberItem}.
     *
     * @return the optional unit symbol for this {@link NumberItem}.
     */
    public String getUnitSymbol() {
        Unit<?> unit = getUnit();
        return unit != null ? unit.toString() : null;
    }

    /**
     * Derive the unit for this item by the following priority:
     * <ul>
     * <li>the unit from the current item state</li>
     * <li>the unit parsed from the state description</li>
     * <li>the default system unit</li>
     * </ul>
     *
     * @return the {@link Unit} for this item if available, {@code null} otherwise.
     */
    private @Nullable Unit<?> getUnit() {
        if (getState() instanceof QuantityType) {
            return ((QuantityType) getState()).getUnit();
        }

        if (getStateDescription() != null) {
            Unit<?> stateDescriptionUnit = unitProvider.parseUnit(getStateDescription().getPattern());
            if (stateDescriptionUnit != null) {
                return stateDescriptionUnit;
            }
        }

        if (dimension != null) {
            return unitProvider.getUnit(dimension);
        }

        return null;
    }

}
