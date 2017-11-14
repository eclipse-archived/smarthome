/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import java.util.List;

/**
 * {@link CompositeJob} comprises multiple {@link Job}s to be executed in order
 *
 * @author Markus Rathgeb - Initial contribution
 * @author Amit Kumar Mondal - Minor modifications
 */
public final class CompositeJob extends AbstractJob {

    private final List<Job> jobs;

    /**
     * Constructor
     * 
     * @param thingUID thing UID
     * @param jobs the jobs to execute
     * @throws IllegalArgumentException
     *             if {@code jobs} is {@code null} or empty
     */
    public CompositeJob(String thingUID, List<Job> jobs) {
        super(thingUID);
        checkArgument(jobs != null, "Jobs must not be null");
        checkArgument(!jobs.isEmpty(), "Jobs must not be empty");

        this.jobs = jobs;

        boolean notMatched = jobs.stream().anyMatch(j -> !j.getThingUID().equals(thingUID));
        checkArgument(!notMatched, "The jobs must associate the same thing UID");
    }

    @Override
    public void run() {
        jobs.forEach(j -> {
            try {
                j.run();
            } catch (RuntimeException ex) {
                logger.warn("Job execution failed.", ex);
            }
        });
    }
}