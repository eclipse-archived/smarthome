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
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * {@link SetupConsoleCommandExtension} provides console commands for setup of things.
 *
 * @author Alex Tugarev - Initial contribution
 */
public class SetupConsoleCommandExtension implements ConsoleCommandExtension {

    private final static String COMMAND_SETUP = "setup";

    private ThingSetupManager thingSetupManager;

    @Override
    public boolean canHandle(String[] args) {
        String firstArgument = args[0];
        return COMMAND_SETUP.equals(firstArgument);
    }

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];
        switch (command) {
            case COMMAND_SETUP:
                int numberOfArguments = args.length;
                if (numberOfArguments < 2) {
                    break;
                }
                String subCommand = args[1];
                switch (subCommand) {
                    case "listHomeGroups":
                        printHomeGroups(console);
                        break;
                    case "addHomeGroup":
                        if (numberOfArguments > 3) {
                            addHomeGroup(console, args[2], args[3]);
                        } else {
                            console.println("Specify name and label of a home group to add: addHomeGroup <groupItemName> <label>");
                        }
                        break;
                    case "removeHomeGroup":
                        if (numberOfArguments > 2) {
                            removeHomeGroup(console, args[2]);
                        } else {
                            console.println("Specify name of a home group to remove: removeHomeGroup <groupItemName>");
                        }
                        break;
                    case "addItemToHomeGroup":
                        if (numberOfArguments > 3) {
                            addItemToHomeGroup(console, args[2], args[3]);
                        } else {
                            console.println("Specify the names of item and home group: removeItemFromHomeGroup <itemName> <groupItemName>");
                        }
                        break;
                    case "removeItemFromHomeGroup":
                        if (numberOfArguments > 3) {
                            removeItemFromHomeGroup(console, args[2], args[3]);
                        } else {
                            console.println("Specify the names of item and home group: removeItemFromHomeGroup <itemName> <groupItemName>");
                        }
                        break;
                    case "enableChannel":
                        if (numberOfArguments > 2) {
                            enableChannel(console, args[2]);
                        } else {
                            console.println("Specify the id of channel to enable: enableChannel <channelUID>");
                        }
                        break;
                    case "disableChannel":
                        if (numberOfArguments > 2) {
                            disableChannel(console, args[2]);
                        } else {
                            console.println("Specify the id of channel to disable: enableChannel <channelUID>");
                        }
                        break;
                    case "setLabel":
                        if (numberOfArguments > 3) {
                            setLabel(console, args[2], args[3]);
                        } else {
                            console.println("Specify the new label for the item linked to the thing: setLabel <thingUID> <label>");
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                return;
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
        thingSetupManager.enableChannel(new ChannelUID(channelUID));
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
                    if (members.hasNext())
                        sb.append(", ");
                } while (members.hasNext());
                sb.append("]");
            }
            console.println(sb.toString());
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList((new String[] {
                COMMAND_SETUP + " listHomeGroups - lists all home groups",
                COMMAND_SETUP + " addHomeGroup <groupItemName> <label> - creates a new home group",
                COMMAND_SETUP + " removeHomeGroup <groupItemName> - creates a new home group",
                COMMAND_SETUP + " addItemToHomeGroup <itemName> <groupItemName> - adds an item to a home group",
                COMMAND_SETUP
                        + " removeItemFromHomeGroup <itemName> <groupItemName> - removes an item from a home group",
                COMMAND_SETUP + " enableChannel <channelUID> - removes all links and linked items of a channel",
                COMMAND_SETUP + " disableChannel <channelUID> - creates all links and linked items for a channel",
                COMMAND_SETUP + " setLabel <thingUID> <label> - sets a new label of the item linked to the thing" }));
    }

    protected void setThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = thingSetupManager;
    }

    protected void unsetThingSetupManager(ThingSetupManager thingSetupManager) {
        this.thingSetupManager = null;
    }

}
