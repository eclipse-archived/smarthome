package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class StringItemTest {

    @Test
    public void setStringType() {
        StringItem item = new StringItem("test");
        State state = new StringType("foobar");
        item.setState(state);
        assertEquals(state, item.getState());
    }

    @Test
    public void setDateTimeTypeType() {
        StringItem item = new StringItem("test");
        State state = new DateTimeType();
        item.setState(state);
        assertEquals(state, item.getState());
    }

    @Test
    public void testUndefType() {
        StringItem item = new StringItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        StringItem item = new StringItem("test");
        StateUtil.testAcceptedStates(item);
    }
}
