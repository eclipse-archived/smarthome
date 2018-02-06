package org.eclipse.smarthome.magic.binding.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

/**
 * ThingHandler for a thing that goes online after 15 seconds
 *
 * @author Stefan Triller - initial contribution
 *
 */
@NonNullByDefault
public class MagicDelayedOnlineHandler extends BaseThingHandler {

    public static int DELAY = 15;

    public MagicDelayedOnlineHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        // schedule delayed job to set the thing to ONLINE
        scheduler.schedule(() -> updateStatus(ThingStatus.ONLINE), DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // empty because it is a dummy thing
    }

}
