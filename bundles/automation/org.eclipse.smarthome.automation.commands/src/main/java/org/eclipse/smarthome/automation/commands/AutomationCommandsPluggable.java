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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandsPluggable extends AutomationCommands implements ServiceTrackerCustomizer,
        ConsoleCommandExtension {

    public static final String NAME = "automation";
    public static final String DESCRIPTION = "Commands for managing Automation Rules, Templates and ModuleTypes resources.";

    private static final int MODULE_TYPE_REGISTRY = 3;
    private static final int TEMPLATE_REGISTRY = 2;
    private static final int RULE_REGISTRY = 1;

    private ServiceTracker tracker;

    static RuleRegistry ruleReg;
    static TemplateRegistry templateRegistry;
    static ModuleTypeRegistry moduleTypeRegistry;
    private ServiceRegistration commandsServiceReg;

    /**
     *
     * @param bc
     */
    public AutomationCommandsPluggable(BundleContext bc) {
        super(bc);
        try {
            Filter filter = bc.createFilter("(|(objectClass=" + RuleRegistry.class.getName() + ")(objectClass="
                    + TemplateRegistry.class.getName() + ")(objectClass=" + ModuleTypeRegistry.class.getName() + "))");
            tracker = new ServiceTracker(bc, filter, this);
            tracker.open();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        commandsServiceReg = bc.registerService(ConsoleCommandExtension.class.getName(), this, null);
    }

    /**
	 *
	 */
    @Override
    public void stop() {
        commandsServiceReg.unregister();
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
        super.stop();
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(ServiceReference reference) {
        Object service = bc.getService(reference);
        if (service instanceof TemplateRegistry) {
            this.templateRegistry = (TemplateRegistry) service;
        }
        if (service instanceof RuleRegistry) {
            this.ruleReg = (RuleRegistry) service;
        }
        if (service instanceof ModuleTypeRegistry) {
            this.moduleTypeRegistry = (ModuleTypeRegistry) service;
        }
        return service;
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    /**
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (service == templateRegistry) {
            templateRegistry = null;
        }
        if (service == ruleReg) {
            ruleReg = null;
        }
        if (service == moduleTypeRegistry) {
            moduleTypeRegistry = null;
        }
    }

    /**
     * @see org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension#execute(java.lang.String[],
     *      org.eclipse.smarthome.io.console.Console)
     */

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];// the first argument is the subcommand name

        String[] params = new String[args.length - 1];// extract the remaining arguments except the first one
        if (params.length > 0) {
            System.arraycopy(args, 1, params, 0, params.length);
        }

        String res = super.executeCommand(command, params);
        if (res == null) {
            console.println(String.format("Unsupported command %s", command));
        } else {
            console.println(res);
        }
    }

    /**
     * @see org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension#getUsages()
     */

    @Override
    public List<String> getUsages() {
        return Arrays
                .asList(new String[] {
                        buildCommandUsage(LIST_MODULE_TYPES + " [-st] <filter>",
                                "lists all Module Types. If filter is present, lists only matching Module Types"),
                        buildCommandUsage(LIST_TEMPLATES + " [-st] <filter>",
                                "lists all Templates. If filter is present, lists only matching Templates"),
                        buildCommandUsage(LIST_RULES + " [-st] <filter>",
                                "lists all Rules. If filter is present, lists only matching Rules"),
                        buildCommandUsage(REMOVE_MODULE_TYPES + " [-st] <url>",
                                "Removes the Module Types, loaded from the given url"),
                        buildCommandUsage(REMOVE_TEMPLATES + " [-st] <url>",
                                "Removes the Templates, loaded from the given url"),
                        buildCommandUsage(REMOVE_RULE + " [-st] <uid>", "Removes the rule, specified by given UID"),
                        buildCommandUsage(REMOVE_RULES + " [-st] <filter>",
                                "Removes the rules. If filter is present, removes only matching Rules"),
                        buildCommandUsage(IMPORT_MODULE_TYPES + " [-p] <parserType> [-st] <url>",
                                "Imports Module Types from given url. If parser type missing, \"json\" parser will be set as default"),
                        buildCommandUsage(IMPORT_TEMPLATES + " [-p] <parserType> [-st] <url>",
                                "Imports Templates from given url. If parser type missing, \"json\" parser will be set as default"),
                        buildCommandUsage(IMPORT_RULES + " [-p] <parserType> [-st] <url>",
                                "Imports Rules from given url. If parser type missing, \"json\" parser will be set as default"),
                        buildCommandUsage(EXPORT_MODULE_TYPES + " [-p] <parserType> [-st] <file>",
                                "Exports Module Types in a file. If parser type missing, \"json\" parser will be set as default"),
                        buildCommandUsage(EXPORT_TEMPLATES + " [-p] <parserType> [-st] <file>",
                                "Exports Templates in a file. If parser type missing, \"json\" parser will be set as default"),
                        buildCommandUsage(EXPORT_RULES + " [-p] <parserType> [-st] <file>",
                                "Exports Rules in a file. If parser type missing, \"json\" parser will be set as default") });
    }

    /**
     * @see org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension#getCommand()
     */

    @Override
    public String getCommand() {
        return NAME;
    }

    /**
     * @see org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension#getDescription()
     */

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#getRule(java.lang.String)
     */

    @Override
    public Rule getRule(String uid) {
        if (ruleReg != null) {
            return ruleReg.get(uid);
        }
        return null;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#getTemplate(java.lang.String,
     *      java.util.Locale)
     */

    @Override
    public Template getTemplate(String templateUID, Locale locale) {
        if (templateRegistry != null) {
            return templateRegistry.get(templateUID, locale);
        }
        return null;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#getTemplates(java.util.Locale)
     */

    @Override
    public Collection<Template> getTemplates(Locale locale) {
        if (templateRegistry != null) {
            return templateRegistry.getAll(locale);
        }
        return null;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#getModuleType(java.lang.String,
     *      java.util.Locale)
     */

    @Override
    public ModuleType getModuleType(String typeUID, Locale locale) {
        if (moduleTypeRegistry != null) {
            return moduleTypeRegistry.get(typeUID, locale);
        }
        return null;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#getModuleTypes(java.lang.Class,
     *      java.lang.String)
     */

    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(Class<T> clazz, Locale locale) {
        if (moduleTypeRegistry != null) {
            return moduleTypeRegistry.get(clazz, locale);
        }
        return null;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#removeRule(java.lang.String)
     */

    @Override
    public boolean removeRule(String uid) {
        if (ruleReg != null) {
            if (ruleReg.remove(uid) != null) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#removeRules(java.lang.String)
     */

    @Override
    public boolean removeRules(String ruleFilter) {
        boolean res = false;
        if (ruleReg != null) {
            ruleReg.remove(ruleFilter);
        }
        return res;
    }

    /**
     * @see com.prosyst.mbs.impl.services.automation.commands.AutomationCommands#parseCommand(java.lang.String,
     *      java.lang.String[])
     */

    @Override
    protected AutomationCommand parseCommand(String command, String[] params) {
        if (command.equalsIgnoreCase(IMPORT_MODULE_TYPES)) {
            return new AutomationCommandImport(IMPORT_MODULE_TYPES, params, MODULE_TYPE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(EXPORT_MODULE_TYPES)) {
            return new AutomationCommandExport(EXPORT_MODULE_TYPES, params, MODULE_TYPE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(LIST_MODULE_TYPES)) {
            return new AutomationCommandList(LIST_MODULE_TYPES, params, MODULE_TYPE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(IMPORT_TEMPLATES)) {
            return new AutomationCommandImport(IMPORT_TEMPLATES, params, TEMPLATE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(EXPORT_TEMPLATES)) {
            return new AutomationCommandExport(EXPORT_TEMPLATES, params, TEMPLATE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(LIST_TEMPLATES)) {
            return new AutomationCommandList(LIST_TEMPLATES, params, TEMPLATE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(IMPORT_RULES)) {
            return new AutomationCommandImport(IMPORT_RULES, params, RULE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(EXPORT_RULES)) {
            return new AutomationCommandExport(EXPORT_RULES, params, RULE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(LIST_RULES)) {
            return new AutomationCommandList(LIST_RULES, params, RULE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(REMOVE_TEMPLATES)) {
            return new AutomationCommandRemove(REMOVE_TEMPLATES, params, TEMPLATE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(REMOVE_MODULE_TYPES)) {
            return new AutomationCommandRemove(REMOVE_MODULE_TYPES, params, MODULE_TYPE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(REMOVE_RULE)) {
            return new AutomationCommandRemove(REMOVE_RULE, params, RULE_REGISTRY, this);
        }
        if (command.equalsIgnoreCase(REMOVE_RULES)) {
            return new AutomationCommandRemove(REMOVE_RULES, params, RULE_REGISTRY, this);
        }
        return null;
    }

    /**
     * Build a command usage string.
     *
     * You should always use that function to use a usage string that complies
     * to a standard format.
     *
     * @param description
     *            the description of the command
     * @return a usage string that complies to a standard format
     */
    protected String buildCommandUsage(final String description) {
        return String.format("%s - %s", getCommand(), description);
    }

    /**
     * Build a command usage string.
     *
     * You should always use that function to use a usage string that complies
     * to a standard format.
     *
     * @param syntax
     *            the syntax format
     * @param description
     *            the description of the command
     * @return a usage string that complies to a standard format
     */
    protected String buildCommandUsage(final String syntax, final String description) {
        return String.format("%s %s - %s", getCommand(), syntax, description);
    }

    @Override
    public Collection<Rule> getRules() {
        if (ruleReg != null) {
            return ruleReg.getAll();
        } else {
            return null;
        }
    }

    @Override
    public RuleStatus getRuleStatus(String ruleUID) {
        if (ruleReg != null) {
            return ruleReg.getStatus(ruleUID);
        } else {
            return null;
        }
    }

}
