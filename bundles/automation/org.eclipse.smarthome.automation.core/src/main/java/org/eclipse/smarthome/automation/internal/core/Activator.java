/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.core;

import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.events.RuleEventFactory;
import org.eclipse.smarthome.automation.internal.core.template.TemplateManager;
import org.eclipse.smarthome.automation.internal.core.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.internal.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.internal.core.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class is an activator of this bundle. Opens the all used service trackers and registers the services -
 * {@link ModuleTypeRegistry}, {@link TemplateRegistry}, {@link RuleRegistry} and {@link AutomationFactory}.
 *
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 * @author Benedikt Niehues - added events for rules
 */
public class Activator implements BundleActivator {

    static ModuleTypeRegistryImpl moduleTypeRegistry;
    static TemplateRegistryImpl templateRegistry;
    static RuleEventFactory ruleEventFactory;
    static BundleContext bc;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ ruleRegistryReg;
    private RuleRegistryImpl ruleRegistry;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ managedRuleProviderReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ templateRegistryReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration/* <?> */ moduleTypeRegistryReg;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration ruleEventFactoryReg;
    @SuppressWarnings("rawtypes")
    private ServiceTracker serviceTracker;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(final BundleContext bc) throws Exception {
        Activator.bc = bc;
        final RuleEngine re = new RuleEngine(bc);

        templateRegistry = new TemplateRegistryImpl(new TemplateManager(bc, re));
        templateRegistryReg = bc.registerService(TemplateRegistry.class.getName(), templateRegistry, null);
        moduleTypeRegistry = new ModuleTypeRegistryImpl(new ModuleTypeManager(bc, re));
        moduleTypeRegistryReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeRegistry, null);
        ruleEventFactory = new RuleEventFactory();
        ruleEventFactoryReg = bc.registerService(EventFactory.class, ruleEventFactory, null);

        ruleRegistry = new RuleRegistryImpl(re);
        ruleRegistryReg = bc.registerService(RuleRegistry.class.getName(), ruleRegistry, null);

        Filter filter = bc.createFilter("(|(" + Constants.OBJECTCLASS + "=" + StorageService.class.getName() + ")("
                + Constants.OBJECTCLASS + "=" + RuleProvider.class.getName() + ")("
                + Constants.OBJECTCLASS + "=" + EventPublisher.class.getName() + "))");

        serviceTracker = new ServiceTracker(bc, filter, new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = bc.getService(reference);
                if (service instanceof StorageService) {
                    StorageService storage = (StorageService) service;
                    if (storage != null) {
                        Storage storageDisabledRules = storage.getStorage("automation_rules_disabled",
                                this.getClass().getClassLoader());
                        ruleRegistry.setDisabledRuleStorage(storageDisabledRules);

                        final ManagedRuleProvider rp = new ManagedRuleProvider(storage);
                        ruleRegistry.addProvider(rp);

                        managedRuleProviderReg = bc.registerService(RuleProvider.class.getName(), rp, null);
                        return storage;
                    }
                } else if (service instanceof RuleProvider) {
                    RuleProvider rp = (RuleProvider) service;
                    ruleRegistry.addProvider(rp);
                    return rp;
                } else if (service instanceof EventPublisher){
                    EventPublisher ep = (EventPublisher)service;
                    ruleRegistry.setEventPublisher(ep);
                    return ep;
                }
                return null;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (service instanceof StorageService) {
                    if (managedRuleProviderReg != null) {
                        managedRuleProviderReg.unregister();
                        managedRuleProviderReg = null;
                    }
                } else if (service instanceof EventPublisher){
                    ruleRegistry.unsetEventPublisher((EventPublisher)service);
                }
            }
        });
        serviceTracker.open();

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
        
        if (ruleEventFactoryReg!=null){
            ruleEventFactoryReg.unregister();
            ruleEventFactory=null;
            ruleEventFactoryReg=null;
        }

        serviceTracker.close();
        serviceTracker = null;

    }

}