/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.io.voice.text.InterpretationException;
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

    public InterpretConsoleCommandExtension() {
        super("interpret", "Interpret a command by a human language interpreter.");
    }

    @Override
    public List<String> getUsages() {
        return Collections.singletonList(buildCommandUsage("<command>", "interprets the human language command"));
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length < 1) {
            console.println("Nothing to interpret");
            return;
        }
        StringBuilder sb = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            sb.append(" ");
            sb.append(args[i]);
        }
        String msg = sb.toString();
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Collection<ServiceReference<HumanLanguageInterpreter>> refs = null;
        try {
            refs = context.getServiceReferences(HumanLanguageInterpreter.class, null);
        } catch (InvalidSyntaxException e) {
            // should never happen
        }
        if (refs != null && refs.size() > 0) {
            try {
                HumanLanguageInterpreter interpreter = context.getService(refs.iterator().next());
                Locale locale = pickLanguage(interpreter.getSupportedLocales());
                console.println(interpreter.interpret(locale, msg));
            } catch (InterpretationException ie) {
                console.println(ie.getMessage());
            }
        } else {
            console.println("No human language interpreter available - tried to interpret: " + msg);
        }
    }

    /**
     * Picks the best fit from a set of available languages (given by {@link Locale}s).
     * Matching happens in the following priority order:
     * 1st: system's default {@link Locale} (e.g. "de-DE"), if contained in {@link locales}
     * 2nd: first item in {@link locales} matching system default language (e.g. "de" matches "de-CH")
     * 3rd: first language in {@link locales}
     * 4th: English, if {@link locales} is null or empty
     *
     * @param locales set of supported {@link Locale}s to pick from
     * @return {@link Locale} of the best fitting language
     */
    private Locale pickLanguage(Set<Locale> locales) {
        if (locales == null || locales.size() == 0) {
            return Locale.ENGLISH;
        }
        Locale locale = Locale.getDefault();
        if (locales.contains(locale)) {
            return locale;
        }
        String language = locale.getLanguage();
        Locale first = null;
        for (Locale l : locales) {
            if (first == null) {
                first = l;
            }
            if (language.equals(l.getLanguage())) {
                return l;
            }
        }
        return first;
    }

}
