/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.time.DateFormatUtils.ISO_DATETIME_FORMAT;
import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.*;
import static org.eclipse.smarthome.binding.astro.internal.util.DateTimeUtils.*;
import static org.eclipse.smarthome.core.scheduler.CronHelper.createCronFromCalendar;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.config.AstroChannelConfig;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Range;
import org.eclipse.smarthome.binding.astro.internal.model.SunPhaseName;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.Expression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface to be implemented by classes which represent a 'job' to be performed
 *
 * @author Amit Kumar Mondal - New Simplified API, Implementation compliant with ESH Scheduler
 */
public interface Job extends Runnable {

    /** Logger Instance */
    public final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Schedules the provided {@link Job} instance
     *
     * @param thingUID the UID of the {@link Thing} instance
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param job the {@link Job} instance to schedule
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void schedule(String thingUID, AstroThingHandler astroHandler, Job job, Calendar eventAt) {
        try {
            Calendar today = Calendar.getInstance();
            if (isSameDay(eventAt, today) && isTimeGreaterEquals(eventAt, today)) {
                ExpressionThreadPoolExecutor executor = astroHandler.getScheduler();
                if (executor != null) {
                    Expression cron = new CronExpression(createCronFromCalendar(eventAt));
                    if (astroHandler.addJobToQueue(job)) {
                        executor.schedule(job, cron);
                        String formattedDate = ISO_DATETIME_FORMAT.format(eventAt);
                        logger.debug("Scheduled astro job for thing {} at {}", thingUID, formattedDate);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param thingUID the Thing UID
     * @param astroHandler the {@link ThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param event the event ID
     * @param channelId the channel ID
     */
    public static void scheduleEvent(String thingUID, AstroThingHandler astroHandler, Calendar eventAt, String event,
            String channelId) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");
        boolean eventNull = checkNull(event, "Event is null");
        boolean channelIdNull = checkNull(channelId, "Channel ID is null");

        if (thingNull || astroHandlerNull || eventAtNull || eventNull || channelIdNull) {
            return;
        }
        AstroChannelConfig config = astroHandler.getThing().getChannel(channelId).getConfiguration()
                .as(AstroChannelConfig.class);
        Calendar instant = applyConfig(eventAt, config);
        Job eventJob = new EventJob(thingUID, channelId, event);
        schedule(thingUID, astroHandler, eventJob, instant);
    }

    /**
     * Schedules {@link Channel} events
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param range the {@link Range} instance
     * @param channelId the channel ID
     */
    public static void scheduleRange(String thingUID, AstroThingHandler astroHandler, Range range, String channelId) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean rangeNull = checkNull(range, "Range is null");
        boolean channelIdNull = checkNull(channelId, "Channel ID is null");

        if (thingNull || astroHandlerNull || rangeNull || channelIdNull) {
            return;
        }
        scheduleEvent(thingUID, astroHandler, range.getStart(), EVENT_START, channelId);
        scheduleEvent(thingUID, astroHandler, range.getEnd(), EVENT_END, channelId);
    }

    /**
     * Schedules {@link Planet} events
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void schedulePublishPlanet(String thingUID, AstroThingHandler astroHandler, Calendar eventAt) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");

        if (thingNull || astroHandlerNull || eventAtNull) {
            return;
        }
        Job publishJob = new PublishPlanetJob(thingUID);
        schedule(thingUID, astroHandler, publishJob, eventAt);
    }

    /**
     * Schedules {@link SunPhaseJob}
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param sunPhaseName {@link SunPhaseName} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void scheduleSunPhase(String thingUID, AstroThingHandler astroHandler, SunPhaseName sunPhaseName,
            Calendar eventAt) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean sunPhaseNull = checkNull(sunPhaseName, "Sun Phase Name is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");

        if (thingNull || astroHandlerNull || sunPhaseNull || eventAtNull) {
            return;
        }
        Job sunPhaseJob = new SunPhaseJob(thingUID, sunPhaseName);
        schedule(thingUID, astroHandler, sunPhaseJob, eventAt);
    }

    /**
     * Checks that the specified object reference is not {@code null} and logs a
     * customized message if it is. This method is designed primarily for doing
     * parameter validation in methods and constructors with multiple
     * parameters, as demonstrated below: <blockquote>
     *
     * <pre>
     * public Foo(Bar bar, Baz baz) {
     *     checkNull(bar, "bar is null");
     *     checkNull(baz, "baz is null");
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param obj the object reference to check for {@code null}
     * @param message detail message to be used in the event
     * @return {@code true} if {@code null} otherwise {@code false}
     */
    public static <T> boolean checkNull(T obj, String message) {
        if (isNull(obj)) {
            logger.trace(message);
            return true;
        }
        return false;
    }

    /**
     * Returns the thing UID that is associated with this {@link Job} (cannot be {@code null})
     */
    public String getThingUID();

}
