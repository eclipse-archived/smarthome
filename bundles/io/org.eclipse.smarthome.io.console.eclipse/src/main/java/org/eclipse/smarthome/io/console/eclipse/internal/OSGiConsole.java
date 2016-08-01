/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.eclipse.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.smarthome.io.console.Console;

/**
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Split to separate file
 *
 */
public class OSGiConsole implements Console {

    private final String baseCommand;
    private final CommandInterpreter interpreter;

    public OSGiConsole(final String baseCommand, final CommandInterpreter interpreter) {
        this.baseCommand = baseCommand;
        this.interpreter = interpreter;
    }

    @Override
    public void print(final String s) {
        interpreter.print(s);
    }

    @Override
    public void println(final String s) {
        interpreter.println(s);
    }

    @Override
    public void printUsage(final String s) {
        interpreter.println(String.format("Usage: %s %s", baseCommand, s));
    }

}
