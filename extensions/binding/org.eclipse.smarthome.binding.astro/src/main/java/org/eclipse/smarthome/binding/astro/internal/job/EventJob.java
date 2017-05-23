/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.smarthome.binding.astro.internal.job.Job.checkNull;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;

/**
 * Scheduled Job to trigger Events
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class EventJob implements Job {

    private final String thingUID;
    private final String channelID;
    private final String event;

    /**
     * Constructor
     *
     * @param thingUID
     *            thing UID
     * @param channelID
     *            channel ID
     * @param event
     *            Event name
     * @throws NullPointerException
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

    /** {@inheritDoc} */
    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUID);
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        astroHandler.triggerEvent(channelID, event);
    }

    /** {@inheritDoc} */
    @Override
    public String getThingUID() {
        return thingUID;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((thingUID == null) ? 0 : thingUID.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventJob other = (EventJob) obj;
        if (thingUID == null) {
            if (other.thingUID != null)
                return false;
        } else if (!thingUID.equals(other.thingUID))
            return false;
        return true;
    }
    
    

}
