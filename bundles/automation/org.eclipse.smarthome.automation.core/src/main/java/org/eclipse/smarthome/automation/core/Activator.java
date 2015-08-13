/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.core.template.TemplateManager;
import org.eclipse.smarthome.automation.core.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
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

    protected static AutomationFactory automationFactory;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration /* <?> */ automationFactoryReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ ruleRegistryReg;
    private RuleRegistryImpl ruleRegistry;
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
        if (automationFactoryReg == null) {
            automationFactory = new AutomationFactoryImpl();
            automationFactoryReg = bc.registerService(AutomationFactory.class.getName(), automationFactory, null);
        }
        templateRegistry = new TemplateRegistryImpl(new TemplateManager(bc));
        templateRegistryReg = bc.registerService(TemplateRegistry.class.getName(), templateRegistry, null);
        moduleTypeRegistry = new ModuleTypeRegistryImpl(new ModuleTypeManager(bc));
        moduleTypeRegistryReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeRegistry, null);

        final RuleEngine rm = new RuleEngine(bc);

        storageTracker = new ServiceTracker(bc, StorageService.class.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                StorageService storage = (StorageService) bc.getService(reference);
                if (storage != null) {
                    final ManagedRuleProvider rp = new ManagedRuleProvider(storage);
                    ruleRegistry = new RuleRegistryImpl(rm, rp);
                    ruleRegistryReg = bc.registerService(RuleRegistry.class.getName(), ruleRegistry, null);
                }
                return storage;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
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

        if (automationFactoryReg != null) {
            automationFactoryReg.unregister();
            automationFactory = null;
            automationFactoryReg = null;
        }

        storageTracker.close();
        storageTracker = null;

    }

}