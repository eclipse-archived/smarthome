/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.extensions;

import java.util.List;

import org.eclipse.smarthome.io.console.Console;

/**
 * Client which provide a console command have to implement this interface
 *
 * @author Oliver Libutzki
 *
 */
public interface ConsoleCommandExtension {

    /**
     * Get the command of for the extension.
     *
     * @return command for the extension
     */
    String getCommand();

    /**
     * Get the description for the extension.
     *
     * @return description for the extension
     */
    String getDescription();

    /**
     * This method called if a {@link #getCommand() command} for that extension is called.
     * Clients are not allowed to throw exceptions. They have to write corresponding messages to the given
     * {@link Console}
     *
     * @param args array which contains all the console command arguments
     * @param console the console used to print
     */
    void execute(String[] args, Console console);

    /**
     * @return the help texts for this extension
     */
    List<String> getUsages();
}
