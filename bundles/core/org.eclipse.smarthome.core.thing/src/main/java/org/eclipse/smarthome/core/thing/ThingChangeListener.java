package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.items.Item;


public interface ThingChangeListener {

    void statusUpdated(ThingStatus thingStatus);
    
    void itemLinked(Channel channel, Item item);
    
    void itemUnlinked(Channel channel, Item item);
    
}
