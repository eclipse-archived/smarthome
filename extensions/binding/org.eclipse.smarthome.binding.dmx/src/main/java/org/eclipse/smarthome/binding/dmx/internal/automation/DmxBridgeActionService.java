/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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

package org.eclipse.smarthome.binding.dmx.internal.automation;

import org.eclipse.smarthome.automation.annotation.ActionInput;
import org.eclipse.smarthome.automation.annotation.ActionScope;
import org.eclipse.smarthome.automation.annotation.RuleAction;
import org.eclipse.smarthome.binding.dmx.internal.DmxBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.AnnotatedActionThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DmxBridgeActionService} provides actions
 * for DMX Bridges
 *
 * @author Jan N. Klug - Initial contribution
 */

@ActionScope(name = "binding.dmx")
@Component(immediate = false, service = { AnnotatedActionThingHandlerService.class })
public class DmxBridgeActionService implements AnnotatedActionThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(DmxBridgeActionService.class);

    private DmxBridgeHandler handler;

    @RuleAction(label = "DMX Output", description = "immediately performs fade on selected DMX channels")
    void sendDmxFade(@ActionInput(name = "channels") String channels, @ActionInput(name = "fade") String fade,
            @ActionInput(name = "resumeAfter") Boolean resumeAfter) {
        logger.debug("thingHandlerAction called with inputs: {} {} {}", channels, fade, resumeAfter);

        if (handler == null) {
            logger.warn("cannot execute action on null handler");
            return;
        }

        handler.immediateFade(channels, fade, resumeAfter);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof DmxBridgeHandler) {
            this.handler = (DmxBridgeHandler) handler;
        }
    }

    @Override
    public ThingHandler getThingHandler() {
        return this.handler;
    }
}
