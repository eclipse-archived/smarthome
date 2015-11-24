/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.automation.RuleProvider;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.core.internal.composite.CompositeModuleHandlerFactory;
import org.eclipse.smarthome.automation.core.internal.template.TemplateManager;
import org.eclipse.smarthome.automation.core.internal.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.core.internal.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.internal.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.events.RuleEventFactory;
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
import org.osgi.service.cm.ManagedService;
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

    private ModuleTypeRegistryImpl moduleTypeRegistry;
    private TemplateRegistryImpl templateRegistry;
    private RuleEventFactory ruleEventFactory;

    private ModuleTypeManager mtManager;
    private TemplateManager tManager;

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
    private ServiceRegistration<?> configReg;
    private RuleEngine ruleEngine;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(final BundleContext bc) throws Exception {
        ruleEngine = new RuleEngine(bc);

        Hashtable props = new Hashtable(11);
        props.put(Constants.SERVICE_PID, "smarthome.rule.configuration");
        configReg = bc.registerService(ManagedService.class.getName(), ruleEngine, props);

        this.tManager = new TemplateManager(bc, ruleEngine);
        ruleEngine.setTemplateManager(tManager);
        mtManager = new ModuleTypeManager(bc, ruleEngine);
        ruleEngine.setModuleTypeManager(mtManager);
        ruleEngine.setCompositeModuleFactory(new CompositeModuleHandlerFactory(bc, mtManager, ruleEngine));
        ConnectionValidator.setManager(mtManager);

        templateRegistry = new TemplateRegistryImpl(tManager);
        templateRegistryReg = bc.registerService(TemplateRegistry.class.getName(), templateRegistry, null);
        moduleTypeRegistry = new ModuleTypeRegistryImpl(mtManager);
        moduleTypeRegistryReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeRegistry, null);
        ruleEventFactory = new RuleEventFactory();
        ruleEventFactoryReg = bc.registerService(EventFactory.class.getName(), ruleEventFactory, null);

        ruleRegistry = new RuleRegistryImpl(ruleEngine);
        ruleRegistryReg = bc.registerService(RuleRegistry.class.getName(), ruleRegistry, null);

        Filter filter = bc.createFilter("(|(" + Constants.OBJECTCLASS + "=" + StorageService.class.getName() + ")("
                + Constants.OBJECTCLASS + "=" + RuleProvider.class.getName() + ")(" + Constants.OBJECTCLASS + "="
                + EventPublisher.class.getName() + "))");

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
                        managedRuleProviderReg = bc.registerService(RuleProvider.class.getName(), rp, null);
                        return storage;
                    }
                } else if (service instanceof RuleProvider) {
                    RuleProvider rp = (RuleProvider) service;
                    ruleRegistry.addProvider(rp);
                    return rp;
                } else if (service instanceof EventPublisher) {
                    EventPublisher ep = (EventPublisher) service;
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
                } else if (service instanceof EventPublisher) {
                    if (ruleRegistry != null) {
                        ruleRegistry.unsetEventPublisher((EventPublisher) service);
                    }
                } else if (service instanceof RuleProvider) {
                    if (ruleRegistry != null) {
                        RuleProvider rp = (RuleProvider) service;
                        ruleRegistry.removeProvider(rp);
                    }
                }

            }
        });
        serviceTracker.open();

    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        serviceTracker.close();
        serviceTracker = null;

        if (configReg != null) {
            configReg.unregister();
            configReg = null;
        }

        if (ruleRegistryReg != null) {
            ruleRegistryReg.unregister();
            ruleRegistryReg = null;
            ruleRegistry.dispose();
            ruleRegistry = null;
        }

        if (templateRegistryReg != null) {
            templateRegistryReg.unregister();
            templateRegistryReg = null;
            templateRegistry.dispose();
            templateRegistry = null;
        }

        if (moduleTypeRegistryReg != null) {
            moduleTypeRegistryReg.unregister();
            moduleTypeRegistryReg = null;
            moduleTypeRegistry.dispose();
            moduleTypeRegistry = null;
        }

        if (ruleEventFactoryReg != null) {
            ruleEventFactoryReg.unregister();
            ruleEventFactory = null;
            ruleEventFactoryReg = null;
        }

        ruleEngine.dispose();

    }

}