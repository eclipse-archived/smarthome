/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.console.internal.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.multimedia.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.io.multimedia.text.InterpretationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Console command extension to interpret human language commands.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class InterpretConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private ItemRegistry itemRegistry;

    public InterpretConsoleCommandExtension() {
        super("interpret", "Interpret a command by a human language interpreter.");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<command>", "interprets the human language command"));
    }

    @Override
    public void execute(String[] args, Console console) {
        String msg = String.join(" ", args);
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Collection<ServiceReference<HumanLanguageInterpreter>> refs = null;
        try {
            refs = context.getServiceReferences(HumanLanguageInterpreter.class, null);
        } catch (InvalidSyntaxException e) {
            // should never happen
        }
        if (refs != null && refs.size() > 0) {
            try {
                console.println(context.getService(refs.iterator().next()).interpret(Locale.ENGLISH, msg));
            } catch (InterpretationException ie) {
                console.println(ie.getMessage());
            }
        } else {
            console.println("No human language interpreter available - tried to interpret: " + msg);
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

}
