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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * {@link ThingConsoleCommandExtension} provides console commands for listing and removing things.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class ThingConsoleCommandExtension implements ConsoleCommandExtension {

    private final static String COMMAND_THINGS = "things";
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;

    @Override
    public boolean canHandle(String[] args) {
        String firstArgument = args[0];
        return COMMAND_THINGS.equals(firstArgument);
    }

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];
        switch (command) {
            case COMMAND_THINGS:
                Collection<Thing> things = thingRegistry.getAll();
                if (args.length > 1) {
                    String subCommand = args[1];
                    switch (subCommand) {
                        case "list":
                            printThings(console, things);
                            return;
                        case "clear":
                            removeAllThings(console, things);
                            return;
                        case "remove":
                            if (args.length > 2) {
                                ThingUID thingUID = new ThingUID(args[2]);
                                removeThing(console, things, thingUID);
                            } else {
                                console.println("Specify thing id to remove: things remove <thingUID> (e.g. \"hue:light:1\")");
                            }
                            return;
                        default:
                            break;
                    }
                } else {
                    printThings(console, things);
                }
                return;
            default:
                return;
        }
    }

    private void removeThing(Console console, Collection<Thing> things, ThingUID thingUID) {
        Thing removedThing = this.managedThingProvider.remove(thingUID);
        if (removedThing != null) {
            console.println("Thing '" + thingUID + "' successfully removed.");
        } else {
            console.println("Could not delete thing " + thingUID + ".");
        }
    }

    private void removeAllThings(Console console, Collection<Thing> things) {
        int numberOfThings = things.size();
        for (Thing thing : things) {
            managedThingProvider.remove(thing.getUID());
        }
        console.println(numberOfThings + " things successfully removed.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList((new String[] { COMMAND_THINGS + " list - lists all things",
                COMMAND_THINGS + " clear - removes all managed things",
                COMMAND_THINGS + " remove <thingUID> - removes a thing" }));
    }

    private void printThings(Console console, Collection<Thing> things) {
        if (things.isEmpty()) {
            console.println("No things found.");
        }

        for (Thing thing : things) {
            String id = thing.getUID().toString();
            String thingType = thing instanceof Bridge ? "Bridge" : "Thing";
            ThingStatus status = thing.getStatus();

            console.println(String.format("%s (Type=%s, Status=%s)", id, thingType, status));
        }
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

}
