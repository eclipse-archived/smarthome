/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.eclipse.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.ConsoleInterpreter;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * This class provides access to Eclipse SmartHome functionality through the OSGi console
 * of Equinox. Unfortunately, there these command providers are not standardized
 * for OSGi, so we need different implementations for different OSGi containers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Split console interface and specific implementation
 *
 */
public class ConsoleSupportEclipse implements CommandProvider {

    private final String BASE = "smarthome";

    private SortedMap<String, ConsoleCommandExtension> consoleCommandExtensions = Collections
            .synchronizedSortedMap(new TreeMap<String, ConsoleCommandExtension>());

    public ConsoleSupportEclipse() {
    }

    public void addConsoleCommandExtension(ConsoleCommandExtension consoleCommandExtension) {
        consoleCommandExtensions.put(consoleCommandExtension.getCommand(), consoleCommandExtension);
    }

    public void removeConsoleCommandExtension(ConsoleCommandExtension consoleCommandExtension) {
        consoleCommandExtensions.remove(consoleCommandExtension.getCommand());
    }

    private ConsoleCommandExtension getConsoleCommandExtension(final String cmd) {
        return consoleCommandExtensions.get(cmd);
    }

    private Collection<ConsoleCommandExtension> getConsoleCommandExtensions() {
        final Set<ConsoleCommandExtension> set = new HashSet<>();
        set.addAll(consoleCommandExtensions.values());
        return set;
    }

    /**
     * Methods staring with "_" will be used as commands. We only define one command "smarthome" to make
     * sure we do not get into conflict with other existing commands. The different functionalities
     * can then be used by the first argument.
     *
     * @param interpreter the equinox command interpreter
     * @return null, return parameter is not used
     */
    public Object _smarthome(final CommandInterpreter interpreter) {

        final Console console = new OSGiConsole(BASE, interpreter);

        final String cmd = interpreter.nextArgument();
        if (cmd == null) {
            ConsoleInterpreter.printHelp(console, BASE, " ", getConsoleCommandExtensions());
        } else {
            final ConsoleCommandExtension extension = getConsoleCommandExtension(cmd);
            if (extension == null) {
                console.println(String.format("No handler for command '%s' was found.", cmd));
            } else {
                // Build argument list
                final List<String> argsList = new ArrayList<String>();
                while (true) {
                    final String narg = interpreter.nextArgument();
                    if (!StringUtils.isEmpty(narg)) {
                        argsList.add(narg);
                    } else {
                        break;
                    }
                }
                final String[] args = new String[argsList.size()];
                argsList.toArray(args);

                ConsoleInterpreter.execute(console, extension, args);
            }
        }

        return null;
    }

    @Override
    public String getHelp() {
        return ConsoleInterpreter.getHelp(BASE, " ", getConsoleCommandExtensions());
    }

}