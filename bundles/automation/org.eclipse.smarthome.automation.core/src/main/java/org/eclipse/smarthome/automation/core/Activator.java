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

import org.eclipse.smarthome.automation.AutomationFactory;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.core.template.TemplateManager;
import org.eclipse.smarthome.automation.core.template.TemplateRegistryImpl;
import org.eclipse.smarthome.automation.core.type.ModuleTypeManager;
import org.eclipse.smarthome.automation.core.type.ModuleTypeRegistryImpl;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Bundle Activator
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
public class Activator implements BundleActivator {
    private ServiceRegistration automationFactoryReg;
    private ServiceRegistration ruleAdminReg;
    private RuleRegistryImpl ruleAdmin;
    private ServiceRegistration templateAdminReg;
    private TemplateRegistryImpl templateAdmin;
    private ServiceRegistration moduleTypeAdminReg;
    private static ModuleTypeRegistryImpl moduleTypeAdmin;
    private static BundleContext bc;

    @Override
    public void start(BundleContext bc) throws Exception {
        Activator.bc = bc;
        // log = new Log(bc);
        if (automationFactoryReg == null) {
            automationFactoryReg = bc.registerService(AutomationFactory.class.getName(), new AutomationFactoryImpl(),
                    null);
        }
        templateAdmin = new TemplateRegistryImpl(new TemplateManager(bc));
        templateAdminReg = bc.registerService(TemplateRegistry.class.getName(), templateAdmin, null);
        moduleTypeAdmin = new ModuleTypeRegistryImpl(new ModuleTypeManager(bc));
        moduleTypeAdminReg = bc.registerService(ModuleTypeRegistry.class.getName(), moduleTypeAdmin, null);
        RuleManagerImpl rm = new RuleManagerImpl(bc);
        RuleProvider rp = new RuleProvider(rm, bc);
        ruleAdmin = new RuleRegistryImpl(rm, rp);
        ruleAdminReg = bc.registerService(RuleRegistry.class.getName(), ruleAdmin, null);
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        if (automationFactoryReg != null) {
            automationFactoryReg.unregister();
            automationFactoryReg = null;
        }
        if (ruleAdminReg != null) {
            ruleAdminReg.unregister();
            ruleAdmin.dispose();
            ruleAdminReg = null;
        }

        if (templateAdminReg != null) {
            templateAdminReg.unregister();
            templateAdmin.dispose();
            templateAdminReg = null;
        }

        if (moduleTypeAdminReg != null) {
            moduleTypeAdminReg.unregister();
            moduleTypeAdmin.dispose();
            moduleTypeAdminReg = null;
        }

    }

}
