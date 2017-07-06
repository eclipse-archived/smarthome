/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.thing.events.ChannelTriggeredEvent;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Trigger Channel with specific events
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class ChannelPayloadTriggerHandler extends GenericEventTriggerHandler {

    private final Logger logger = LoggerFactory.getLogger(ChannelPayloadTriggerHandler.class);

    private String eventOnChannel;

    public static final String MODULE_TYPE_ID = "core.ChannelPayloadTrigger";

    private static final String CFG_CHANNEL_EVENT = "eventOnChannel";

    public ChannelPayloadTriggerHandler(Trigger module, BundleContext bundleContext) {
        super(module, bundleContext);

        this.eventOnChannel = (String) module.getConfiguration().get(CFG_CHANNEL_EVENT);
    }

    @Override
    public boolean apply(Event event) {
        boolean channelMatches = super.apply(event);

        boolean eventMatches = true;
        if (event instanceof ChannelTriggeredEvent) {
            ChannelTriggeredEvent cte = (ChannelTriggeredEvent) event;
            logger.trace("->FILTER: {}:{}", cte.getEvent(), eventOnChannel);
            if (StringUtils.isNotBlank(eventOnChannel) && !eventOnChannel.equals(cte.getEvent())) {
                eventMatches = false;
            }
        }
        return channelMatches && eventMatches;
    }

}
