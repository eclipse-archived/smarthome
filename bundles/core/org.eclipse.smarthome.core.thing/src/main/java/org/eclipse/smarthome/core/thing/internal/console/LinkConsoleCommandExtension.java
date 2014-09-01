/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * {@link LinkConsoleCommandExtension} provides console commands for listing,
 * addding and removing links.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class LinkConsoleCommandExtension implements ConsoleCommandExtension {

    private final static String COMMAND_LINKS = "links";
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;

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
                    list(console, itemChannelLinkRegistry.getAll());
                    return;
                case "add":
                    if (args.length > 3) {
                        String itemName = args[2];
                        ChannelUID channelUID = new ChannelUID(args[3]);
                        link(console, itemName, channelUID);
                    } else {
                        console.println("Specify item name and channel UID to link: link <itemName> <channelUID>");
                    }
                    return;
                case "remove":
                    if (args.length > 3) {
                        String itemName = args[2];
                        ChannelUID channelUID = new ChannelUID(args[3]);
                        remove(console, itemName, channelUID);
                    } else {
                        console.println("Specify item name and channel UID to unlink: link <itemName> <channelUID>");
                    }
                    return;
                case "clear":
                    clear(console);
                    return;
                default:
                    break;
                }
            } else {
                list(console, itemChannelLinkRegistry.getAll());
            }
            return;
        default:
            return;
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList((new String[] { COMMAND_LINKS + " list - lists all links",
                COMMAND_LINKS + " add <itemName> <channelUID> - links an item with a channel",
                COMMAND_LINKS + " remove <itemName> <channelUID> - unlinks an item with a channel",
                COMMAND_LINKS + " clear - removes all managed links" }));
    }

    private void clear(Console console) {
        Collection<ItemChannelLink> itemChannelLinks = managedItemChannelLinkProvider.getAll();
        int numberOfLinks = itemChannelLinks.size();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            managedItemChannelLinkProvider.remove(itemChannelLink.getID());
        }
        console.println(numberOfLinks + " links successfully removed.");
    }

    private void link(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        managedItemChannelLinkProvider.add(itemChannelLink);
        console.println("Link " + itemChannelLink.toString() + " successfully added.");
    }

    private void list(Console console, Collection<ItemChannelLink> itemChannelLinks) {
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            console.println(itemChannelLink.toString());
        }
    }

    private void remove(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        ItemChannelLink removedItemChannelLink = managedItemChannelLinkProvider
                .remove(itemChannelLink.getID());
        if (removedItemChannelLink != null) {
            console.println("Link " + itemChannelLink.toString() + "successfully removed.");
        } else {
            console.println("Could not remove link " + itemChannelLink.toString() + ".");
        }
    }

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    protected void setManagedItemChannelLinkProvider(
            ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = managedItemChannelLinkProvider;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.itemChannelLinkRegistry = null;
    }

    protected void unsetManagedItemChannelLinkProvider(
            ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = null;
    }

}
