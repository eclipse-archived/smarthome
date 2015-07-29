/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.smarthome.automation.core.template.TemplateManager;
import org.eclipse.smarthome.automation.core.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;

public class Activator implements BundleActivator {
    
    static ModuleTypeRegistryImpl moduleTypeRegistry;
    static TemplateRegistryImpl templateRegistry;
    static BundleContext bc;
    
    protected static AutomationFactory automationFactory;
    
    private ServiceRegistration /* <?> */automationFactoryReg;
    private ServiceRegistration/* <?> */ruleRegistryReg;
    private RuleRegistryImpl ruleRegistry;
    private ServiceRegistration/* <?> */templateRegistryReg;
    private ServiceRegistration/* <?> */moduleTypeRegistryReg;

    public void start(BundleContext bc) throws Exception {
        Activator.bc = bc;
        // log = new Log(bc);
        if (automationFactoryReg == null) {
            automationFactory = new AutomationFactoryImpl();
            automationFactoryReg = bc.registerService(AutomationFactory.class.getName(), automationFactory, null);
        }
        templateRegistry = new TemplateRegistryImpl(new TemplateManager(bc));
        templateRegistryReg = bc.registerService(TemplateRegistry.class.getName(), templateRegistry, null);
        moduleTypeRegistry = new ModuleTypeRegistryImpl(new ModuleTypeManager(bc));
        moduleTypeRegistryReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeRegistry, null);
        
        RuleManagerImpl rm = new RuleManagerImpl(bc);
        RuleProvider rp = new RuleProvider(rm, bc);
        ruleRegistry = new RuleRegistryImpl(rm, rp);
        ruleRegistryReg = bc.registerService(RuleRegistry.class.getName(), ruleRegistry, null);
    }

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

    }

}