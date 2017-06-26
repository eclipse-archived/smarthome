/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler;

import static java.util.stream.Collectors.toList;
import static org.eclipse.smarthome.core.scheduler.CronHelper.*;
import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.eclipse.smarthome.core.thing.type.ChannelKind.TRIGGER;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.astro.internal.config.AstroChannelConfig;
import org.eclipse.smarthome.binding.astro.internal.config.AstroThingConfig;
import org.eclipse.smarthome.binding.astro.internal.job.Job;
import org.eclipse.smarthome.binding.astro.internal.job.PositionalJob;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.util.PropertyUtils;
import org.eclipse.smarthome.core.scheduler.CronExpression;
import org.eclipse.smarthome.core.scheduler.Expression;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager.ExpressionThreadPoolExecutor;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base ThingHandler for all Astro handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public abstract class AstroThingHandler extends BaseThingHandler {

    /** Logger Instance */
    protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Maximum number of scheduled jobs */
    private static final int MAX_SCHEDULED_JOBS = 200;

    /** Scheduler to schedule jobs */
    private final ExpressionThreadPoolExecutor scheduledExecutor;

    private int linkedPositionalChannels = 0;
    protected AstroThingConfig thingConfig;
    private final Lock monitor = new ReentrantLock();
    private final Queue<Job> scheduledJobs;

    public AstroThingHandler(Thing thing) {
        super(thing);
        scheduledExecutor = ExpressionThreadPoolManager.getExpressionScheduledPool("astro");
        scheduledJobs = new LinkedBlockingQueue<>(MAX_SCHEDULED_JOBS);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(AstroThingConfig.class);
        thingConfig.setThingUid(thingUid);
        boolean validConfig = true;

        if (StringUtils.trimToNull(thingConfig.getGeolocation()) == null) {
            logger.error("Astro parameter geolocation is mandatory and must be configured, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        } else {
            thingConfig.parseGeoLocation();
        }

        if (thingConfig.getLatitude() == null || thingConfig.getLongitude() == null) {
            logger.error(
                    "Astro parameters geolocation could not be split into latitude and longitude, disabling thing '{}'",
                    thingUid);
            validConfig = false;
        }
        if (thingConfig.getInterval() == null || thingConfig.getInterval() < 1 || thingConfig.getInterval() > 86400) {
            logger.error("Astro parameter interval must be in the range of 1-86400, disabling thing '{}'", thingUid);
            validConfig = false;
        }

        if (validConfig) {
            logger.debug("{}", thingConfig);
            updateStatus(ONLINE);
            restartJobs();
        } else {
            updateStatus(OFFLINE);
        }
        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        stopJobs();
        logger.debug("Thing {} disposed", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH == command) {
            logger.debug("Refreshing {}", channelUID);
            publishChannelIfLinked(channelUID);
        } else {
            logger.warn("The Astro-Binding is a read-only binding and can not handle commands");
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        logger.warn("The Astro-Binding is a read-only binding and can not handle channel updates");
        super.handleUpdate(channelUID, newState);
    }

    /**
     * Iterates all channels of the thing and updates their states.
     */
    public void publishPlanet() {
        logger.debug("Publishing planet {} for thing {}", getPlanet().getClass().getSimpleName(), getThing().getUID());
        for (Channel channel : getThing().getChannels()) {
            if (channel.getKind() != TRIGGER) {
                publishChannelIfLinked(channel.getUID());
            }
        }
    }

    /**
     * Publishes the channel with data if it's linked.
     */
    public void publishChannelIfLinked(ChannelUID channelUID) {
        if (isLinked(channelUID.getId()) && getPlanet() != null) {
            try {
                AstroChannelConfig config = getThing().getChannel(channelUID.getId()).getConfiguration()
                        .as(AstroChannelConfig.class);
                updateState(channelUID, PropertyUtils.getState(channelUID, config, getPlanet()));
            } catch (Exception ex) {
                logger.error("Can't update state for channel {} : {}", channelUID, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Schedules a positional and a daily job at midnight for Astro calculation and starts it immediately too. Removes
     * already scheduled jobs first.
     */
    private void restartJobs() {
        logger.debug("Restarting jobs for thing {}", getThing().getUID());
        monitor.lock();
        try {
            stopJobs();
            if (getThing().getStatus() == ONLINE) {
                String thingUID = getThing().getUID().toString();
                String typeId = getThing().getThingTypeUID().getId();
                if (scheduledExecutor == null) {
                    logger.warn("Thread Pool Executor is not available");
                    return;
                }
                // Daily Job
                Job dailyJob = getDailyJob();
                if (dailyJob == null) {
                    logger.error("Daily job instance is not available");
                    return;
                }
                Expression midNightExpression = new CronExpression(DAILY_MIDNIGHT);
                if (addJobToQueue(dailyJob)) {
                    scheduledExecutor.schedule(dailyJob, midNightExpression);
                    logger.info("Scheduled astro job-daily-{} at midnight for thing {}", typeId, thingUID);
                }
                // Execute daily startup job immediately
                dailyJob.run();

                // Repeat scheduled job every configured seconds
                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime futureTimeWithInterval = currentTime.plusSeconds(thingConfig.getInterval());
                Date start = Date.from(futureTimeWithInterval.atZone(ZoneId.systemDefault()).toInstant());
                if (isPositionalChannelLinked()) {
                    Expression expression = new CronExpression(
                            createCronForRepeatEverySeconds(thingConfig.getInterval()), start);
                    Job positionalJob = new PositionalJob(thingUID);
                    if (addJobToQueue(positionalJob)) {
                        scheduledExecutor.schedule(positionalJob, expression);
                        logger.info("Scheduled astro job-positional with interval of {} seconds for thing {}",
                                thingConfig.getInterval(), thingUID);
                    }
                    // Execute positional startup job immediately
                    positionalJob.run();
                }
            }
        } catch (ParseException ex) {
            logger.error("{}", ex.getMessage(), ex);
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Stops all jobs for this thing.
     */
    private void stopJobs() {
        ThingUID thingUID = getThing().getUID();
        monitor.lock();
        try {
            if (scheduledExecutor != null) {
                List<Job> jobsToRemove = scheduledJobs.stream().filter(this::isJobAssociatedWithThing)
                        .collect(toList());
                if (jobsToRemove.isEmpty()) {
                    return;
                }
                logger.debug("Stopping Scheduled Jobs for thing {}", thingUID);
                Consumer<Job> removalFromExecutor = scheduledExecutor::remove;
                Consumer<Job> removalFromQueue = scheduledJobs::remove;
                jobsToRemove.forEach(removalFromExecutor.andThen(removalFromQueue));
                logger.debug("Stopped {} Scheduled Jobs for thing {}", jobsToRemove.size(), thingUID);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        } finally {
            monitor.unlock();
        }
    }

    private boolean isJobAssociatedWithThing(Job job) {
        String thingUID = getThing().getUID().getAsString();
        String jobThingUID = job.getThingUID();
        return Objects.equals(thingUID, jobThingUID);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, 1);
        publishChannelIfLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannelChange(channelUID, -1);
    }

    /**
     * Counts positional channels and restarts Astro jobs.
     */
    private void linkedChannelChange(ChannelUID channelUID, int step) {
        if (ArrayUtils.contains(getPositionalChannelIds(), channelUID.getId())) {
            int oldValue = linkedPositionalChannels;
            linkedPositionalChannels += step;
            if (oldValue == 0 && linkedPositionalChannels > 0 || oldValue > 0 && linkedPositionalChannels == 0) {
                restartJobs();
            }
        }
    }

    /**
     * Returns {@code true}, if at least one positional channel is linked.
     */
    private boolean isPositionalChannelLinked() {
        for (Channel channel : getThing().getChannels()) {
            if (ArrayUtils.contains(getPositionalChannelIds(), channel.getUID().getId())
                    && isLinked(channel.getUID().getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Emits an event for the given channel.
     */
    public void triggerEvent(String channelId, String event) {
        if (getThing().getChannel(channelId) == null) {
            logger.warn("Event {} in thing {} does not exist, please recreate the thing", event, getThing().getUID());
            return;
        }
        triggerChannel(getThing().getChannel(channelId).getUID(), event);
    }

    /**
     * Returns the scheduler for the Astro jobs
     */
    public ExpressionThreadPoolExecutor getScheduler() {
        return scheduledExecutor;
    }

    /**
     * Adds the provided {@link Job} to the queue (cannot be {@code null})
     *
     * @return {@code true} if the {@code job} is added to the queue, otherwise {@code false}
     */
    public boolean addJobToQueue(Job job) {
        if (job != null) {
            return scheduledJobs.add(job);
        }
        return false;
    }

    /**
     * Calculates and publishes the daily Astro data.
     */
    public abstract void publishDailyInfo();

    /**
     * Calculates and publishes the interval Astro data.
     */
    public abstract void publishPositionalInfo();

    /**
     * Returns the {@link Planet} instance (cannot be {@code null})
     */
    public abstract Planet getPlanet();

    /**
     * Returns the channelIds for positional calculation (cannot be {@code null})
     */
    protected abstract String[] getPositionalChannelIds();

    /**
     * Returns the daily calculation {@link Job} (cannot be {@code null})
     */
    protected abstract Job getDailyJob();
}
