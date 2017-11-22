/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.internal;

import org.eclipse.smarthome.automation.ManagedRuleProvider;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.test.storage.VolatileStorageService;

public class RuleRegistryMockup extends RuleRegistryImpl {

    public RuleRegistryMockup () {
        super();
        final StorageService storageService = new VolatileStorageService();

        final ManagedRuleProvider managedRuleProvider = new TestingManagedRuleProvider(storageService);

        setStorageService(storageService);
        setManagedProvider(managedRuleProvider);
        setModuleTypeRegistry(new ModuleTypeRegistryMockup());

        addProvider(managedRuleProvider);
    }

    static class TestingManagedRuleProvider extends ManagedRuleProvider {
        public TestingManagedRuleProvider(final StorageService storageService) {
            super();
            setStorageService(storageService);
        }
    }

}
