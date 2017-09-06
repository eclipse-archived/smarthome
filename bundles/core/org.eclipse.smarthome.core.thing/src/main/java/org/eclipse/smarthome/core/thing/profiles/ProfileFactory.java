/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;

/**
 * Implementors are capable of creating a {@link Profile} for one or several links.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public interface ProfileFactory {

    /**
     * Create a {@link Profile} instance for the given link
     *
     * @param link the ItemChannelLink for which the profile should be created
     * @param item the linked item (for convenience)
     * @param channel the linked channel (for convenience)
     * @return a profile instance or {@code null} if this factory does not handle the given link
     */
    @Nullable
    Profile createProfile(@NonNull ItemChannelLink link, @NonNull Item item, @NonNull Channel channel);

}
