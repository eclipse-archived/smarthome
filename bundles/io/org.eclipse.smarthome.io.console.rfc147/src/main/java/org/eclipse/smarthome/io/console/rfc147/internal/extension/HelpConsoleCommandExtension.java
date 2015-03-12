/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.rfc147.internal.extension;

import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.ConsoleInterpreter;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.rfc147.internal.Rfc147Manager;

/**
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class HelpConsoleCommandExtension extends AbstractConsoleCommandExtension {

    public HelpConsoleCommandExtension() {
        super("help", "Get help for all available commands.");
    }

    // Add a method that name is equal to our command
    public void help(String[] args) {
        execute(args, Rfc147Manager.CONSOLE);
    }

    @Override
    public void execute(String[] args, Console console) {
        ConsoleInterpreter.printHelp(console, Rfc147Manager.CONSOLE.getBase(), ":",
                Rfc147Manager.getConsoleCommandExtensions());
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage(getDescription()));
    }

}
