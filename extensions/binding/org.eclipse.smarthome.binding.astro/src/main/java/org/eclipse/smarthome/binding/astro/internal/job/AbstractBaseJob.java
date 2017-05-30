/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Baseclass for all jobs with common methods.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Removed Quartz dependency
 */
public abstract class AbstractBaseJob implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(AbstractBaseJob.class);
    public static final SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final String KEY_THING_UID = "thingUid";
    public static final String KEY_CHANNEL_ID = "channelId";
    public static final String KEY_JOB_NAME = "jobName";
    public static final String KEY_PHASE_NAME = "phaseName";

    protected Map<String, Object> jobDataMap;

    public AbstractBaseJob(Map<String, Object> jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    @Override
    public void run() {
        String thingUid = (String) jobDataMap.get(KEY_THING_UID);
        String jobName = (String) jobDataMap.get(KEY_JOB_NAME);
        if (logger.isDebugEnabled()) {
            logger.debug("Starting astro {} for thing {}", jobName, thingUid);
        }

        executeJob(thingUid);
    }

    /**
     * Method to override by the different jobs to be executed.
     */
    protected abstract void executeJob(String thingUid);

}
