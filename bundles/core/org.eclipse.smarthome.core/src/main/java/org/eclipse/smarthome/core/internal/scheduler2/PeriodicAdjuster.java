/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.scheduler2;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * This is a Temporal Adjuster that takes a list of delays.
 *
 * The given delays are used sequentially.
 * If no more values are present, the last value is re-used.
 *
 * @author Peter Kriens - initial contribution and API
 *
 */
class PeriodicAdjuster implements TemporalAdjuster {

    private final Iterator<Duration> iterator;
    private Duration current;

    PeriodicAdjuster(Duration first, Duration... delays) {
        iterator = Stream.concat(Stream.of(first), Arrays.stream(delays)).iterator();
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (iterator.hasNext()) {
            current = iterator.next();
        }
        return temporal.plus(current);
    }

}
