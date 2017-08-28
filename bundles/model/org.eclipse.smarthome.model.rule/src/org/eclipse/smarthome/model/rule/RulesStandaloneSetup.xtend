/** 
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.rule

import com.google.inject.Injector

/** 
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
class RulesStandaloneSetup extends RulesStandaloneSetupGenerated {
    static Injector injector

    def static void doSetup() {
        if (injector === null) {
            injector = new RulesStandaloneSetup().createInjectorAndDoEMFRegistration()
        }
    }

    def static Injector getInjector() {
        return injector
    }
}
