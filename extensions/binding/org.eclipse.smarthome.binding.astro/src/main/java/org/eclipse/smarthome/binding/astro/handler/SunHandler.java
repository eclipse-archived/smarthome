/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.handler;

import static org.eclipse.smarthome.binding.astro.AstroBindingConstants.THING_TYPE_SUN;

import java.util.Calendar;
import java.util.Set;

import org.eclipse.smarthome.binding.astro.internal.calc.SunCalc;
import org.eclipse.smarthome.binding.astro.internal.job.AbstractDailyJob;
import org.eclipse.smarthome.binding.astro.internal.job.DailyJobSun;
import org.eclipse.smarthome.binding.astro.internal.model.Planet;
import org.eclipse.smarthome.binding.astro.internal.model.Sun;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The SunHandler is responsible for updating calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunHandler extends AstroThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_SUN);

    private String[] positionalChannelIds = new String[] { "position#azimuth", "position#elevation", "radiation#direct",
            "radiation#diffuse", "radiation#total" };
    private SunCalc sunCalc = new SunCalc();
    private Sun sun;

    public SunHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDailyInfo() {
        sun = sunCalc.getSunInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude(),
                thingConfig.getAltitude());
        publishPositionalInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPositionalInfo() {
        sunCalc.setPositionalInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude(),
                thingConfig.getAltitude(), sun);
        publishPlanet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Planet getPlanet() {
        return sun;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        sun = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends AbstractDailyJob> getDailyJobClass() {
        return DailyJobSun.class;
    }

}
