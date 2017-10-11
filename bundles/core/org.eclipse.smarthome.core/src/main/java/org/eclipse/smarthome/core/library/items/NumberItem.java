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
    private String unit;

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
        if (isAcceptedState(acceptedDataTypes, state)) {
            super.setState(state);
        } else {
            logSetTypeError(state);
        }
    }

    /**
     * Returns the optional unit string configured for this {@link NumberItem}.
     *
     * @return the optional unit string configured for this {@link NumberItem}.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the optional unit string configured for this {@link NumberItem}. The unit string will be used to convert a
     * {@link QuantityType} to the corresponding {@link Unit}.
     *
     * @param unit the optional unit string configured for this {@link NumberItem}.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

}
