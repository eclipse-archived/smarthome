/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.console;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * Console command extension to access channels
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class TriggerChannelConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_TRIGGER = "trigger";

    private EventPublisher eventPublisher;

    public TriggerChannelConsoleCommandExtension() {
        super("channel", "Access a channel for triggering events.");
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_TRIGGER:
                    if (args.length < 3) {
                        console.println("Command '" + subCommand + "' needs arguments <channeluid> <event>");
                        printUsage(console);
                    } else {
                        sendCommand(console, args[1], args[2]);
                    }
                    break;
                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void sendCommand(Console console, String channelUid, String cmd) {
        eventPublisher.post(ThingEventFactory.createTriggerEvent(cmd, new ChannelUID(channelUid)));
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_TRIGGER + " <channeluid> <event>",
                "triggers the specified channel with the given command") });
    }

}
