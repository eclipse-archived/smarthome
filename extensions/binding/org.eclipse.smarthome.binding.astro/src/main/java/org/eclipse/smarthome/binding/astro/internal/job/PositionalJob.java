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
 * Scheduled Job for Planet Positions
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class PositionalJob implements Job {

    private final String thingUID;

    /**
     * Constructor
     *
     * @param thingUID
     *            thing UID
     * @throws NullPointerException
     *             if the provided argument is {@code null}
     */
    public PositionalJob(String thingUID) {
        checkArgument(thingUID != null, "Thing UID cannot be null");
        this.thingUID = thingUID;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUID);
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        astroHandler.publishPositionalInfo();
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
        PositionalJob other = (PositionalJob) obj;
        if (thingUID == null) {
            if (other.thingUID != null)
                return false;
        } else if (!thingUID.equals(other.thingUID))
            return false;
        return true;
    }
    
    

}
