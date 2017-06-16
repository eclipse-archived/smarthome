package org.eclipse.smarthome.core.library.items;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.junit.Test;

/**
 *
 * @author Stefan Triller - Initial version
 *
 */
public class PlayerItemTest {

    @Test
    public void setPlayPause() {
        PlayerItem item = new PlayerItem("test");
        item.setState(PlayPauseType.PLAY);
        assertEquals(PlayPauseType.PLAY, item.getState());

        item.setState(PlayPauseType.PAUSE);
        assertEquals(PlayPauseType.PAUSE, item.getState());
    }

    @Test
    public void setRewindFastforward() {
        PlayerItem item = new PlayerItem("test");
        item.setState(RewindFastforwardType.REWIND);
        assertEquals(RewindFastforwardType.REWIND, item.getState());

        item.setState(RewindFastforwardType.FASTFORWARD);
        assertEquals(RewindFastforwardType.FASTFORWARD, item.getState());
    }

    @Test
    public void testUndefType() {
        PlayerItem item = new PlayerItem("test");
        StateUtil.testUndefStates(item);
    }

    @Test
    public void testAcceptedStates() {
        PlayerItem item = new PlayerItem("test");
        StateUtil.testAcceptedStates(item);
    }

}
