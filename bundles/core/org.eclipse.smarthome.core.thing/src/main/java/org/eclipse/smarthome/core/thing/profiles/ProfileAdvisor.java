/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;

/**
 * Implementors can give advice which {@link Profile}s can/should be used for a given link.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ProfileAdvisor {

    /**
     * Return all custom profiles which can be applied to the given link.
     *
     * Please note: The default profiles must not returned here.
     *
     * @param link the Link
     * @param item the linked item (for convenience)
     * @param channel the linked channel (for convenience)
     * @return a collection of profile type IDs.
     */
    Collection<ProfileTypeUID> getApplicableProfileTypeUIDs(ItemChannelLink link, Item item, Channel channel);

    /**
     * Suggest a custom profile for the given link, if applicable at all.
     *
     * Please note:
     * <ul>
     * <li>This will override any default behavior
     * <li>A "profile" configuration on the link will override this suggestion
     * </ul>
     *
     * @param link the Link
     * @param item the linked item (for convenience)
     * @param channel the linked channel (for convenience)
     * @return the profile identifier or {@code null} if this advisor
     */
    @Nullable
    ProfileTypeUID getSuggestedProfileTypeUID(ItemChannelLink link, Item item, Channel channel);

}
