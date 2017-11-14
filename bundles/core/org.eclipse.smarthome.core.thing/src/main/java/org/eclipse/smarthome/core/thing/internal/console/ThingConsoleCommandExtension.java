/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.console;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.events.ThingEventFactory;
import org.eclipse.smarthome.core.thing.i18n.ThingStatusInfoI18nLocalizationService;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link ThingConsoleCommandExtension} provides console commands for listing and removing things.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Höfer - Added localization of thing status
 * @author Stefan Triller - Added trigger channel command
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class ThingConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_LIST = "list";
    private static final String SUBCMD_CLEAR = "clear";
    private static final String SUBCMD_REMOVE = "remove";
    private static final String SUBCMD_TRIGGER = "trigger";

    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;
    private ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService;
    private EventPublisher eventPublisher;

    public ThingConsoleCommandExtension() {
        super("things", "Access your thing registry.");
    }

    @Override
    public void execute(String[] args, Console console) {
        Collection<Thing> things = thingRegistry.getAll();
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_LIST:
                    printThings(console, things);
                    return;
                case SUBCMD_CLEAR:
                    removeAllThings(console, things);
                    return;
                case SUBCMD_REMOVE:
                    if (args.length > 1) {
                        ThingUID thingUID = new ThingUID(args[1]);
                        removeThing(console, things, thingUID);
                    } else {
                        console.println("Specify thing id to remove: things remove <thingUID> (e.g. \"hue:light:1\")");
                    }
                    return;
                case SUBCMD_TRIGGER:
                    if (args.length == 3) {
                        triggerChannel(console, args[1], args[2]);
                    } else if (args.length == 2) {
                        triggerChannel(console, args[1], null);
                    } else {
                        console.println("Command '" + subCommand + "' needs arguments <channelUID> [<event>]");
                    }
                    break;
                default:
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void triggerChannel(Console console, String channelUid, String event) {
        eventPublisher.post(ThingEventFactory.createTriggerEvent(event, new ChannelUID(channelUid)));
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
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_LIST, "lists all things"),
                buildCommandUsage(SUBCMD_CLEAR, "removes all managed things"),
                buildCommandUsage(SUBCMD_REMOVE + " <thingUID>", "removes a thing"),
                buildCommandUsage(SUBCMD_TRIGGER + " <channelUID> [<event>]",
                        "triggers the <channelUID> with <event> (if given)") });
    }

    private void printThings(Console console, Collection<Thing> things) {
        if (things.isEmpty()) {
            console.println("No things found.");
        }

        for (Thing thing : things) {
            String id = thing.getUID().toString();
            String thingType = thing instanceof Bridge ? "Bridge" : "Thing";
            ThingStatusInfo status = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null);
            ThingUID bridgeUID = thing.getBridgeUID();
            String label = thing.getLabel();

            console.println(String.format("%s (Type=%s, Status=%s, Label=%s, Bridge=%s)", id, thingType, status, label,
                    bridgeUID));
        }
    }

    @Reference
    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    @Reference
    protected void setThingStatusInfoI18nLocalizationService(
            ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService) {
        this.thingStatusInfoI18nLocalizationService = thingStatusInfoI18nLocalizationService;
    }

    protected void unsetThingStatusInfoI18nLocalizationService(
            ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService) {
        this.thingStatusInfoI18nLocalizationService = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

}
