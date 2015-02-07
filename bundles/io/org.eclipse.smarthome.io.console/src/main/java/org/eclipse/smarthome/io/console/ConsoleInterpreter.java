/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemNotUniqueException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.eclipse.smarthome.io.console.internal.ConsoleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides generic methods for handling console input (i.e. pure strings).
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ConsoleInterpreter {

    /**
     * This method simply takes a list of arguments, where the first one is treated
     * as the console command (such as "update", "send" etc.). The following entries
     * are then the arguments for this command.
     * If the command is unknown, the complete usage is printed to the console.
     * 
     * @param args array which contains the console command and all its arguments
     * @param console the console for printing messages for the user
     */
    static public void handleRequest(String[] args, Console console,
            Set<ConsoleCommandExtension> consoleCommandExtensions) {
        String arg = args[0];
        boolean handledByExtension = false;
        Iterator<ConsoleCommandExtension> extensionIterator = consoleCommandExtensions.iterator();
        Logger logger = LoggerFactory.getLogger(ConsoleInterpreter.class);

        while (!handledByExtension && extensionIterator.hasNext()) {
            ConsoleCommandExtension consoleCommandExtension = extensionIterator.next();
            // The clients should not break the whole processing, so we need to catch exceptions in canHandle
            boolean canHandle = false;
            try {
                canHandle = consoleCommandExtension.canHandle(args);
            } catch (Exception e) {
                logger.error("An error occured while invoking the command extension's can handle method.", e);
            }
            if (canHandle) {
                handledByExtension = true;
                try {
                    consoleCommandExtension.execute(args, console);
                } catch (Exception e) {
                    logger.error("An error occured while executing the console command.", e);
                    console.println("An unexpected error occured during execution.");
                }
            }
        }

        if (!handledByExtension) {
            args = (String[]) ArrayUtils.remove(args, 0);
            if (arg.equals("items")) {
                ConsoleInterpreter.handleItems(args, console);
            } else if (arg.equals("send")) {
                ConsoleInterpreter.handleSend(args, console);
            } else if (arg.equals("update")) {
                ConsoleInterpreter.handleUpdate(args, console);
            } else if (arg.equals("status")) {
                ConsoleInterpreter.handleStatus(args, console);
            } else {
                console.printUsage(getUsage(consoleCommandExtensions));
            }
        }
    }

    /**
     * This method handles an update command.
     * 
     * @param args array which contains the arguments for the update command
     * @param console the console for printing messages for the user
     */
    static public void handleUpdate(String[] args, Console console) {
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
                            console.printUsage(ConsoleInterpreter.getUpdateUsage());
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
                    console.printUsage(ConsoleInterpreter.getUpdateUsage());
                }
            } else {
                console.println("Sorry, no item registry service available!");
            }
        } else {
            console.println("Sorry, no event publisher service available!");
        }
    }

    /**
     * This method handles a send command.
     * 
     * @param args array which contains the arguments for the send command
     * @param console the console for printing messages for the user
     */
    static public void handleSend(String[] args, Console console) {
        ItemRegistry registry = ConsoleActivator.itemRegistryTracker.getService();
        EventPublisher publisher = ConsoleActivator.eventPublisherTracker.getService();
        if (publisher != null) {
            if (registry != null) {
                if (args.length > 0) {
                    String itemName = args[0];
                    try {
                        Item item = registry.getItemByPattern(itemName);
                        if (args.length > 1) {
                            String commandName = args[1];
                            Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandName);
                            if (command != null) {
                                publisher.sendCommand(itemName, command);
                                console.println("Command has been sent successfully.");
                            } else {
                                console.println("Error: Command '" + commandName + "' is not valid for item '"
                                        + itemName + "'");
                                console.print("Valid command types are: ( ");
                                for (Class<? extends Command> acceptedType : item.getAcceptedCommandTypes()) {
                                    console.print(acceptedType.getSimpleName() + " ");
                                }
                                console.println(")");
                            }
                        } else {
                            console.printUsage(ConsoleInterpreter.getCommandUsage());
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
                    console.printUsage(ConsoleInterpreter.getCommandUsage());
                }
            } else {
                console.println("Sorry, no item registry service available!");
            }
        } else {
            console.println("Sorry, no event publisher service available!");
        }
    }

    /**
     * This method handles an items command.
     * 
     * @param args array which contains the arguments for the items command
     * @param console the console for printing messages for the user
     */
    static public void handleItems(String[] args, Console console) {
        ItemRegistry registry = ConsoleActivator.itemRegistryTracker.getService();
        if (registry != null) {
            String pattern = (args.length == 0) ? "*" : args[0];
            Collection<Item> items = registry.getItems(pattern);
            if (items.size() > 0) {
                for (Item item : items) {
                    console.println(item.toString());
                }
            } else {
                console.println("No items found for this pattern.");
            }
        } else {
            console.println("Sorry, no item registry service available!");
        }
    }

    /**
     * This method handles a status command.
     * 
     * @param args array which contains the arguments for the status command
     * @param console the console for printing messages for the user
     */
    static public void handleStatus(String[] args, Console console) {
        ItemRegistry registry = ConsoleActivator.itemRegistryTracker.getService();
        if (registry != null) {
            if (args.length > 0) {
                String itemName = args[0];
                try {
                    Item item = registry.getItemByPattern(itemName);
                    console.println(item.getState().toString());
                } catch (ItemNotFoundException e) {
                    console.println("Error: Item '" + itemName + "' does not exist.");
                } catch (ItemNotUniqueException e) {
                    console.print("Error: Multiple items match this pattern: ");
                    for (Item item : e.getMatchingItems()) {
                        console.print(item.getName() + " ");
                    }
                }
            } else {
                console.printUsage(ConsoleInterpreter.getStatusUsage());
            }
        } else {
            console.println("Sorry, no item registry service available!");
        }
    }

    /** returns a CR-separated list of usage texts for all available commands */
    private static String getUsage(Set<ConsoleCommandExtension> consoleCommandExtensions) {
        StringBuilder sb = new StringBuilder();
        for (String usage : ConsoleInterpreter.getUsages(consoleCommandExtensions)) {
            sb.append(usage + "\n");
        }
        return sb.toString();
    }

    /** returns an array of the usage texts for all available commands */
    static public List<String> getUsages(Set<ConsoleCommandExtension> consoleCommandExtensions) {
        List<String> usages = new ArrayList<String>();
        for (ConsoleCommandExtension consoleCommandExtension : consoleCommandExtensions) {
            usages.addAll(consoleCommandExtension.getUsages());
        }

        usages.add(getUpdateUsage());
        usages.add(getCommandUsage());
        usages.add(getStatusUsage());
        usages.add(getItemsUsage());
        return usages;

    }

    static public String getUpdateUsage() {
        return "update <item> <state> - sends a status update for an item";
    }

    static public String getCommandUsage() {
        return "send <item> <command> - sends a command for an item";
    }

    static public String getStatusUsage() {
        return "status <item> - shows the current status of an item";
    }

    static public String getItemsUsage() {
        return "items [<pattern>] - lists names and types of all items matching the pattern";
    }

}
