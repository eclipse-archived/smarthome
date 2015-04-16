/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

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
 *
 */
public class ItemConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private ItemRegistry itemRegistry;

    public ItemConsoleCommandExtension() {
        super("items", "Get information about items.");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("[<pattern>]",
                "lists names and types of all items matching the pattern"));
    }

    @Override
    public void execute(String[] args, Console console) {
        String pattern = (args.length == 0) ? "*" : args[0];
        Collection<Item> items = this.itemRegistry.getItems(pattern);
        if (items.size() > 0) {
            for (Item item : items) {
                console.println(item.toString());
            }
        } else {
            console.println("No items found for this pattern.");
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

}
