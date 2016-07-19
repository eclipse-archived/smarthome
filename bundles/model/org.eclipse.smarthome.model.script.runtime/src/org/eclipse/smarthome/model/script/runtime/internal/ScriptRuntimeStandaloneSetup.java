/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.model.script.ScriptStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ScriptRuntimeStandaloneSetup extends ScriptStandaloneSetup {

    private static Injector injector;

    public static void doSetup() {
        if (injector == null) {
            injector = new ScriptRuntimeStandaloneSetup().createInjectorAndDoEMFRegistration();
        }
    }

    static public Injector getInjector() {
        return injector;
    }

    @Override
    public Injector createInjector() {
        return Guice.createInjector(new org.eclipse.smarthome.model.script.ScriptRuntimeModule(),
                new ScriptRuntimeModule());
    }
}
