/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.karaf.internal;

import java.util.List;

import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.ConsoleInterpreter;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;

/**
 * This class wraps ESH ConsoleCommandExtensions to commands for Apache Karaf
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class CommandWrapper implements Command {

    // Define a scope for all commands.
    public static final String SCOPE = "smarthome";

    private final ConsoleCommandExtension command;

    public CommandWrapper(final ConsoleCommandExtension command) {
        this.command = command;
    }

    @Override
    public Object execute(Session session, List<Object> argList) throws Exception {
        final String[] args = new String[argList.size()];
        for (int i = 0; i < args.length; ++i) {
            final Object ele = argList.get(i);
            final String str;
            if (ele instanceof String) {
                str = (String) ele;
            } else {
                str = ele.toString();
            }
            args[i] = str;
        }

        final Console console = new OSGiConsole(getScope());

        if (args.length == 1 && "--help".equals(args[0])) {
            for (final String usage : command.getUsages()) {
                console.printUsage(usage);
            }
        } else {
            ConsoleInterpreter.execute(console, command, args);
        }
        return null;
    }

    @Override
    public Completer getCompleter(boolean arg0) {
        return null;
    }

    @Override
    public String getDescription() {
        return command.getDescription();
    }

    @Override
    public String getName() {
        return command.getCommand();
    }

    @Override
    public Parser getParser() {
        return null;
    }

    @Override
    public String getScope() {
        return SCOPE;
    }

}
