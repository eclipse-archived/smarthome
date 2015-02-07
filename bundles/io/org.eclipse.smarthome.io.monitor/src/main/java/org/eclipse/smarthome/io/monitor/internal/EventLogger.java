/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.monitor.internal;

import org.eclipse.smarthome.core.events.AbstractEventSubscriber;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger extends AbstractEventSubscriber {

    private final Logger logger = LoggerFactory.getLogger("runtime.busevents");

    @Override
    public void receiveCommand(String itemName, Command command) {
        logger.info("{} received command {}", itemName, command);
    }

    @Override
    public void receiveUpdate(String itemName, State newStatus) {
        logger.info("{} state updated to {}", itemName, newStatus);
    }

}
