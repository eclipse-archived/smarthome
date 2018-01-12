/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
    public ThingUID getLinkedUID() {
        return thingUID;
    }

}
