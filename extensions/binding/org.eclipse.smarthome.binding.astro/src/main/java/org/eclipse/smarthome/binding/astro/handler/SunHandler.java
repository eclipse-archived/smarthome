/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.THING_TYPE_SUN;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.binding.astro.internal.calc.SunCalc;
import org.eclipse.smarthome.binding.astro.internal.job.DailyJobSun;
import org.eclipse.smarthome.binding.astro.internal.job.Job;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The SunHandler is responsible for updating calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
public class SunHandler extends AstroThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_SUN));

    private final String[] positionalChannelIds = new String[] { "position#azimuth", "position#elevation",
            "radiation#direct", "radiation#diffuse", "radiation#total" };
    private final SunCalc sunCalc = new SunCalc();
    private Sun sun;

    /** Constructor */
    public SunHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void publishDailyInfo() {
        initializeSun();
        publishPositionalInfo();
    }

    @Override
    public void publishPositionalInfo() {
        initializeSun();
        sunCalc.setPositionalInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude(),
                thingConfig.getAltitude(), sun);
        publishPlanet();
    }

    @Override
    public Planet getPlanet() {
        return sun;
    }

    @Override
    public void dispose() {
        super.dispose();
        sun = null;
    }

    @Override
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

    @Override
    protected Job getDailyJob() {
        return new DailyJobSun(thing.getUID().getAsString(), this);
    }
    
    private void initializeSun() {
        sun = sunCalc.getSunInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude(),
                thingConfig.getAltitude());
    }

}
