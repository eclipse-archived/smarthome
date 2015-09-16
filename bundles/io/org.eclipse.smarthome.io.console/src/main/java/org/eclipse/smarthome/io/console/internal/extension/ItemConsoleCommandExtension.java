/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * Console command extension to get item list
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Create DS for command extension
 * @author Dennis Nobel - Changed service references to be injected via DS
 * @author Simon Kaufmann - Added commands to clear and remove items
 *
 */
public class ItemConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_LIST = "list";
    private static final String SUBCMD_CLEAR = "clear";
    private static final String SUBCMD_REMOVE = "remove";

    private ItemRegistry itemRegistry;

    public ItemConsoleCommandExtension() {
        super("items", "Access the item registry.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] {
                buildCommandUsage(SUBCMD_LIST + " [<pattern>]",
                        "lists names and types of all items (matching the pattern, if given)"),
                buildCommandUsage(SUBCMD_CLEAR, "removes all items"),
                buildCommandUsage(SUBCMD_REMOVE + " <itemName>", "removes the given item") });
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_LIST:
                    listItems(console, (args.length < 2) ? "*" : args[1]);
                    break;
                case SUBCMD_CLEAR:
                    removeItems(console, itemRegistry.getAll());
                    break;
                case SUBCMD_REMOVE:
                    if (args.length > 1) {
                        String name = args[1];
                        Item item = itemRegistry.get(name);
                        removeItems(console, Collections.singleton(item));
                    } else {
                        console.println("Specify the name of the item to remove: " + this.getCommand() + " "
                                + SUBCMD_REMOVE + " <itemName>");
                    }
                    break;
                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        } else {
            listItems(console, "*");
        }
    }

    private void removeItems(Console console, Collection<Item> items) {
        int count = items.size();
        for (Item item : items) {
            itemRegistry.remove(item.getName());
        }
        console.println(count + " item(s) removed successfully.");
    }

    private void listItems(Console console, String pattern) {
        Collection<Item> items = this.itemRegistry.getItems(pattern);
        if (items.size() > 0) {
            for (Item item : items) {
                console.println(item.toString());
            }
        } else {
            console.println("No item found for this pattern.");
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

}
