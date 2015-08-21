/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.core.template.TemplateManager;
import org.eclipse.smarthome.automation.core.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class is an activator of this bundle. Opens the all used service trackers and registers the services -
 * {@link ModuleTypeRegistry}, {@link TemplateRegistry}, {@link RuleRegistry} and {@link AutomationFactory}.
 *
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 */
public class Activator implements BundleActivator {

    static ModuleTypeRegistryImpl moduleTypeRegistry;
    static TemplateRegistryImpl templateRegistry;
    static BundleContext bc;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ ruleRegistryReg;
    private RuleRegistryImpl ruleRegistry;
    private ServiceRegistration/* <?> */ ruleProviderReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ templateRegistryReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ moduleTypeRegistryReg;
    @SuppressWarnings("rawtypes")
    private ServiceTracker storageTracker;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(final BundleContext bc) throws Exception {
        Activator.bc = bc;
        templateRegistry = new TemplateRegistryImpl(new TemplateManager(bc));
        templateRegistryReg = bc.registerService(TemplateRegistry.class.getName(), templateRegistry, null);
        moduleTypeRegistry = new ModuleTypeRegistryImpl(new ModuleTypeManager(bc));
        moduleTypeRegistryReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeRegistry, null);

        final RuleEngine rm = new RuleEngine(bc);

        ruleRegistry = new RuleRegistryImpl(rm);
        ruleRegistryReg = bc.registerService(RuleRegistry.class.getName(), ruleRegistry, null);

        storageTracker = new ServiceTracker(bc, StorageService.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                StorageService storage = (StorageService) bc.getService(reference);
                if (storage != null) {
                    Storage storageDisabledRules = storage.getStorage("automation_rules_disabled",
                            this.getClass().getClassLoader());
                    ruleRegistry.setDisabledRuleStorage(storageDisabledRules);
                    final ManagedRuleProvider rp = new ManagedRuleProvider(storage);
                    ruleRegistry.addProvider(rp);
                    Dictionary props = new Hashtable(3);
                    props.put("REG_PROP_MANAGED_PROVIDE", Boolean.TRUE);
                    ruleProviderReg = bc.registerService(RuleProvider.class.getName(), rp, props);
                }
                return storage;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (ruleProviderReg != null) {
                    ruleProviderReg.unregister();
                    ruleProviderReg = null;
                }

                if (ruleRegistryReg != null) {
                    ruleRegistryReg.unregister();
                    ruleRegistry.dispose();
                    ruleRegistryReg = null;
                }
            }
        });
        storageTracker.open();

    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        if (ruleRegistryReg != null) {
            ruleRegistryReg.unregister();
            ruleRegistry.dispose();
            ruleRegistryReg = null;
        }

        if (templateRegistryReg != null) {
            templateRegistryReg.unregister();
            templateRegistry.dispose();
            templateRegistryReg = null;
        }

        if (moduleTypeRegistryReg != null) {
            moduleTypeRegistryReg.unregister();
            moduleTypeRegistry.dispose();
            moduleTypeRegistryReg = null;
        }

        storageTracker.close();
        storageTracker = null;

    }

}