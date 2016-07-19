/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.io.console.rfc147.internal.ConsoleCommandsContainer;
import org.eclipse.smarthome.io.console.rfc147.internal.ConsoleSupportRfc147;

/**
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class HelpConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private ConsoleCommandsContainer commandsContainer;

    public HelpConsoleCommandExtension() {
        super("help", "Get help for all available commands.");
    }

    public void setConsoleCommandsContainer(final ConsoleCommandsContainer commandsContainer) {
        this.commandsContainer = commandsContainer;
    }

    // Add a method that name is equal to our command
    public void help(String[] args) {
        execute(args, ConsoleSupportRfc147.CONSOLE);
    }

    @Override
    public void execute(String[] args, Console console) {
        if (this.commandsContainer != null) {
            ConsoleInterpreter.printHelp(console, ConsoleSupportRfc147.CONSOLE.getBase(), ":",
                    this.commandsContainer.getConsoleCommandExtensions());
        }
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage(getDescription()));
    }

}
