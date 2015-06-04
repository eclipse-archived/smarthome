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

package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.handler.provider.ModuleTypeProvider;
import org.eclipse.smarthome.automation.handler.provider.TemplateProvider;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

/**
 * @author Ana Dimova - Initial Contribution
 */
public abstract class AutomationCommands {

    protected static final int RULE_ADMIN = 1;
    protected static final int TEMPLATE_ADMIN = 2;
    protected static final int MODULE_TYPE_ADMIN = 3;

    protected static final String IMPORT_MODULE_TYPES = "importModuleTypes";
    protected static final String IMPORT_MODULE_TYPES_SHORT = "imt";

    protected static final String IMPORT_TEMPLATES = "importTemplates";
    protected static final String IMPORT_TEMPLATES_SHORT = "it";

    protected static final String IMPORT_RULES = "importRules";
    protected static final String IMPORT_RULES_SHORT = "ir";

    protected static final String EXPORT_MODULE_TYPES = "exportModuleTypes";
    protected static final String EXPORT_MODULE_TYPES_SHORT = "emt";

    protected static final String EXPORT_TEMPLATES = "exportTemplates";
    protected static final String EXPORT_TEMPLATES_SHORT = "et";

    protected static final String EXPORT_RULES = "exportRules";
    protected static final String EXPORT_RULES_SHORT = "er";

    protected static final String REMOVE_RULE = "removeRule";
    protected static final String REMOVE_RULE_SHORT = "rmr";

    protected static final String REMOVE_RULES = "removeRules";
    protected static final String REMOVE_RULES_SHORT = "rmrs";

    protected static final String REMOVE_TEMPLATES = "removeTemplates";
    protected static final String REMOVE_TEMPLATES_SHORT = "rmts";

    protected static final String REMOVE_MODULE_TYPES = "removetModuleTypes";
    protected static final String REMOVE_MODULE_TYPES_SHORT = "rmmts";

    protected static final String LIST_MODULE_TYPES = "listModuleTypes";
    protected static final String LIST_MODULE_TYPES_SHORT = "lsmt";

    protected static final String LIST_TEMPLATES = "listTemplates";
    protected static final String LIST_TEMPLATES_SHORT = "lst";

    protected static final String LIST_RULES = "listRules";
    protected static final String LIST_RULES_SHORT = "lsr";

    protected BundleContext bc;
    protected ModuleTypeProviderImpl moduleTypeProvider;
    protected TemplateProviderImpl templateProvider;
    protected RuleProvider ruleProvider;

    protected ServiceRegistration tpReg;
    protected ServiceRegistration mtpReg;

    public AutomationCommands(BundleContext bc) {
        this.bc = bc;
        moduleTypeProvider = new ModuleTypeProviderImpl(bc);
        templateProvider = new TemplateProviderImpl(bc);
        ruleProvider = new RuleProvider(bc);

        mtpReg = bc.registerService(
                new String[] { ModuleTypeProvider.class.getName(), ModuleTypeProvider.class.getName() },
                moduleTypeProvider, null);

        tpReg = bc.registerService(new String[] { TemplateProvider.class.getName(), TemplateProvider.class.getName() },
                templateProvider, null);
    }

    /**
     *
     * @param uid
     * @return
     */
    public abstract Rule getRule(String uid);

    /**
     * @param ruleFilter
     * @return
     */
    public abstract Collection<Rule> getRules(String ruleFilter);

    /**
     *
     * @param templateUID
     * @param locale
     * @return
     */
    public abstract Template getTemplate(String templateUID, Locale locale);

    /**
     * @return
     */
    public abstract Collection<Template> getTemplates(Locale locale);

    /**
     * @param typeUID
     * @param locale
     * @return
     */
    public abstract ModuleType getModuleType(String typeUID, Locale locale);

    /**
     * @param clazz
     * @param locale
     * @return
     */
    public abstract <T extends ModuleType> Collection<T> getModuleTypes(Class<T> clazz, Locale locale);

    /**
     * @param id
     * @return
     */
    public abstract boolean removeRule(String id);

    /**
     * @param id
     * @return
     */
    public abstract boolean removeRules(String id);

    /**
     *
     * @param command
     * @param params
     * @return
     */
    protected abstract AutomationCommand parseCommand(String command, String[] params);

    /**
     *
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportModuleTypes(String parserType, Set set, File file) {
        if (moduleTypeProvider != null) {
            return moduleTypeProvider.exportModuleTypes(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     *
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportTemplates(String parserType, Set set, File file) {
        if (templateProvider != null) {
            return templateProvider.exportTemplates(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     *
     * @param parserType
     * @param set
     * @param file
     */
    public Status exportRules(String parserType, Set set, File file) {
        if (ruleProvider != null) {
            return ruleProvider.exportRules(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     *
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importModuleTypes(String parserType, URL url) {
        if (moduleTypeProvider != null) {
            return moduleTypeProvider.importModuleTypes(parserType, url);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        Set<Status> set = new HashSet<Status>();
        set.add(s);
        return set;
    }

    /**
     *
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importTemplates(String parserType, URL url) {
        if (templateProvider != null) {
            return templateProvider.importTemplates(parserType, url);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        Set<Status> set = new HashSet<Status>();
        set.add(s);
        return set;
    }

    /**
     *
     * @param parserType
     * @param url
     * @return
     */
    public Set<Status> importRules(String parserType, URL url) {
        if (ruleProvider != null) {
            return ruleProvider.importRules(parserType, url);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        Set<Status> set = new HashSet<Status>();
        set.add(s);
        return set;
    }

    /**
     *
     * @param adminType
     * @param url
     * @return
     */
    public boolean remove(int adminType, URL url) {
        switch (adminType) {
            case AutomationCommands.MODULE_TYPE_ADMIN:
                if (moduleTypeProvider != null) {
                    moduleTypeProvider.remove(url);
                    return true;
                }
                return false;
            case AutomationCommands.TEMPLATE_ADMIN:
                if (templateProvider != null) {
                    templateProvider.remove(url);
                    return true;
                }
                return false;
        }
        return false;
    }

    /**
     *
     * @param command
     * @param params
     * @return
     */
    public String executeCommand(String command, String[] params) {
        AutomationCommand commandInst = parseCommand(command, params);
        if (commandInst != null) {
            String res = commandInst.execute();
            return res;
        }
        return null;
    }

    /**
   *
   */
    public void stop() {
        moduleTypeProvider.close();
        templateProvider.close();
        ruleProvider.close();
        mtpReg.unregister();
        tpReg.unregister();
        moduleTypeProvider = null;
        templateProvider = null;
        ruleProvider = null;
    }

}