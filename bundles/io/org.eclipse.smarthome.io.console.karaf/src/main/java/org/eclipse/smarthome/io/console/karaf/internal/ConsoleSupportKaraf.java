/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.karaf.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.karaf.shell.api.console.Registry;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple container for all command extensions.
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class ConsoleSupportKaraf {

    private Logger logger = LoggerFactory.getLogger(ConsoleSupportKaraf.class);

    private SessionFactory sessionFactory;

    // This collection contains all available / known commands.
    private final Collection<ConsoleCommandExtension> commands = new HashSet<>();

    // This map contains all registered commands.
    private final Map<ConsoleCommandExtension, CommandWrapper> registeredCommands = new HashMap<>();

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        registerCommands();
    }

    public void unsetSessionFactory(SessionFactory sessionFactory) {
        if (this.sessionFactory == sessionFactory) {
            unregisterCommands();
            sessionFactory = null;
        }
    }

    public void addConsoleCommandExtension(final ConsoleCommandExtension consoleCommandExtension) {
        commands.add(consoleCommandExtension);
        if (sessionFactory != null) {
            registerCommand(consoleCommandExtension);
        }
    }

    public void removeConsoleCommandExtension(final ConsoleCommandExtension consoleCommandExtension) {
        commands.remove(consoleCommandExtension);
        if (sessionFactory != null) {
            unregisterCommand(consoleCommandExtension);
        }
    }

    /**
     * Register all commands.
     *
     * The session factory must be not null.
     */
    private void registerCommands() {
        for (final ConsoleCommandExtension command : commands) {
            registerCommand(command);
        }
    }

    /**
     * Unregister all commands.
     *
     * The session factory must be not null.
     */
    private void unregisterCommands() {
        for (final ConsoleCommandExtension command : commands) {
            registerCommand(command);
        }
    }

    /**
     * Register a command.
     *
     * The session factory must be not null.
     *
     * @param command The command that should be registered.
     */
    private void registerCommand(final ConsoleCommandExtension command) {
        /*
         * TODO:
         * Keep this as long as we are using bundles with the "old" console command extension interface.
         * The registry calls function of the wrapper, that calls method of the command that are not available before.
         * The try catch around the register method does not catch this.
         * So if there is at least one command that does not complain to the new interface
         * this component stops working and the result is that no command is available.
         */
        if (!(command instanceof AbstractConsoleCommandExtension)) {
            logger.warn("Skip extensions that not using the new interface.");
            return;
        }

        final Registry registry = sessionFactory.getRegistry();

        final CommandWrapper wrapperOld;
        final CommandWrapper wrapperNew;

        wrapperNew = new CommandWrapper(command);
        wrapperOld = registeredCommands.put(command, wrapperNew);
        if (wrapperOld != null) {
            registry.unregister(wrapperOld);
        }
        try {
            registry.register(wrapperNew);
        } catch (final Exception ex) {
            logger.error("Cannot register command '{}'.", command.getCommand(), ex);
        }
    }

    /**
     * Unregister a command.
     *
     * The session factory must be not null.
     *
     * @param command The command that should be unregistered.
     */
    private void unregisterCommand(final ConsoleCommandExtension command) {
        final Registry registry = sessionFactory.getRegistry();

        final CommandWrapper wrapperOld;

        wrapperOld = registeredCommands.get(command);
        if (wrapperOld != null) {
            try {
                registry.unregister(wrapperOld);
            } catch (final Exception ex) {
                logger.error("Cannot unregister command '{}'.", command.getCommand(), ex);
            }
        }
    }

}
