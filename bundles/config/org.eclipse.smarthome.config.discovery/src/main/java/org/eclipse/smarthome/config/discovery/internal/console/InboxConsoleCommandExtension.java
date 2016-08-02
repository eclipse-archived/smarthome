/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.internal.console;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.inbox.Inbox;
import org.eclipse.smarthome.config.discovery.inbox.InboxFilterCriteria;
import org.eclipse.smarthome.config.discovery.internal.PersistentInbox;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * This class provides console commands around the inbox functionality
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class InboxConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_APPROVE = "approve";
    private static final String SUBCMD_IGNORE = "ignore";
    private static final String SUBCMD_LIST_IGNORED = "listignored";
    private static final String SUBCMD_CLEAR = "clear";

    private Inbox inbox;

    public InboxConsoleCommandExtension() {
        super("inbox", "Manage your inbox.");
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            final String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_APPROVE:
                    if (args.length > 2) {
                        String label = args[2];
                        try {
                            ThingUID thingUID = new ThingUID(args[1]);
                            List<DiscoveryResult> results = inbox.get(new InboxFilterCriteria(thingUID, null));
                            if (results.isEmpty()) {
                                console.println("No matching inbox entry could be found.");
                                return;
                            }
                            inbox.approve(thingUID, label);
                        } catch (Exception e) {
                            console.println(e.getMessage());
                        }
                    } else {
                        console.println("Specify thing id to approve: inbox approve <thingUID> <label>");
                    }
                    break;
                case SUBCMD_IGNORE:
                    if (args.length > 1) {
                        try {
                            ThingUID thingUID = new ThingUID(args[1]);
                            PersistentInbox persistentInbox = (PersistentInbox) inbox;
                            persistentInbox.setFlag(thingUID, DiscoveryResultFlag.IGNORED);
                        } catch (IllegalArgumentException e) {
                            console.println("'" + args[1] + "' is no valid thing UID.");
                        }
                    } else {
                        console.println("Cannot approve thing as managed thing provider is missing.");
                    }
                    break;
                case SUBCMD_LIST_IGNORED:
                    printInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.IGNORED)));
                    break;
                case SUBCMD_CLEAR:
                    clearInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)));
                    break;
                default:
                    break;
            }
        } else {
            printInboxEntries(console, inbox.get(new InboxFilterCriteria(DiscoveryResultFlag.NEW)));
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
            String timestamp = new Date(discoveryResult.getTimestamp()).toString();
            String timeToLive = discoveryResult.getTimeToLive() == DiscoveryResult.TTL_UNLIMITED ? "UNLIMITED"
                    : "" + discoveryResult.getTimeToLive();
            console.println(
                    String.format("%s [%s]: %s [thingId=%s, bridgeId=%s, properties=%s, timestamp=%s, timeToLive=%s]",
                            flag.name(), thingTypeUID, label, thingUID, bridgeId, properties, timestamp, timeToLive));

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
        return Arrays.asList(new String[] { buildCommandUsage("lists all current inbox entries"),
                buildCommandUsage(SUBCMD_LIST_IGNORED, "lists all ignored inbox entries"),
                buildCommandUsage(SUBCMD_APPROVE + " <thingUID> <label>", "creates a thing for an inbox entry"),
                buildCommandUsage(SUBCMD_CLEAR, "clears all current inbox entries"),
                buildCommandUsage(SUBCMD_IGNORE + " <thingUID>", "ignores an inbox entry permanently") });
    }

    protected void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    protected void unsetInbox(Inbox inbox) {
        this.inbox = null;
    }

}
