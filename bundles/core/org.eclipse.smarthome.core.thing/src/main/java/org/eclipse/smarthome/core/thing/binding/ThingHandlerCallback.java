package org.eclipse.smarthome.core.thing.binding;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;

public interface ThingHandlerCallback {
    
    void stateUpdated(ChannelUID channelUID, State state);
    
    void thingUpdated(Thing thing);
    
}
