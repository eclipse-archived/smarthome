/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
     * @param args array which contains the console command and all its arguments
     * @return true if the extension is able to handle the command
     */
    boolean canHandle(String[] args);

    /**
     * This method called if {@link #canHandle(String[]) canHandle} returns true.
     * Clients are not allowed to throw exceptions. They have to write corresponding messages to the given
     * {@link Console}
     * 
     * @param args array which contains the console command and all its arguments
     * @param console the console used to print
     */
    void execute(String[] args, Console console);

    /**
     * @return the help texts for this extension
     */
    List<String> getUsages();
}
