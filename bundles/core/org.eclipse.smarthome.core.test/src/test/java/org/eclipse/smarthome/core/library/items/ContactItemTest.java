package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class ContactItemTest {

    @Test
    public void testOpenCloseType() {
        ContactItem item = new ContactItem("test");
        item.setState(OpenClosedType.OPEN);
        assertEquals(OpenClosedType.OPEN, item.getState());

        item.setState(OpenClosedType.CLOSED);
        assertEquals(OpenClosedType.CLOSED, item.getState());
    }

    @Test
    public void testUndefType() {
        ContactItem item = new ContactItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        ContactItem item = new ContactItem("test");
        StateUtil.testAcceptedStates(item);
    }
}
