/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static org.eclipse.smarthome.binding.astro.internal.job.Job.checkNull;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;

/**
 * Scheduled job to trigger events
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class EventJob extends AbstractJob {

    private final String channelID;
    private final String event;

    /**
     * Constructor
     *
     * @param thingUID thing UID
     * @param channelID channel ID
     * @param event Event name
     * @throws IllegalArgumentException
     *             if any of the arguments is {@code null}
     */
    public EventJob(String thingUID, String channelID, String event) {
        checkArgument(thingUID != null, "Thing UID cannot be null");
        checkArgument(channelID != null, "Channel ID cannot be null");
        checkArgument(event != null, "Event cannot be null");

        this.thingUID = thingUID;
        this.channelID = channelID;
        this.event = event;
    }

    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUID);
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        astroHandler.triggerEvent(channelID, event);
    }

}
