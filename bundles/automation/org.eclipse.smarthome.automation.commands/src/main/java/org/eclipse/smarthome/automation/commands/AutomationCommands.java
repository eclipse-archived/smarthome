/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

/**
 * This class provides mechanism to separate the Automation Commands implementation from the Automation Core
 * implementation.
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public abstract class AutomationCommands {

    /**
     * This static field is used to switch between providers in different commands.
     */
    protected static final int RULE_PROVIDER = 1;

    /**
     * This static field is used to switch between providers in different commands.
     */
    protected static final int TEMPLATE_PROVIDER = 2;

    /**
     * This static field is used to switch between providers in different commands.
     */
    protected static final int MODULE_TYPE_PROVIDER = 3;

    /**
     * This static field is an identifier of the command {@link AutomationCommandImport} for {@link ModuleType}s.
     */
    protected static final String IMPORT_MODULE_TYPES = "importModuleTypes";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandImport} for {@link ModuleType}s.
     */
    protected static final String IMPORT_MODULE_TYPES_SHORT = "imt";

    /**
     * This static field is an identifier of the command {@link AutomationCommandImport} for {@link RuleTemplate}s.
     */
    protected static final String IMPORT_TEMPLATES = "importTemplates";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandImport} for {@link RuleTemplate}s.
     */
    protected static final String IMPORT_TEMPLATES_SHORT = "it";

    /**
     * This static field is an identifier of the command {@link AutomationCommandImport} for {@link Rule}s.
     */
    protected static final String IMPORT_RULES = "importRules";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandImport} for {@link Rule}s.
     */
    protected static final String IMPORT_RULES_SHORT = "ir";

    /**
     * This static field is an identifier of the command {@link AutomationCommandExport} for {@link ModuleType}s.
     */
    protected static final String EXPORT_MODULE_TYPES = "exportModuleTypes";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandExport} for {@link ModuleType}s.
     */
    protected static final String EXPORT_MODULE_TYPES_SHORT = "emt";

    /**
     * This static field is an identifier of the command {@link AutomationCommandExport} for {@link RuleTemplate}s.
     */
    protected static final String EXPORT_TEMPLATES = "exportTemplates";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandExport} for {@link RuleTemplate}s.
     */
    protected static final String EXPORT_TEMPLATES_SHORT = "et";

    /**
     * This static field is an identifier of the command {@link AutomationCommandExport} for {@link Rule}s.
     */
    protected static final String EXPORT_RULES = "exportRules";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandExport} for {@link Rule}s.
     */
    protected static final String EXPORT_RULES_SHORT = "er";

    /**
     * This static field is an identifier of the command {@link AutomationCommandRemove} for {@link Rule}.
     */
    protected static final String REMOVE_RULE = "removeRule";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandRemove} for {@link Rule}.
     */
    protected static final String REMOVE_RULE_SHORT = "rmr";

    /**
     * This static field is an identifier of the command {@link AutomationCommandRemove} for {@link Rule}s.
     */
    protected static final String REMOVE_RULES = "removeRules";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandRemove} for {@link Rule}s.
     */
    protected static final String REMOVE_RULES_SHORT = "rmrs";

    /**
     * This static field is an identifier of the command {@link AutomationCommandRemove} for {@link RuleTemplate}s.
     */
    protected static final String REMOVE_TEMPLATES = "removeTemplates";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandRemove} for {@link RuleTemplate}s.
     */
    protected static final String REMOVE_TEMPLATES_SHORT = "rmts";

    /**
     * This static field is an identifier of the command {@link AutomationCommandRemove} for {@link ModuleType}s.
     */
    protected static final String REMOVE_MODULE_TYPES = "removeModuleTypes";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandRemove} for {@link ModuleType}s.
     */
    protected static final String REMOVE_MODULE_TYPES_SHORT = "rmmts";

    /**
     * This static field is an identifier of the command {@link AutomationCommandList} for {@link ModuleType}s.
     */
    protected static final String LIST_MODULE_TYPES = "listModuleTypes";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandList} for {@link ModuleType}s.
     */
    protected static final String LIST_MODULE_TYPES_SHORT = "lsmt";

    /**
     * This static field is an identifier of the command {@link AutomationCommandList} for {@link RuleTemplate}s.
     */
    protected static final String LIST_TEMPLATES = "listTemplates";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandList} for {@link RuleTemplate}s.
     */
    protected static final String LIST_TEMPLATES_SHORT = "lst";

    /**
     * This static field is an identifier of the command {@link AutomationCommandList} for {@link Rule}s.
     */
    protected static final String LIST_RULES = "listRules";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandList} for {@link Rule}s.
     */
    protected static final String LIST_RULES_SHORT = "lsr";

    /**
     * This static field is an identifier of the command {@link AutomationCommandEnableRule}.
     */
    protected static final String ENABLE_RULE = "enableRule";

    /**
     * This static field is a short identifier of the command {@link AutomationCommandEnableRule}.
     */
    protected static final String ENABLE_RULE_SHORT = "enr";

    /**
     * This field serves for the {@link ModuleTypeProvider} service and the {@link TemplateProvider} service
     * registration.
     */
    protected BundleContext bc;

    /**
     * This field holds a reference to the {@link CommandlineModuleTypeProvider} instance.
     */
    protected CommandlineModuleTypeProvider moduleTypeProvider;

    /**
     * This field holds a reference to the {@link CommandlineTemplateProvider} instance.
     */
    protected CommandlineTemplateProvider templateProvider;

    /**
     * This field holds a reference to the {@link CommandlineRuleImporter} instance.
     */
    protected CommandlineRuleImporter ruleImporter;

    /**
     * This field holds a reference to the {@link ModuleTypeProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration tpReg;

    /**
     * This field holds a reference to the {@link TemplateProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration mtpReg;

    /**
     * This constructor is responsible for initializing instances of {@link CommandlineModuleTypeProvider},
     * {@link CommandlineTemplateProvider} and {@link CommandlineRuleImporter} and for registering the services
     * {@link ModuleTypeProvider} and {@link TemplateProvider}
     *
     * @param bc it is the {@link BundleContext}. It serves here to register the services {@link ModuleTypeProvider} and
     *            {@link TemplateProvider}
     */
    public AutomationCommands(BundleContext bc) {
        this.bc = bc;
        moduleTypeProvider = new CommandlineModuleTypeProvider(bc);
        templateProvider = new CommandlineTemplateProvider(bc);
        ruleImporter = new CommandlineRuleImporter(bc);

        mtpReg = bc.registerService(
                new String[] { ModuleTypeProvider.class.getName(), ModuleTypeProvider.class.getName() },
                moduleTypeProvider, null);

        tpReg = bc.registerService(new String[] { TemplateProvider.class.getName(), TemplateProvider.class.getName() },
                templateProvider, null);
    }

    /**
     * This method is used for getting the rule corresponding to the specified UID from the RuleEngine.
     *
     * @param uid specifies the wanted {@link Rule} uniquely.
     * @return a {@link Rule}, corresponding to the specified UID.
     */
    public abstract Rule getRule(String uid);

    /**
     * This method is used to get the all existing rules from the RuleEngine.
     *
     * @return a collection of all existing rules in the RuleEngine.
     */
    public abstract Collection<Rule> getRules();

    public abstract RuleStatus getRuleStatus(String uid);

    public abstract void setEnabled(String uid, boolean isEnabled);

    /**
     * This method is used for getting the {@link RuleTemplate} corresponding to the specified UID from the manager of
     * the {@link Template}s.
     *
     * @param templateUID specifies the wanted {@link RuleTemplate} uniquely.
     * @param locale a {@link Locale} that specifies the variant of the {@link RuleTemplate} that the user wants to see.
     *            Can be <code>null</code> and then the default locale will be used.
     * @return a {@link RuleTemplate}, corresponding to the specified UID and locale.
     */
    public abstract Template getTemplate(String templateUID, Locale locale);

    /**
     * This method is used for getting the collection of {@link RuleTemplate}s corresponding to the specified locale
     * from the manager of the {@link Template}s.
     *
     * @param locale a {@link Locale} that specifies the variant of the {@link RuleTemplate}s that the user wants to
     *            see.
     *            Can be <code>null</code> and then the default locale will be used.
     * @return a collection of {@link RuleTemplate}s, corresponding to the specified locale.
     */
    public abstract Collection<Template> getTemplates(Locale locale);

    /**
     * This method is used for getting the {@link ModuleType} corresponding to the specified UID from the manager of the
     * {@link ModuleType}s.
     *
     * @param typeUID specifies the wanted {@link ModuleType} uniquely.
     * @param locale a {@link Locale} that specifies the variant of the {@link ModuleType} that the user wants to see.
     *            Can be <code>null</code> and then the default locale will be used.
     * @return a {@link ModuleType}, corresponding to the specified UID and locale.
     */
    public abstract ModuleType getModuleType(String typeUID, Locale locale);

    /**
     * This method is used for getting the collection of {@link ModuleType}s corresponding to the specified class and
     * locale from the manager of the {@link ModuleType}s.
     *
     * @param clazz can be {@link TriggerType}, {@link ConditionType} or {@link ActionType} class.
     * @param locale a {@link Locale} that specifies the variant of the {@link ModuleType}s that the user wants to see.
     *            Can be <code>null</code> and then the default locale will be used.
     * @return a collection of {@link ModuleType}s from given class and locale.
     */
    public abstract <T extends ModuleType> Collection<T> getModuleTypes(Class<T> clazz, Locale locale);

    /**
     * This method is used for removing a rule corresponding to the specified UID from the RuleEngine.
     *
     * @param uid specifies the wanted {@link Rule} uniquely.
     * @return <b>true</b> if succeeds and <b>false</b> if fails.
     */
    public abstract boolean removeRule(String uid);

    /**
     * This method is used for removing the rules from the RuleEngine, corresponding to the specified filter.
     *
     * @param ruleFilter specifies the wanted {@link Rule}s.
     * @return <b>true</b> if succeeds and <b>false</b> if fails.
     */
    public abstract boolean removeRules(String ruleFilter);

    /**
     * This method is responsible for choosing a particular class of commands and creates an instance of this class on
     * the basis of the identifier of the command.
     *
     * @param command is the identifier of the command.
     * @param parameterValues is an array of strings which are basis for initializing the options and parameters of the
     *            command. The order for their description is a random.
     * @return an instance of the class corresponding to the identifier of the command.
     */
    protected abstract AutomationCommand parseCommand(String command, String[] parameterValues);

    /**
     * This method is responsible for exporting a set of {@link ModuleType}s in a specified file.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link ModuleType}s in text.
     * @param set a set of {@link ModuleType}s to export.
     * @param file a specified file for export.
     * @return a {@link Status} object, representing understandable for the user message containing information on the
     *         outcome of the export.
     */
    public Status exportModuleTypes(String parserType, Set<ModuleType> set, File file) {
        if (moduleTypeProvider != null) {
            return moduleTypeProvider.exportModuleTypes(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     * This method is responsible for exporting a set of {@link Template}s in a specified file.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link Template}s in text.
     * @param set a set of {@link Template}s to export.
     * @param file a specified file for export.
     * @return a {@link Status} object, representing understandable for the user message containing information on the
     *         outcome of the export.
     */
    public Status exportTemplates(String parserType, Set<RuleTemplate> set, File file) {
        if (templateProvider != null) {
            return templateProvider.exportTemplates(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     * This method is responsible for exporting a set of {@link Rule}s in a specified file.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link Rule}s in text.
     * @param set a set of {@link Rule}s to export.
     * @param file a specified file for export.
     * @return a {@link Status} object, representing understandable for the user message containing information on the
     *         outcome of the export.
     */
    public Status exportRules(String parserType, Set<Rule> set, File file) {
        if (ruleImporter != null) {
            return ruleImporter.exportRules(parserType, set, file);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        return s;
    }

    /**
     * This method is responsible for importing a set of {@link ModuleType}s from a specified file or URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link ModuleType}s from text.
     * @param url is a specified file or URL resource.
     * @return a set of {@link Status} objects, representing understandable for the user message containing information
     *         on the outcome of the import per each {@link ModuleType}.
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
     * This method is responsible for importing a set of {@link Template}s from a specified file or URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link Template}s from text.
     * @param url is a specified file or URL resource.
     * @return a set of {@link Status} objects, representing understandable for the user message containing information
     *         on the outcome of the import per each {@link Template}.
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
     * This method is responsible for importing a set of {@link Rule}s from a specified file or URL resource.
     *
     * @param parserType is relevant to the format that you need for conversion of the {@link Rule}s from text.
     * @param url is a specified file or URL resource.
     * @return a set of {@link Status} objects, representing understandable for the user message containing information
     *         on the outcome of the import per each {@link Rule}.
     */
    public Set<Status> importRules(String parserType, URL url) {
        if (ruleImporter != null) {
            return ruleImporter.importRules(parserType, url);
        }
        Status s = new Status(LoggerFactory.getLogger(AutomationCommands.class), 0, null);
        s.error("Pluggable Commands Service not available.", new IllegalArgumentException());
        Set<Status> set = new HashSet<Status>();
        set.add(s);
        return set;
    }

    /**
     * This method is responsible for removing a set of objects loaded from a specified file or URL resource.
     *
     * @param providerType specifies the provider responsible for removing the objects loaded from a specified file or
     *            URL resource.
     * @param url is a specified file or URL resource.
     * @return <b>true</b> if succeeds and <b>false</b> if fails.
     */
    public boolean remove(int providerType, URL url) {
        switch (providerType) {
            case AutomationCommands.MODULE_TYPE_PROVIDER:
                if (moduleTypeProvider != null) {
                    List<String> portfolio = moduleTypeProvider.providerPortfolio.remove(url);
                    if (portfolio != null && !portfolio.isEmpty()) {
                        for (String uid : portfolio) {
                            moduleTypeProvider.providedObjectsHolder.remove(uid);
                        }
                        return true;
                    }
                }
                return false;
            case AutomationCommands.TEMPLATE_PROVIDER:
                if (templateProvider != null) {
                    List<String> portfolio = templateProvider.providerPortfolio.remove(url);
                    if (portfolio != null && !portfolio.isEmpty()) {
                        for (String uid : portfolio) {
                            templateProvider.providedObjectsHolder.remove(uid);
                        }
                        return true;
                    }
                }
                return false;
        }
        return false;
    }

    /**
     * This method is responsible for execution of every particular command and to return the result of the execution.
     *
     * @param command is an identifier of the command.
     * @param parameterValues is an array of strings which are basis for initializing the options and parameters of the
     *            command.
     *            The order for their description is a random.
     * @return understandable for the user message containing information on the outcome of the command.
     */
    public String executeCommand(String command, String[] parameterValues) {
        AutomationCommand commandInst = parseCommand(command, parameterValues);
        if (commandInst != null) {
            return commandInst.execute();
        }
        return String.format("[Automation Commands : Command \"%s\"] Command not supported!", command);
    }

    /**
     * This method closes the providers and the importer and unregisters the services {@link ModuleTypeProvider} and
     * {@link TemplateProvider}.
     */
    public void stop() {
        moduleTypeProvider.close();
        templateProvider.close();
        ruleImporter.close();
        mtpReg.unregister();
        tpReg.unregister();
        moduleTypeProvider = null;
        templateProvider = null;
        ruleImporter = null;
    }

}