/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static org.eclipse.smarthome.binding.astro.internal.job.Job.checkNull;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;

/**
 * Scheduled job for planets
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class PublishPlanetJob extends AbstractJob {

    /**
     * Constructor
     *
     * @param thingUID thing UID
     * @throws IllegalArgumentException
     *             if the provided argument is {@code null}
     */
    public PublishPlanetJob(String thingUID) {
        super(thingUID);
    }

    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(getThingUID());
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        astroHandler.publishDailyInfo();
    }

    @Override
    public String toString() {
        return "Publish planet job " + getThingUID();
    }

}
