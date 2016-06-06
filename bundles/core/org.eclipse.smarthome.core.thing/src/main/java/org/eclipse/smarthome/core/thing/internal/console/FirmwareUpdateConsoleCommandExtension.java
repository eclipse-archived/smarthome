/**
 * Copyright (c) 2016 Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.internal.console;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.firmware.Firmware;
import org.eclipse.smarthome.core.thing.binding.firmware.FirmwareUID;
import org.eclipse.smarthome.core.thing.firmware.FirmwareRegistry;
import org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo;
import org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateService;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;

/**
 * {@link FirmwareUpdateConsoleCommandExtension} provides console commands for managing the firmwares of things.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class FirmwareUpdateConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_LIST = "list";
    private static final String SUBCMD_STATUS = "status";
    private static final String SUBCMD_UPDATE = "update";

    private FirmwareUpdateService firmwareUpdateService;
    private FirmwareRegistry firmwareRegistry;
    private ThingRegistry thingRegistry;

    public FirmwareUpdateConsoleCommandExtension() {
        super("firmware", "Manage your things' firmwares.");
    }

    @Override
    public void execute(String[] args, Console console) {
        int numberOfArguments = args.length;
        if (numberOfArguments < 1) {
            console.println("No firmware subcommand given.");
            printUsage(console);
            return;
        }

        String subCommand = args[0];

        switch (subCommand) {
            case SUBCMD_LIST:
                listFirmwares(console, args);
                break;
            case SUBCMD_STATUS:
                listFirmwareStatus(console, args);
                break;
            case SUBCMD_UPDATE:
                updateFirmware(console, args);
                break;
            default:
                console.println(String.format("Unkown firmware sub command '%s'.", subCommand));
                printUsage(console);
                break;
        }
    }

    private void listFirmwares(Console console, String[] args) {
        if (args.length != 2) {
            console.println("Specify the thing type id to get its available firmwares: firmware list <thingTypeUID>");
            return;
        }

        ThingTypeUID thingTypeUID = new ThingTypeUID(args[1]);
        Collection<Firmware> firmwares = firmwareRegistry.getFirmwares(thingTypeUID);

        if (firmwares.isEmpty()) {
            console.println("No firmwares found.");
        }

        for (Firmware firmware : firmwares) {
            console.println(firmware.toString());
        }
    }

    private void listFirmwareStatus(Console console, String[] args) {
        if (args.length != 2) {
            console.println("Specify the thing id to get its firmware status: firmware status <thingUID>");
            return;
        }

        ThingUID thingUID = new ThingUID(args[1]);
        FirmwareStatusInfo firmwareStatusInfo = firmwareUpdateService.getFirmwareStatusInfo(thingUID);

        if (firmwareStatusInfo != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("Firmware status for thing with UID %s is %s.", thingUID,
                    firmwareStatusInfo.getFirmwareStatus()));

            if (firmwareStatusInfo.getUpdatableFirmwareUID() != null) {
                sb.append(String.format(" The latest updatable firmware version is %s.",
                        firmwareStatusInfo.getUpdatableFirmwareUID().getFirmwareVersion()));
            }

            console.println(sb.toString());
        }
    }

    private void updateFirmware(Console console, String[] args) {
        if (args.length != 3) {
            console.println(
                    "Specify the thing id and the firmware version to update the firmware: firmware update <thingUID> <firmware version>");
            return;
        }

        ThingUID thingUID = new ThingUID(args[1]);
        Thing thing = thingRegistry.get(thingUID);
        FirmwareUID firmwareUID = new FirmwareUID(thing.getThingTypeUID(), args[2]);

        firmwareUpdateService.updateFirmware(thingUID, firmwareUID, null);

        console.println("Firmware update started.");
    }

    @Override
    public List<String> getUsages() {
        return Arrays
                .asList(new String[] {
                        buildCommandUsage(SUBCMD_LIST + " <thingTypeUID>",
                                "lists the available firmwares for a thing type"),
                        buildCommandUsage(SUBCMD_STATUS + " <thingUID>", "lists the firmware status for a thing"),
                        buildCommandUsage(SUBCMD_UPDATE + " <thingUID> <firmware version>",
                                "updates the firmware for a thing") });
    }

    protected void setFirmwareUpdateService(FirmwareUpdateService firmwareUpdateService) {
        this.firmwareUpdateService = firmwareUpdateService;
    }

    protected void unsetFirmwareUpdateService(FirmwareUpdateService firmwareUpdateService) {
        this.firmwareUpdateService = null;
    }

    protected void setFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = firmwareRegistry;
    }

    protected void unsetFirmwareRegistry(FirmwareRegistry firmwareRegistry) {
        this.firmwareRegistry = null;
    }

    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }
}
