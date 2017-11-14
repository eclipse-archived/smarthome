/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule.runtime.internal.engine;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * The {@link GuiceAwareJobFactory} instantiates {@link Job}s using the Guice injector. This said it's possible to use
 * Guice injection within the Quartz jobs.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class GuiceAwareJobFactory implements JobFactory {

    @Inject
    private Injector injector;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

        return injector.getInstance(bundle.getJobDetail().getJobClass());
    }
}