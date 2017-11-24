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
 * The {@link BaseDmxChannel} represents a basic DMX channel
 *
 * @author Jan N. Klug - Initial contribution
 */

public class BaseDmxChannel implements Comparable<BaseDmxChannel> {
    public static final int MIN_CHANNEL_ID = 1;
    public static final int MAX_CHANNEL_ID = 512;

    protected static final Pattern CHANNEL_PATTERN = Pattern.compile("^(\\d*(?=:))?:?(\\d*)\\/?(\\d*)?$");

    // this static declaration is needed because of the static fromString method
    private static final Logger logger = LoggerFactory.getLogger(BaseDmxChannel.class);

    private int universeId;
    private final int dmxChannelId;

    /**
     * BaseChannel object
     *
     * @param universeId integer for DMX universe
     * @param dmxChannelId integer for DMX channel
     */
    public BaseDmxChannel(int universeId, int dmxChannelId) {
        this.universeId = universeId;
        this.dmxChannelId = Util.coerceToRange(dmxChannelId, MIN_CHANNEL_ID, MAX_CHANNEL_ID, logger, "channelId");
    }

    /**
     * copy constructor
     *
     * @param dmxChannel a BaseChannel object
     */
    public BaseDmxChannel(BaseDmxChannel dmxChannel) {
        this.universeId = dmxChannel.getUniverseId();
        this.dmxChannelId = dmxChannel.getChannelId();
        logger.trace("created DMX channel {} in universe {} ", dmxChannelId, universeId);
    }

    /**
     * get DMX channel
     *
     * @return a integer for the DMX channel
     */
    public int getChannelId() {
        return dmxChannelId;
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
    public int compareTo(BaseDmxChannel otherDmxChannel) {
        if (otherDmxChannel == null) {
            return -1;
        }
        int universeCompare = new Integer(getUniverseId()).compareTo(new Integer(otherDmxChannel.getUniverseId()));
        if (universeCompare == 0) {
            return new Integer(getChannelId()).compareTo(new Integer(otherDmxChannel.getChannelId()));
        } else {
            return universeCompare;
        }
    }

    @Override
    public String toString() {
        return universeId + ":" + dmxChannelId;
    }

    /**
     * parse a BaseChannel list from string
     *
     * @param dmxChannelString channel string in format [universe:]channel[/width],...
     * @param defaultUniverseId default id to use if universe not specified
     * @return a List of BaseChannels
     */
    public static List<BaseDmxChannel> fromString(String dmxChannelString, int defaultUniverseId)
            throws IllegalArgumentException {
        List<BaseDmxChannel> dmxChannels = new ArrayList<BaseDmxChannel>();

        Stream.of(dmxChannelString.split(",")).forEach(singleDmxChannelString -> {
            int dmxChannelId, dmxChannelWidth;
            Matcher channelMatch = CHANNEL_PATTERN.matcher(singleDmxChannelString);
            if (channelMatch.matches()) {
                final int universeId = (channelMatch.group(1) == null) ? defaultUniverseId
                        : Integer.valueOf(channelMatch.group(1));
                dmxChannelWidth = channelMatch.group(3).equals("") ? 1 : Integer.valueOf(channelMatch.group(3));
                dmxChannelId = Integer.valueOf(channelMatch.group(2));
                logger.trace("parsed channel string {} to universe {}, id {}, width {}", singleDmxChannelString,
                        universeId, dmxChannelId, dmxChannelWidth);
                IntStream.range(dmxChannelId, dmxChannelId + dmxChannelWidth)
                        .forEach(c -> dmxChannels.add(new BaseDmxChannel(universeId, c)));
            } else {
                throw new IllegalArgumentException("invalid channel definition" + singleDmxChannelString);
            }
        });

        return dmxChannels;
    }
}
