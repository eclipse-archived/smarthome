package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.core.items.Item;

public interface ItemLinkChangeListener {

    void itemLinked(Item item);
    
    void itemUnlinked(Item item);
}
