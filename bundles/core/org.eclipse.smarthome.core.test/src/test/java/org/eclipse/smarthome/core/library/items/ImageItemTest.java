package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class ImageItemTest {

    @Test
    public void testRawType() {
        ImageItem item = new ImageItem("test");
        State state = new RawType(new byte[0], "application/octet-stream");
        item.setState(state);
        assertEquals(state, item.getState());
    }

    @Test
    public void testUndefType() {
        ImageItem item = new ImageItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        DateTimeItem item = new DateTimeItem("test");
        StateUtil.testAcceptedStates(item);
    }

}
