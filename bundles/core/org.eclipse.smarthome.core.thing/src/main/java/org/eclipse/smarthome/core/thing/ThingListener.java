package org.eclipse.smarthome.core.thing;

import java.util.EventListener;

import org.eclipse.smarthome.core.types.State;

/**
 * {@link ThingListener} can be registered at a {@link Thing} object.
 * 
 * @see Thing#addThingListener(ThingListener)
 * @author Dennis Nobel - Initial contribution
 */
public interface ThingListener extends EventListener {

    /**
     * Channel updated is called when the state of a channel was updated.
     * 
     * @param channelUID
     *            unique identifier of a channel
     * @param state
     *            state
     */
    void channelUpdated(ChannelUID channelUID, State state);

}
