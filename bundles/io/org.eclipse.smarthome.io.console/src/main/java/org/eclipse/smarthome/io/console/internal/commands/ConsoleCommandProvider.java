/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.ConsoleInterpreter;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * This class provides access to openHAB functionality through the OSGi console
 * of Equinox. Unfortunately, there these command providers are not standardized
 * for OSGi, so we need different implementations for different OSGi containers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ConsoleCommandProvider implements CommandProvider {

    protected Set<ConsoleCommandExtension> consoleCommandExtensions = new CopyOnWriteArraySet<ConsoleCommandExtension>();

    /**
     * Methods staring with "_" will be used as commands. We only define one command "smarthome" to make
     * sure we do not get into conflict with other existing commands. The different functionalities
     * can then be used by the first argument.
     * 
     * @param interpreter the equinox command interpreter
     * @return null, return parameter is not used
     */
    public Object _smarthome(CommandInterpreter interpreter) {
        String arg = interpreter.nextArgument();

        if (arg == null) {
            interpreter.println(getHelp());
            return null;
        }

        List<String> argsList = new ArrayList<String>();
        argsList.add(arg);
        Console console = new OSGiConsole(interpreter);

        while (true) {
            String narg = interpreter.nextArgument();
            if (!StringUtils.isEmpty(narg)) {
                argsList.add(narg);
            } else {
                break;
            }
        }

        String[] args = argsList.toArray(new String[argsList.size()]);
        ConsoleInterpreter.handleRequest(args, console, consoleCommandExtensions);

        return null;
    }

    public void addConsoleCommandExtension(ConsoleCommandExtension consoleCommandExtension) {
        this.consoleCommandExtensions.add(consoleCommandExtension);
    }

    public void removeConsoleCommandExtension(ConsoleCommandExtension consoleCommandExtension) {
        this.consoleCommandExtensions.remove(consoleCommandExtension);
    }

    /**
     * Contributes the usage of our command to the console help output.
     */
    @Override
    public String getHelp() {
        List<String> usages = ConsoleInterpreter.getUsages(consoleCommandExtensions);
        StringBuffer buffer = new StringBuffer();
        buffer.append("---SmartHome commands---\n\t");
        for (int i = 0; i < usages.size(); i++) {
            String usageString = usages.get(i);
            buffer.append("smarthome " + usageString + "\n");
            if (usages.size() > i + 1) {
                buffer.append("\t");
            }
        }

        return buffer.toString();
    }

    private static class OSGiConsole implements Console {

        private CommandInterpreter interpreter;

        public OSGiConsole(CommandInterpreter interpreter) {
            this.interpreter = interpreter;
        }

        @Override
        public void print(String s) {
            interpreter.print(s);
        }

        @Override
        public void println(String s) {
            interpreter.println(s);
        }

        @Override
        public void printUsage(String s) {
            interpreter.println("Usage: smarthome " + s);
        }

    }

}
