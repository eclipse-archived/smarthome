/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;

import com.google.common.base.Joiner;

/**
 * {@link ThingHelper} provides a utility method to create and bind items.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Andre Fuechsel - graceful creation of items and links
 * @author Benedikt Niehues - Fix ESH Bug 450236
 *         https://bugs.eclipse.org/bugs/show_bug.cgi?id=450236 - Considering
 *         ThingTypeDescription
 * @author Dennis Nobel - Removed createAndBindItems method
 */
public class ThingHelper {

    /**
     * Indicates whether two {@link Thing}s are technical equal.
     * 
     * @param a
     *            Thing object
     * @param b
     *            another Thing object
     * @return true whether a and b are equal, otherwise false
     */
    public static boolean equals(Thing a, Thing b) {
        if (!a.getUID().equals(b.getUID())) {
            return false;
        }
        if (a.getBridgeUID() == null && b.getBridgeUID() != null) {
            return false;
        }
        if (a.getBridgeUID() != null && !a.getBridgeUID().equals(b.getBridgeUID())) {
            return false;
        }
        // configuration
        if (a.getConfiguration() == null && b.getConfiguration() != null) {
            return false;
        }
        if (a.getConfiguration() != null && !a.getConfiguration().equals(b.getConfiguration())) {
            return false;
        }
        // channels
        List<Channel> channelsOfA = a.getChannels();
        List<Channel> channelsOfB = b.getChannels();
        if (channelsOfA.size() != channelsOfB.size()) {
            return false;
        }
        if (!toString(channelsOfA).equals(toString(channelsOfB))) {
            return false;
        }
        return true;
    }

    private static String toString(List<Channel> channels) {
        List<String> strings = new ArrayList<>(channels.size());
        for (Channel channel : channels) {
            strings.add(channel.getUID().toString() + '#' + channel.getAcceptedItemType());
        }
        Collections.sort(strings);
        return Joiner.on(',').join(strings);
    }

    public static void addChannelsToThing(Thing thing, Collection<Channel> channels) {
        ((ThingImpl) thing).getChannelsMutable().addAll(channels);
    }
}
