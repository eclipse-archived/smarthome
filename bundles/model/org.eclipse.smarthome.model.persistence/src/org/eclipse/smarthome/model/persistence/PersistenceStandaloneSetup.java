/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.persistence;


/**
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
public class PersistenceStandaloneSetup extends PersistenceStandaloneSetupGenerated {

    public static void doSetup() {
        new PersistenceStandaloneSetup().createInjectorAndDoEMFRegistration();
    }
}
