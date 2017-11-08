/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.profiles;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;

/**
 * This profile allows a channel of the "system:rawbutton" type to be bound to an item.
 *
 * It reads the triggered events and uses the item's current state and toggles it once it detects that the
 * button was pressed.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class RawButtonToggleProfile implements TriggerProfile {

    public static final ProfileTypeUID UID = new ProfileTypeUID(ProfileTypeUID.SYSTEM_SCOPE, "rawbutton-toggle",
            "Raw Button Toggle");

    @Override
    public void onTrigger(EventPublisher eventPublisher, ItemChannelLink link, String event, Item item) {
        if (CommonTriggerEvents.PRESSED.equals(event)) {
            if (item.getAcceptedCommandTypes().contains(OnOffType.class)) {
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                    eventPublisher.post(ItemEventFactory.createCommandEvent(link.getItemName(), OnOffType.OFF,
                            link.getLinkedUID().toString()));
                } else {
                    eventPublisher.post(ItemEventFactory.createCommandEvent(link.getItemName(), OnOffType.ON,
                            link.getLinkedUID().toString()));
                }
            }
        }
    }

}
