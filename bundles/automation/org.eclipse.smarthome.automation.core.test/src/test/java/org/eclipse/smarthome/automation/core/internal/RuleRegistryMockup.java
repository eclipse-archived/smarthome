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
