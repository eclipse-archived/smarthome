/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;
import static org.eclipse.smarthome.binding.astro.internal.job.Job.*;
import static org.eclipse.smarthome.binding.astro.internal.model.SunPhaseName.*;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.binding.astro.internal.model.SunEclipse;

/**
 * Daily scheduled jobs For Sun planet
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class DailyJobSun extends AbstractJob {

    private final AstroThingHandler handler;

    /**
     * Constructor
     *
     * @param thingUID the Thing UID
     * @param handler the {@link AstroThingHandler} instance
     * @throws IllegalArgumentException
     *             if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobSun(String thingUID, AstroThingHandler handler) {
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
        Sun sun = (Sun) planet;
        scheduleRange(thingUID, handler, sun.getRise(), EVENT_CHANNEL_ID_RISE);
        scheduleRange(thingUID, handler, sun.getSet(), EVENT_CHANNEL_ID_SET);
        scheduleRange(thingUID, handler, sun.getNoon(), EVENT_CHANNEL_ID_NOON);
        scheduleRange(thingUID, handler, sun.getNight(), EVENT_CHANNEL_ID_NIGHT);
        scheduleRange(thingUID, handler, sun.getMorningNight(), EVENT_CHANNEL_ID_MORNING_NIGHT);
        scheduleRange(thingUID, handler, sun.getAstroDawn(), EVENT_CHANNEL_ID_ASTRO_DAWN);
        scheduleRange(thingUID, handler, sun.getNauticDawn(), EVENT_CHANNEL_ID_NAUTIC_DAWN);
        scheduleRange(thingUID, handler, sun.getCivilDawn(), EVENT_CHANNEL_ID_CIVIL_DAWN);
        scheduleRange(thingUID, handler, sun.getAstroDusk(), EVENT_CHANNEL_ID_ASTRO_DUSK);
        scheduleRange(thingUID, handler, sun.getNauticDusk(), EVENT_CHANNEL_ID_NAUTIC_DUSK);
        scheduleRange(thingUID, handler, sun.getCivilDusk(), EVENT_CHANNEL_ID_CIVIL_DUSK);
        scheduleRange(thingUID, handler, sun.getEveningNight(), EVENT_CHANNEL_ID_EVENING_NIGHT);
        scheduleRange(thingUID, handler, sun.getDaylight(), EVENT_CHANNEL_ID_DAYLIGHT);

        SunEclipse eclipse = sun.getEclipse();
        scheduleEvent(thingUID, handler, eclipse.getPartial(), EVENT_ECLIPSE_PARTIAL, EVENT_CHANNEL_ID_ECLIPSE, false);
        scheduleEvent(thingUID, handler, eclipse.getTotal(), EVENT_ECLIPSE_TOTAL, EVENT_CHANNEL_ID_ECLIPSE, false);
        scheduleEvent(thingUID, handler, eclipse.getRing(), EVENT_ECLIPSE_RING, EVENT_CHANNEL_ID_ECLIPSE, false);

        // schedule republish jobs
        schedulePublishPlanet(thingUID, handler, sun.getZodiac().getEnd());
        schedulePublishPlanet(thingUID, handler, sun.getSeason().getNextSeason());

        // schedule phase jobs
        scheduleSunPhase(thingUID, handler, SUN_RISE, sun.getRise().getStart());
        scheduleSunPhase(thingUID, handler, SUN_SET, sun.getSet().getStart());
        scheduleSunPhase(thingUID, handler, NIGHT, sun.getNight().getStart());
        scheduleSunPhase(thingUID, handler, DAYLIGHT, sun.getDaylight().getStart());
        scheduleSunPhase(thingUID, handler, ASTRO_DAWN, sun.getAstroDawn().getStart());
        scheduleSunPhase(thingUID, handler, NAUTIC_DAWN, sun.getNauticDawn().getStart());
        scheduleSunPhase(thingUID, handler, CIVIL_DAWN, sun.getCivilDawn().getStart());
        scheduleSunPhase(thingUID, handler, ASTRO_DUSK, sun.getAstroDusk().getStart());
        scheduleSunPhase(thingUID, handler, NAUTIC_DUSK, sun.getNauticDusk().getStart());
        scheduleSunPhase(thingUID, handler, CIVIL_DUSK, sun.getCivilDusk().getStart());
    }

    @Override
    public String toString() {
        return "Daily job sun " + getThingUID();
    }

}
