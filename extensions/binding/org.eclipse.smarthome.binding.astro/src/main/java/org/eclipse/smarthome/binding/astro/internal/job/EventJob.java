/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.astro.internal.job;

import java.util.Map;

import org.eclipse.smarthome.binding.astro.handler.AstroThingHandler;
import org.eclipse.smarthome.binding.astro.internal.AstroHandlerFactory;

/**
 * Job to trigger a event.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Removed Quartz dependency
 */
public class EventJob extends AbstractBaseJob {
    public static final String KEY_EVENT = "event";

    public EventJob(Map<String, Object> jobDataMap) {
        super(jobDataMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(String thingUid) {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        if (astroHandler != null) {
            String event = (String) jobDataMap.get(KEY_EVENT);
            String channelId = (String) jobDataMap.get(KEY_CHANNEL_ID);
            astroHandler.triggerEvent(channelId, event);
        }
    }

}
