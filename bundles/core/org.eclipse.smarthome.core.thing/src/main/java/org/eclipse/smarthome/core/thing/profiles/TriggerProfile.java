/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;

/**
 * A {@link TriggerProfile} specifies the communication between the framework and the handler for trigger channels.
 *
 * Although trigger channels by their nature do not have a state, it becomes possible to link such trigger channels to
 * items using such a profile.
 * <p>
 * The main purpose of a {@link TriggerProfile} is to listen to triggered events and use them to calculate a meaningful
 * state.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface TriggerProfile extends Profile {

    void onTrigger(@NonNull EventPublisher eventPublisher, @NonNull ItemChannelLink link, String event,
            @NonNull Item item);

}
