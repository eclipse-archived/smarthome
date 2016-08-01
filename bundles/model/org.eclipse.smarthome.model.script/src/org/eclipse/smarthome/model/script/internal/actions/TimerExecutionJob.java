/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.actions;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Quartz {@link Job} which executes the code of a closure that is passed
 * to the createTimer() extension method.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class TimerExecutionJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(TimerExecutionJob.class);

    /**
     * Runs the configured closure of this job
     * 
     * @param context the execution context of the job
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("Executing timer '{}'", context.getJobDetail().getKey().toString());
        Procedure0 procedure = (Procedure0) context.getJobDetail().getJobDataMap().get("procedure");
        TimerImpl timer = (TimerImpl) context.getJobDetail().getJobDataMap().get("timer");
        procedure.apply();
        timer.setTerminated(true);
    }

}
