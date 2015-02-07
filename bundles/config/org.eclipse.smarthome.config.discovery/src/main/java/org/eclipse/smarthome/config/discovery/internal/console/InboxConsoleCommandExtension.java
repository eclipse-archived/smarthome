/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal.console;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.config.discovery.internal.PersistentInbox;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

import com.google.common.collect.Lists;

public class InboxConsoleCommandExtension implements ConsoleCommandExtension {

    private final static String COMMAND_INBOX = "inbox";

    private final static List<String> SUPPORTED_COMMANDS = Lists.newArrayList(COMMAND_INBOX);

    private Inbox inbox;
    private ManagedThingProvider managedThingProvider;
    private ThingSetupManager thingSetupManager;

    @Override
    public boolean canHandle(String[] args) {
        String firstArgument = args[0];
        return SUPPORTED_COMMANDS.contains(firstArgument);
    }

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];
        switch (command) {
            case COMMAND_INBOX:
                if (args.length > 1) {
                    String subCommand = args[1];
                    switch (subCommand) {
                        case "approve":
                            if (args.length > 2) {
                                boolean fullSetup = false;
                                if (args.length > 3) {
                                    fullSetup = Boolean.parseBoolean(args[3]);
                                }
                                if (managedThingProvider != null && thingSetupManager != null) {
                                    try {
                                        ThingUID thingUID = new ThingUID(args[2]);
                                        List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUID,
                                                null));
                                        if (results.isEmpty()) {
                                            console.println("No matching inbox entry could be found.");
                                            return;
                                        }
                                        DiscoveryResult result = results.get(0);
                                        Configuration configuratiob = new Configuration(result.getProperties());
                                        if (fullSetup) {
                                            thingSetupManager.addThing(thingUID, configuratiob, result.getBridgeUID());
                                        } else {
                                            managedThingProvider.createThing(result.getThingTypeUID(),
                                                    result.getThingUID(), result.getBridgeUID(), configuratiob);
                                        }
                                    } catch (Exception e) {
                                        console.println(e.getMessage());
                                    }
                                } else {
                                    console.println("Cannot approve thing as managed thing provider or setup manager is missing.");
                                }
                            } else {
                                console.println("Specify thing id to approve: inbox approve <thingUID> <fullSetup - true|false>");
                            }
                            return;
                        case "ignore":
                            if (args.length > 2) {
                                try {
                                    ThingUID thingUID = new ThingUID(args[2]);
                                    PersistentInbox persistentInbox = (PersistentInbox) inbox;
                                    persistentInbox.setFlag(thingUID, DiscoveryResultFlag.IGNORED);
                                } catch (IllegalArgumentException e) {
                                    console.println("'" + args[2] + "' is no valid thing UID.");
                                }
                            } else {
                                console.println("Cannot approve thing as managed thing provider is missing.");
                            }
                            return;
                        case "listignored":
                            printInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.IGNORED)));
                            return;
                        case "clear":
                            clearInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)));
                            return;
                        default:
                            break;
                    }
                }
                printInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)));
                return;
            default:
                return;
        }
    }

    private void printInboxEntries(Console console, List<DiscoveryResult> discoveryResults) {

        if (discoveryResults.isEmpty()) {
            console.println("No inbox entries found.");
        }

        for (DiscoveryResult discoveryResult : discoveryResults) {
            ThingTypeUID thingTypeUID = discoveryResult.getThingTypeUID();
            ThingUID thingUID = discoveryResult.getThingUID();
            String label = discoveryResult.getLabel();
            DiscoveryResultFlag flag = discoveryResult.getFlag();
            ThingUID bridgeId = discoveryResult.getBridgeUID();
            Map<String, Object> properties = discoveryResult.getProperties();
            console.println(String.format("%s [%s]: %s [thingId=%s, bridgeId=%s, properties=%s]", flag.name(),
                    thingTypeUID, label, thingUID, bridgeId, properties));

        }
    }

    private void clearInboxEntries(Console console, List<DiscoveryResult> discoveryResults) {

        if (discoveryResults.isEmpty()) {
            console.println("No inbox entries found.");
        }

        for (DiscoveryResult discoveryResult : discoveryResults) {
            ThingTypeUID thingTypeUID = discoveryResult.getThingTypeUID();
            ThingUID thingUID = discoveryResult.getThingUID();
            String label = discoveryResult.getLabel();
            DiscoveryResultFlag flag = discoveryResult.getFlag();
            ThingUID bridgeId = discoveryResult.getBridgeUID();
            Map<String, Object> properties = discoveryResult.getProperties();
            console.println(String.format("REMOVED [%s]: %s [thingId=%s, bridgeId=%s, properties=%s]", flag.name(),
                    thingTypeUID, label, thingUID, bridgeId, properties));
            inbox.remove(thingUID);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList((new String[] { COMMAND_INBOX + " - lists all current inbox entries",
                COMMAND_INBOX + " listignored - lists all ignored inbox entries",
                COMMAND_INBOX + " approve <thingUID> <fullSetup - true|false> - creates a thing for an inbox entry",
                COMMAND_INBOX + " clear - clears all current inbox entries",
                COMMAND_INBOX + " ignore <thingUID> - ignores an inbox entry permanently" }));
    }

    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    protected void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }
}
