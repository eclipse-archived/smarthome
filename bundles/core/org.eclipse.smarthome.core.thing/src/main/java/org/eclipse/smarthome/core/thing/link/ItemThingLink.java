/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.link;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * {@link ItemThingLink} defines a link between an {@link Item} and a {@link Thing}.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ItemThingLink extends AbstractLink {

    private final ThingUID thingUID;

    ItemThingLink() {
        super();
        this.thingUID = null;
    }

    public ItemThingLink(String itemName, ThingUID thingUID) {
        super(itemName);
        this.thingUID = thingUID;
    }

    @Override
    public ThingUID getUID() {
        return thingUID;
    }

}
