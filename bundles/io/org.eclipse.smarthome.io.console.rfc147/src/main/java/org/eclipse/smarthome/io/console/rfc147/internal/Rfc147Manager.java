/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.rfc147.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.eclipse.smarthome.io.console.rfc147.internal.extension.HelpConsoleCommandExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 *
 * @author Markus Rathgeb - Initial contribution and API
 *
 */
public class Rfc147Manager {

    // private static final String KEY_SCOPE = CommandProcessor.COMMAND_SCOPE;
    // private static final String KEY_FUNCTION = CommandProcessor.COMMAND_FUNCTION;
    private static final String KEY_SCOPE = "osgi.command.scope";
    private static final String KEY_FUNCTION = "osgi.command.function";

    private static final String SCOPE = "smarthome";

    public static final OSGiConsole CONSOLE = new OSGiConsole(SCOPE);

    private final BundleContext bc;

    private static final Map<ConsoleCommandExtension, ServiceRegistration<?>> commands = Collections
            .synchronizedMap(new HashMap<ConsoleCommandExtension, ServiceRegistration<?>>());

    public Rfc147Manager(BundleContext bc) {
        this.bc = bc;
        addCommand(new HelpConsoleCommandExtension());
    }

    private void cleanupOld(final ServiceRegistration<?> serviceRegistration) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Dictionary<String, Object> createProperties() {
        return (Dictionary) new Properties();
    }

    public void addCommand(final ConsoleCommandExtension cmd) {

        Dictionary<String, Object> props = createProperties();
        props.put(KEY_SCOPE, SCOPE);
        props.put(KEY_FUNCTION, cmd.getCommand());

        final ServiceRegistration<?> serviceRegistrationOld;
        final ServiceRegistration<?> serviceRegistrationNew;

        serviceRegistrationNew = bc.registerService(CommandWrapper.class.getName(), new CommandWrapper(cmd), props);
        serviceRegistrationOld = commands.put(cmd, serviceRegistrationNew);
        cleanupOld(serviceRegistrationOld);
    }

    public void removeCommand(final ConsoleCommandExtension cmd) {
        cleanupOld(commands.remove(cmd));
    }

    public static Collection<ConsoleCommandExtension> getConsoleCommandExtensions() {
        final Set<ConsoleCommandExtension> set = new HashSet<>();
        set.addAll(commands.keySet());
        return set;
    }

}
