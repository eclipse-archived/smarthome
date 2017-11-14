/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;
import static org.eclipse.smarthome.binding.astro.internal.job.Job.scheduleEvent;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.model.Eclipse;
import org.eclipse.smarthome.binding.astro.internal.model.Moon;
import org.eclipse.smarthome.binding.astro.internal.model.MoonPhase;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;

/**
 * Daily scheduled jobs for Moon planet
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class DailyJobMoon extends AbstractJob {

    private final AstroThingHandler handler;

    /**
     * Constructor
     *
     * @param thingUID the Thing UID
     * @param handler the {@link AstroThingHandler} instance
     * @throws IllegalArgumentException if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobMoon(String thingUID, AstroThingHandler handler) {
        super(thingUID);
        checkArgument(handler != null, "The handler must not be null");
        this.handler = handler;
    }

    @Override
    public void run() {
        handler.publishDailyInfo();
        String thingUID = getThingUID();
        logger.info("Scheduled Astro event-jobs for thing {}", thingUID);

        Planet planet = handler.getPlanet();
        if (planet == null) {
            logger.error("Planet not instantiated");
            return;
        }
        Moon moon = (Moon) planet;
        scheduleEvent(thingUID, handler, moon.getRise().getStart(), EVENT_START, EVENT_CHANNEL_ID_RISE, false);
        scheduleEvent(thingUID, handler, moon.getSet().getEnd(), EVENT_END, EVENT_CHANNEL_ID_SET, false);

        MoonPhase moonPhase = moon.getPhase();
        scheduleEvent(thingUID, handler, moonPhase.getFirstQuarter(), EVENT_PHASE_FIRST_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE, false);
        scheduleEvent(thingUID, handler, moonPhase.getThirdQuarter(), EVENT_PHASE_THIRD_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE, false);
        scheduleEvent(thingUID, handler, moonPhase.getFull(), EVENT_PHASE_FULL, EVENT_CHANNEL_ID_MOON_PHASE, false);
        scheduleEvent(thingUID, handler, moonPhase.getNew(), EVENT_PHASE_NEW, EVENT_CHANNEL_ID_MOON_PHASE, false);

        Eclipse eclipse = moon.getEclipse();
        scheduleEvent(thingUID, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE, false);
        scheduleEvent(thingUID, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE, false);

        scheduleEvent(thingUID, handler, moon.getPerigee().getDate(), EVENT_PERIGEE, EVENT_CHANNEL_ID_PERIGEE, false);
        scheduleEvent(thingUID, handler, moon.getApogee().getDate(), EVENT_APOGEE, EVENT_CHANNEL_ID_APOGEE, false);
    }

    @Override
    public String toString() {
        return "Daily job moon " + getThingUID();
    }

}
