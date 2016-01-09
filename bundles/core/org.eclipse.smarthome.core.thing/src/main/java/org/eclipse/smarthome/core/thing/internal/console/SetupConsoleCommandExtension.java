/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.console;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.setup.ThingSetupManager;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * {@link SetupConsoleCommandExtension} provides console commands for setup of things.
 *
 * @author Alex Tugarev - Initial contribution
 */
public class SetupConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_HG_LIST = "listHomeGroups";
    private static final String SUBCMD_HG_ADD = "addHomeGroup";
    private static final String SUBCMD_HG_REMOVE = "removeHomeGroup";
    private static final String SUBCMD_HG_ITEM_ADD = "addItemToHomeGroup";
    private static final String SUBCMD_HG_ITEM_REMOVE = "removeItemFromHomeGroup";
    private static final String SUBCMD_CHANNEL_ENABLE = "enableChannel";
    private static final String SUBCMD_CHANNEL_DISABLE = "disableChannel";
    private static final String SUBCMD_LABEL_SET = "setLabel";

    private ThingSetupManager thingSetupManager;

    public SetupConsoleCommandExtension() {
        super("setup", "Setup your system.");
    }

    @Override
    public void execute(String[] args, Console console) {
        int numberOfArguments = args.length;
        if (numberOfArguments < 1) {
            return;
        }
        String subCommand = args[0];
        switch (subCommand) {
            case SUBCMD_HG_LIST:
                printHomeGroups(console);
                break;
            case SUBCMD_HG_ADD:
                if (numberOfArguments > 2) {
                    addHomeGroup(console, args[1], args[2]);
                } else {
                    console.println(
                            "Specify name and label of a home group to add: addHomeGroup <groupItemName> <label>");
                }
                break;
            case SUBCMD_HG_REMOVE:
                if (numberOfArguments > 1) {
                    removeHomeGroup(console, args[1]);
                } else {
                    console.println("Specify name of a home group to remove: removeHomeGroup <groupItemName>");
                }
                break;
            case SUBCMD_HG_ITEM_ADD:
                if (numberOfArguments > 2) {
                    addItemToHomeGroup(console, args[1], args[2]);
                } else {
                    console.println(
                            "Specify the names of item and home group: removeItemFromHomeGroup <itemName> <groupItemName>");
                }
                break;
            case SUBCMD_HG_ITEM_REMOVE:
                if (numberOfArguments > 2) {
                    removeItemFromHomeGroup(console, args[1], args[2]);
                } else {
                    console.println(
                            "Specify the names of item and home group: removeItemFromHomeGroup <itemName> <groupItemName>");
                }
                break;
            case SUBCMD_CHANNEL_ENABLE:
                if (numberOfArguments > 1) {
                    enableChannel(console, args[1]);
                } else {
                    console.println("Specify the id of channel to enable: enableChannel <channelUID>");
                }
                break;
            case SUBCMD_CHANNEL_DISABLE:
                if (numberOfArguments > 1) {
                    disableChannel(console, args[1]);
                } else {
                    console.println("Specify the id of channel to disable: enableChannel <channelUID>");
                }
                break;
            case SUBCMD_LABEL_SET:
                if (numberOfArguments > 2) {
                    setLabel(console, args[1], args[2]);
                } else {
                    console.println(
                            "Specify the new label for the item linked to the thing: setLabel <thingUID> <label>");
                }
                break;
            default:
                break;
        }
    }

    private void setLabel(Console console, String thingUID, String newLabel) {
        thingSetupManager.setLabel(new ThingUID(thingUID), newLabel);
        console.println("New label \"" + newLabel + "\" was set.");
    }

    private void disableChannel(Console console, String channelUID) {
        thingSetupManager.disableChannel(new ChannelUID(channelUID));
        console.println("The channel \"" + channelUID + "\" was disabled.");
    }

    private void enableChannel(Console console, String channelUID) {
        thingSetupManager.enableChannel(new ChannelUID(channelUID), null);
        console.println("The channel \"" + channelUID + "\" was enabled.");
    }

    private void removeItemFromHomeGroup(Console console, String itemName, String groupItemName) {
        thingSetupManager.removeFromHomeGroup(itemName, groupItemName);
        console.println("The item \"" + itemName + "\" was removed from the home group \"" + groupItemName + "\".");
    }

    private void addItemToHomeGroup(Console console, String itemName, String groupItemName) {
        thingSetupManager.addToHomeGroup(itemName, groupItemName);
        console.println("The item \"" + itemName + "\" was added to home group \"" + groupItemName + "\".");
    }

    private void removeHomeGroup(Console console, String groupItemName) {
        thingSetupManager.removeHomeGroup(groupItemName);
        console.println("The home group \"" + groupItemName + "\" was removed.");
    }

    private void addHomeGroup(Console console, String groupItemName, String label) {
        thingSetupManager.addHomeGroup(groupItemName, label);
        console.println("The home group \"" + groupItemName + "\" was created.");
    }

    private void printHomeGroups(Console console) {
        Collection<GroupItem> homeGroups = thingSetupManager.getHomeGroups();
        if (homeGroups.isEmpty()) {
            console.println("No home groups found.");
        }
        for (GroupItem groupItem : homeGroups) {
            StringBuilder sb = new StringBuilder();
            sb.append(groupItem.getName());
            sb.append(" label=");
            sb.append(groupItem.getLabel());
            Iterator<Item> members = groupItem.getAllMembers().iterator();
            if (members.hasNext()) {
                sb.append(" [");
                do {
                    sb.append(members.next().getName());
                    if (members.hasNext()) {
                        sb.append(", ");
                    }
                } while (members.hasNext());
                sb.append("]");
            }
            console.println(sb.toString());
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays
                .asList(new String[] { buildCommandUsage(SUBCMD_HG_LIST, "lists all home groups"),
                buildCommandUsage(SUBCMD_HG_ADD + " <groupItemName> <label>", "creates a new home group"),
                buildCommandUsage(SUBCMD_HG_REMOVE + " <groupItemName>", "removes a home group"),
                buildCommandUsage(SUBCMD_HG_ITEM_ADD + " <itemName> <groupItemName>", "adds an item to a home group"),
                buildCommandUsage(SUBCMD_HG_ITEM_REMOVE + " <itemName> <groupItemName>",
                        "removes an item from a home group"),
                buildCommandUsage(SUBCMD_CHANNEL_ENABLE + " <channelUID>",
                        "creates all links and linked items for a channel"),
                buildCommandUsage(SUBCMD_CHANNEL_DISABLE + " <channelUID>",
                        "removes all links and linked items of a channel"),
                buildCommandUsage(SUBCMD_LABEL_SET + " <thingUID> <label>",
                        "sets a new label of the item linked to the thing") });
    }

    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }

}
