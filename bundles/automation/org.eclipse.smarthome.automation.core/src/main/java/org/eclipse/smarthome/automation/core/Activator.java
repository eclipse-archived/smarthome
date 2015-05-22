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

public class Activator implements BundleActivator {
    private ServiceRegistration automationFactoryReg;
    private ServiceRegistration ruleAdminReg;
    private RuleRegistryImpl ruleAdmin;
    private ServiceRegistration templateAdminReg;
    private TemplateRegistryImpl templateAdmin;
    private ServiceRegistration moduleTypeAdminReg;
    private static ModuleTypeRegistryImpl moduleTypeAdmin;
    private static BundleContext bc;

    public void start(BundleContext bc) throws Exception {
        this.bc = bc;
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
