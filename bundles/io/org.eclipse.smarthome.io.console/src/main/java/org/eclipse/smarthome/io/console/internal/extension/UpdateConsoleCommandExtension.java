/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.internal.ConsoleActivator;

/**
 * Console command extension to send status update to item
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Create DS for command extension
 *
 */
public class UpdateConsoleCommandExtension extends AbstractConsoleCommandExtension {

    public UpdateConsoleCommandExtension() {
        super("update", "Send a state update to an item.");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<item> <state>", "sends a status update for an item"));
    }

    @Override
    public void execute(String[] args, Console console) {
        ItemRegistry registry = ConsoleActivator.itemRegistryTracker.getService();
        EventPublisher publisher = ConsoleActivator.eventPublisherTracker.getService();
        if (publisher != null) {
            if (registry != null) {
                if (args.length > 0) {
                    String itemName = args[0];
                    try {
                        Item item = registry.getItemByPattern(itemName);
                        if (args.length > 1) {
                            String stateName = args[1];
                            State state = TypeParser.parseState(item.getAcceptedDataTypes(), stateName);
                            if (state != null) {
                                publisher.postUpdate(item.getName(), state);
                                console.println("Update has been sent successfully.");
                            } else {
                                console.println("Error: State '" + stateName + "' is not valid for item '" + itemName
                                        + "'");
                                console.print("Valid data types are: ( ");
                                for (Class<? extends State> acceptedType : item.getAcceptedDataTypes()) {
                                    console.print(acceptedType.getSimpleName() + " ");
                                }
                                console.println(")");
                            }
                        } else {
                            printUsage(console);
                        }
                    } catch (ItemNotFoundException e) {
                        console.println("Error: Item '" + itemName + "' does not exist.");
                    } catch (ItemNotUniqueException e) {
                        console.print("Error: Multiple items match this pattern: ");
                        for (Item item : e.getMatchingItems()) {
                            console.print(item.getName() + " ");
                        }
                    }
                } else {
                    printUsage(console);
                }
            } else {
                console.println("Sorry, no item registry service available!");
            }
        } else {
            console.println("Sorry, no event publisher service available!");
        }
    }

}
