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
import java.util.List;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLinkRegistry;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * {@link LinkConsoleCommandExtension} provides console commands for listing,
 * addding and removing links.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Alex Tugarev - Added support for links between items and things
 */
public class LinkConsoleCommandExtension implements ConsoleCommandExtension {

    private final static String COMMAND_LINKS = "links";
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ItemThingLinkRegistry itemThingLinkRegistry;

    @Override
    public boolean canHandle(String[] args) {
        String firstArgument = args[0];
        return COMMAND_LINKS.equals(firstArgument);
    }

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];
        switch (command) {
            case COMMAND_LINKS:
                if (args.length > 1) {
                    String subCommand = args[1];
                    switch (subCommand) {
                        case "list":
                            list(console, itemChannelLinkRegistry.getAll(), itemThingLinkRegistry.getAll());
                            return;
                        case "addChannelLink":
                            if (args.length > 3) {
                                String itemName = args[2];
                                ChannelUID channelUID = new ChannelUID(args[3]);
                                addChannelLink(console, itemName, channelUID);
                            } else {
                                console.println("Specify item name and channel UID to link: link <itemName> <channelUID>");
                            }
                            return;
                        case "removeChannelLink":
                            if (args.length > 3) {
                                String itemName = args[2];
                                ChannelUID channelUID = new ChannelUID(args[3]);
                                removeChannelLink(console, itemName, channelUID);
                            } else {
                                console.println("Specify item name and channel UID to unlink: link <itemName> <channelUID>");
                            }
                            return;
                        case "addThingLink":
                            if (args.length > 3) {
                                String itemName = args[2];
                                ThingUID thingUID = new ThingUID(args[3]);
                                addThingLink(console, itemName, thingUID);
                            } else {
                                console.println("Specify item name and thing UID to link: link <itemName> <thingUID>");
                            }
                            return;
                        case "removeThingLink":
                            if (args.length > 3) {
                                String itemName = args[2];
                                ThingUID thingUID = new ThingUID(args[3]);
                                removeThingLink(console, itemName, thingUID);
                            } else {
                                console.println("Specify item name and thing UID to unlink: link <itemName> <thingUID>");
                            }
                            return;
                        case "clear":
                            clear(console);
                            return;
                        default:
                            break;
                    }
                } else {
                    list(console, itemChannelLinkRegistry.getAll(), itemThingLinkRegistry.getAll());
                }
                return;
            default:
                return;
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList((new String[] { COMMAND_LINKS + " list - lists all links",
                COMMAND_LINKS + " addChannelLink <itemName> <channelUID> - links an item with a channel",
                COMMAND_LINKS + " addThingLink <itemName> <thingUID> - links an item with a thing",
                COMMAND_LINKS + " removeChannelLink <itemName> <channelUID> - unlinks an item with a channel",
                COMMAND_LINKS + " removeThingLink <itemName> <thingUID> - unlinks an item with a thing",
                COMMAND_LINKS + " clear - removes all managed links" }));
    }

    private void clear(Console console) {
        Collection<ItemChannelLink> itemChannelLinks = itemChannelLinkRegistry.getAll();
        Collection<ItemThingLink> itemThingLinks = itemThingLinkRegistry.getAll();
        int numberOfLinks = itemChannelLinks.size() + itemThingLinks.size();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            itemChannelLinkRegistry.remove(itemChannelLink.getID());
        }
        for (ItemThingLink itemThingLink : itemThingLinks) {
            itemThingLinkRegistry.remove(itemThingLink.getID());
        }
        console.println(numberOfLinks + " links successfully removed.");
    }

    private void addChannelLink(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        itemChannelLinkRegistry.add(itemChannelLink);
        console.println("Link " + itemChannelLink.toString() + " successfully added.");
    }

    private void addThingLink(Console console, String itemName, ThingUID thingUID) {
        ItemThingLink itemThingLink = new ItemThingLink(itemName, thingUID);
        itemThingLinkRegistry.add(itemThingLink);
        console.println("Link " + itemThingLink.toString() + " successfully added.");
    }

    private void list(Console console, Collection<ItemChannelLink> itemChannelLinks,
            Collection<ItemThingLink> itemThingLinks) {
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            console.println(itemChannelLink.toString());
        }
        for (ItemThingLink itemThingLink : itemThingLinks) {
            console.println(itemThingLink.toString());
        }
    }

    private void removeChannelLink(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        ItemChannelLink removedItemChannelLink = itemChannelLinkRegistry.remove(itemChannelLink.getID());
        if (removedItemChannelLink != null) {
            console.println("Link " + itemChannelLink.toString() + "successfully removed.");
        } else {
            console.println("Could not remove link " + itemChannelLink.toString() + ".");
        }
    }

    private void removeThingLink(Console console, String itemName, ThingUID thingUID) {
        ItemThingLink itemThingLink = new ItemThingLink(itemName, thingUID);
        ItemThingLink removedItemThingLink = itemThingLinkRegistry.remove(itemThingLink.getID());
        if (removedItemThingLink != null) {
            console.println("Link " + removedItemThingLink.toString() + "successfully removed.");
        } else {
            console.println("Could not remove link " + itemThingLink.toString() + ".");
        }
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void setItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = itemThingLinkRegistry;
    }

    protected void unsetItemThingLinkRegistry(ItemThingLinkRegistry itemThingLinkRegistry) {
        this.itemThingLinkRegistry = null;
    }

}
