/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.items;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * A ColorItem can be used for color values, e.g. for LED lights
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ColorItem extends DimmerItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();

    static {
        acceptedDataTypes.add(OnOffType.class);
        acceptedDataTypes.add(PercentType.class);
        acceptedDataTypes.add(HSBType.class);
        acceptedDataTypes.add(UnDefType.class);

        acceptedCommandTypes.add(OnOffType.class);
        acceptedCommandTypes.add(IncreaseDecreaseType.class);
        acceptedCommandTypes.add(PercentType.class);
        acceptedCommandTypes.add(HSBType.class);
        acceptedCommandTypes.add(RefreshType.class);
    }

    public ColorItem(String name) {
        super(CoreItemFactory.COLOR, name);
    }

    public void send(HSBType command) {
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
     * {@inheritDoc}
     */
    @Override
    public void setState(State state) {
        State currentState = this.state;

        if (currentState instanceof HSBType) {
            DecimalType hue = ((HSBType) currentState).getHue();
            PercentType saturation = ((HSBType) currentState).getSaturation();
            // we map ON/OFF values to dark/bright, so that the hue and saturation values are not changed
            if (state == OnOffType.OFF) {
                super.setState(new HSBType(hue, saturation, PercentType.ZERO));
            } else if (state == OnOffType.ON) {
                super.setState(new HSBType(hue, saturation, PercentType.HUNDRED));
            } else if (state instanceof PercentType && !(state instanceof HSBType)) {
                super.setState(new HSBType(hue, saturation, (PercentType) state));
            } else {
                super.setState(state);
            }
        } else {
            // we map ON/OFF values to black/white and percentage values to grey scale
            if (state == OnOffType.OFF) {
                super.setState(HSBType.BLACK);
            } else if (state == OnOffType.ON) {
                super.setState(HSBType.WHITE);
            } else if (state instanceof PercentType && !(state instanceof HSBType)) {
                super.setState(new HSBType(DecimalType.ZERO, PercentType.ZERO, (PercentType) state));
            } else {
                super.setState(state);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getStateAs(Class<? extends State> typeClass) {
        if (typeClass == HSBType.class) {
            return this.state;
        } else if (typeClass == OnOffType.class) {
            if (state instanceof HSBType) {
                HSBType hsbState = (HSBType) state;
                // if brightness is not completely off, we consider the state to be on
                return hsbState.getBrightness().equals(PercentType.ZERO) ? OnOffType.OFF : OnOffType.ON;
            }
        } else if (typeClass == DecimalType.class) {
            if (state instanceof HSBType) {
                HSBType hsbState = (HSBType) state;
                return new DecimalType(
                        hsbState.getBrightness().toBigDecimal().divide(new BigDecimal(100), 8, RoundingMode.UP));
            }
        } else if (typeClass == PercentType.class) {
            if (state instanceof HSBType) {
                HSBType hsbState = (HSBType) state;
                return new PercentType(hsbState.getBrightness().toBigDecimal());
            }
        }
        return super.getStateAs(typeClass);
    }
}
