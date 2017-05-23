/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.CHANNEL_ID_SUN_PHASE_NAME;
import static org.eclipse.smarthome.binding.astro.internal.job.Job.checkNull;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.binding.astro.internal.model.SunPhaseName;
import org.eclipse.smarthome.core.thing.Channel;

/**
 * Scheduled Job for Sun Phase Change
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public final class SunPhaseJob implements Job {

    private final String thingUID;
    private final SunPhaseName sunPhaseName;

    /**
     * Constructor
     *
     * @param thingUID
     *            thing UID
     * @param sunPhaseName
     *            {@link SunPhaseName} name
     * @throws NullPointerException
     *             if any of the arguments is {@code null}
     */
    public SunPhaseJob(String thingUID, SunPhaseName sunPhaseName) {
        checkArgument(thingUID != null, "Thing UID cannot be null");
        checkArgument(sunPhaseName != null, "Sun Phase Name cannot be null");

        this.thingUID = thingUID;
        this.sunPhaseName = sunPhaseName;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUID);
        if (checkNull(astroHandler, "AstroThingHandler is null")) {
            return;
        }
        Channel phaseNameChannel = astroHandler.getThing().getChannel(CHANNEL_ID_SUN_PHASE_NAME);
        if (checkNull(phaseNameChannel, "Phase Name Channel is null")) {
            return;
        }
        Planet planet = astroHandler.getPlanet();
        if (planet != null && planet instanceof Sun) {
            final Sun typedSun = (Sun) planet;
            typedSun.getPhase().setName(sunPhaseName);
            astroHandler.publishChannelIfLinked(phaseNameChannel.getUID());
        }
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
        SunPhaseJob other = (SunPhaseJob) obj;
        if (thingUID == null) {
            if (other.thingUID != null)
                return false;
        } else if (!thingUID.equals(other.thingUID))
            return false;
        return true;
    }

}
