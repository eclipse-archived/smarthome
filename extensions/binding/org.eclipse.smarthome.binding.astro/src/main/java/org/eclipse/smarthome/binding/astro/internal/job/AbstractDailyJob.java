/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;
import org.eclipse.smarthome.binding.astro.internal.config.AstroChannelConfig;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Range;
import org.eclipse.smarthome.binding.astro.internal.model.SunPhaseName;
import org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils;
import org.eclipse.smarthome.core.scheduler.DateExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates and publishes the planet data and also schedules the events for the current day.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Removed Quartz dependency
 */
public abstract class AbstractDailyJob extends AbstractBaseJob {
    private final Logger logger = LoggerFactory.getLogger(AbstractDailyJob.class);

    public AbstractDailyJob(Map<String, Object> jobDataMap) {
        super(jobDataMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(String thingUid) {
        AstroThingHandler handler = AstroHandlerFactory.getHandler(thingUid);
        if (handler != null) {
            handler.publishDailyInfo();
            schedulePlanetEvents(thingUid, handler, handler.getPlanet());
            logger.info("Scheduled astro event-jobs for thing {}", thingUid);
        }
    }

    /**
     * Schedules the events for the planet for the current day.
     */
    protected abstract void schedulePlanetEvents(String thingUid, AstroThingHandler handler, Planet planet);

    protected void scheduleRange(String thingUid, AstroThingHandler astroHandler, Range range, String channelId) {
        scheduleEvent(thingUid, astroHandler, range.getStart(), EVENT_START, channelId);
        scheduleEvent(thingUid, astroHandler, range.getEnd(), EVENT_END, channelId);
    }

    protected void scheduleEvent(String thingUid, AstroThingHandler astroHandler, Calendar eventAt, String event,
            String channelId) {
        if (eventAt == null) {
            logger.trace("Timestamp for job for event {} of channel {} was NULL", event, channelId);
            return;
        }
        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put(KEY_THING_UID, thingUid);
        jobDataMap.put(EventJob.KEY_EVENT, event);
        jobDataMap.put(KEY_CHANNEL_ID, channelId);

        AstroChannelConfig config = astroHandler.getThing().getChannel(channelId).getConfiguration()
                .as(AstroChannelConfig.class);
        eventAt = DateTimeUtils.applyConfig(eventAt, config);
        schedule(astroHandler, EventJob.class, jobDataMap, "event-" + event.toLowerCase() + "-" + channelId, eventAt);
    }

    protected void schedulePublishPlanet(String thingUid, AstroThingHandler astroHandler, String jobKey,
            Calendar eventAt) {
        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put(KEY_THING_UID, thingUid);

        schedule(astroHandler, PublishPlanetJob.class, jobDataMap, "publish-" + jobKey, eventAt);
    }

    protected void scheduleSunPhase(String thingUid, AstroThingHandler astroHandler, SunPhaseName phaseName,
            Calendar eventAt) {
        Map<String, Object> jobDataMap = new HashMap<>();
        jobDataMap.put(KEY_THING_UID, thingUid);
        jobDataMap.put(KEY_PHASE_NAME, phaseName);

        schedule(astroHandler, SunPhaseJob.class, jobDataMap, "sunPhase-" + phaseName.toString().toLowerCase(),
                eventAt);
    }

    private void schedule(AstroThingHandler astroHandler, Class<? extends AbstractBaseJob> clazz,
            Map<String, Object> jobDataMap, String jobKey, Calendar eventAt) {
        try {
            Calendar today = Calendar.getInstance();
            if (eventAt != null && DateTimeUtils.isSameDay(eventAt, today)
                    && DateTimeUtils.isTimeGreaterEquals(eventAt, today)) {
                jobDataMap.put(KEY_JOB_NAME, "job-" + jobKey);
                String thingUid = (String) jobDataMap.get(KEY_THING_UID);
                DateExpression dateExpression = new DateExpression(
                        AbstractBaseJob.ISO8601_FORMAT.format(eventAt.getTime()));
                astroHandler.getScheduler().schedule(clazz.getDeclaredConstructor(Map.class).newInstance(jobDataMap),
                        dateExpression);
                logger.debug("Scheduled astro job-{} for thing {} at {}", jobKey, thingUid,
                        DateFormatUtils.ISO_DATETIME_FORMAT.format(eventAt));
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }
}
