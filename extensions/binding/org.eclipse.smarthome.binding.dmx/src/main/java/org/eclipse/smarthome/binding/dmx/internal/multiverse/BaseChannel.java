/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.dmx.internal.multiverse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.smarthome.binding.dmx.internal.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseChannel} represents a basic DMX channel
 *
 * @author Jan N. Klug - Initial contribution
 */

public class BaseChannel implements Comparable<BaseChannel> {
    public static final int MIN_CHANNEL_ID = 1;
    public static final int MAX_CHANNEL_ID = 512;

    protected static final Pattern CHANNEL_PATTERN = Pattern.compile("^(\\d*(?=:))?:?(\\d*)\\/?(\\d*)?$");

    // this static declaration is needed because of the static fromString method
    private static final Logger logger = LoggerFactory.getLogger(BaseChannel.class);

    private int universeId, channelId;

    /**
     * BaseChannel object
     *
     * @param universeId integer for DMX universe
     * @param channelId integer for DMX channel
     */
    public BaseChannel(int universeId, int channelId) {
        this.universeId = universeId;
        this.channelId = Util.coerceToRange(channelId, MIN_CHANNEL_ID, MAX_CHANNEL_ID, logger, "channelId");
    }

    /**
     * copy constructor
     *
     * @param channel a BaseChannel object
     */
    public BaseChannel(BaseChannel channel) {
        this.universeId = channel.getUniverseId();
        this.channelId = channel.getChannelId();
        logger.trace("created DMX channel {} in universe {} ", channelId, universeId);
    }

    /**
     * get DMX channel
     *
     * @return a integer for the DMX channel
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * get DMX universe
     *
     * @return a integer for the DMX universe
     */
    public int getUniverseId() {
        return universeId;
    }

    /**
     * set the DMX universe id
     *
     * @param universeId a integer for the new universe
     */
    public void setUniverseId(int universeId) {
        this.universeId = universeId;
    }

    @Override
    public int compareTo(BaseChannel otherChannel) {
        if (otherChannel == null) {
            return -1;
        }
        int universeCompare = new Integer(getUniverseId()).compareTo(new Integer(otherChannel.getUniverseId()));
        if (universeCompare == 0) {
            return new Integer(getChannelId()).compareTo(new Integer(otherChannel.getChannelId()));
        } else {
            return universeCompare;
        }
    }

    @Override
    public String toString() {
        return universeId + ":" + channelId;
    }

    /**
     * parse a BaseChannel list from string
     *
     * @param channelString channel string in format [universe:]channel[/width],...
     * @param defaultUniverseId default id to use if universe not specified
     * @return a List of BaseChannels
     */
    public static List<BaseChannel> fromString(String channelString, int defaultUniverseId)
            throws IllegalArgumentException {
        List<BaseChannel> channels = new ArrayList<BaseChannel>();

        Stream.of(channelString.split(",")).forEach(singleChannelString -> {
            int channelId, channelWidth;
            Matcher channelMatch = CHANNEL_PATTERN.matcher(singleChannelString);
            if (channelMatch.matches()) {
                final int universeId = (channelMatch.group(1) == null) ? defaultUniverseId
                        : Integer.valueOf(channelMatch.group(1));
                channelWidth = channelMatch.group(3).equals("") ? 1 : Integer.valueOf(channelMatch.group(3));
                channelId = Integer.valueOf(channelMatch.group(2));
                logger.trace("parsed channel string {} to universe {}, id {}, width {}", singleChannelString,
                        universeId, channelId, channelWidth);
                IntStream.range(channelId, channelId + channelWidth)
                        .forEach(c -> channels.add(new BaseChannel(universeId, c)));
            } else {
                throw new IllegalArgumentException("invalid channel definition" + singleChannelString);
            }
        });

        return channels;
    }
}
