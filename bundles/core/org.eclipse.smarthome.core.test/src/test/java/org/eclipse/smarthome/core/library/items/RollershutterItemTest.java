package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

public class RollershutterItemTest {

    @Test
    public void setState_stateDown_returnPercent100() {
        RollershutterItem sut = new RollershutterItem("Test");
        State state = UpDownType.DOWN;
        sut.setState(state);
        assertEquals(PercentType.HUNDRED, sut.getState());
    }

    @Test
    public void setState_stateUp_returnPercent0() {
        RollershutterItem sut = new RollershutterItem("Test");
        State state = UpDownType.UP;
        sut.setState(state);
        assertEquals(PercentType.ZERO, sut.getState());
    }

    @Test
    public void setState_statePercent50_returnPercent50() {
        RollershutterItem sut = new RollershutterItem("Test");
        State state = new PercentType(50);
        sut.setState(state);
        assertEquals(state, sut.getState());
    }

    @Test
    public void setState_stateDecimal050_returnPercent50() {
        RollershutterItem sut = new RollershutterItem("Test");
        State state = new DecimalType(0.50);
        sut.setState(state);
        assertEquals(new PercentType(50), sut.getState());
    }

}
