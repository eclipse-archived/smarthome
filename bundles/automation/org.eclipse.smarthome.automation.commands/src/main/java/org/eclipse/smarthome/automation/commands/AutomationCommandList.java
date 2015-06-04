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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandList extends AutomationCommand {

    private String id; // uid of rule, template, etc., or filter, or sequence number
    private Locale locale = Locale.getDefault();

    /**
     *
     * @param command
     * @param params
     * @param adminType
     * @param autoCommands
     */
    public AutomationCommandList(String command, String[] params, int adminType,
            AutomationCommandsPluggable autoCommands) {
        super(command, params, adminType, autoCommands);
    }

    /**
     * @see org.eclipse.smarthome.automation.commands.AutomationCommand#execute()
     */
    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        if (adminType == AutomationCommands.MODULE_TYPE_ADMIN) {
            return listModuleTypes();
        }
        if (adminType == AutomationCommands.TEMPLATE_ADMIN) {
            return listTemplates();
        }
        if (adminType == AutomationCommands.RULE_ADMIN) {
            return listRules();
        }
        return FAIL;
    }

    /**
     * @see org.eclipse.smarthome.automation.commands.AutomationCommand#parseOptionsAndParameters(PrintStream, String[])
     */
    @Override
    protected String parseOptionsAndParameters(String[] params) {
        boolean getId = true;
        for (int i = 0; i < params.length; i++) {
            if (null == params[i]) {
                continue;
            }
            if (params[i].charAt(0) == '-') {
                if (params[i].equals(OPTION_ST)) {
                    st = true;
                    continue;
                }
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        params[i]);
            }
            if (getId) {
                id = params[i];
                getId = false;
            }
            if (getId)
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        params[i]);
        }
        return SUCCESS;
    }

    /**
     *
     * @return
     */
    private String listRules() {
        Collection collection = autoCommands.getRules(null);
        Hashtable<String, Rule> rules = new Hashtable<String, Rule>();
        Hashtable<String, String> listRules = null;
        if (collection != null && !collection.isEmpty()) {
            addCollection(collection, rules);
            listRules = Utils.sortList(rules);
            if (id != null) {
                collection = getRuleByFilter(listRules);
                if (collection.size() == 1) {
                    Rule r = (Rule) collection.toArray()[0];
                    if (r != null) {
                        return Printer.printRule(r);
                    } else {
                        return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
                    }
                } else if (collection.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
                } else {
                    if (!rules.isEmpty())
                        rules.clear();
                    addCollection(collection, rules);
                    listRules = Utils.filterList(rules, listRules);
                }
            }
            return Printer.print(listRules, command, id);
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Rules available!", command);
    }

    /**
     *
     * @return
     */
    private String listTemplates() {
        Collection collection = autoCommands.getTemplates(locale);
        Hashtable<String, Template> templates = new Hashtable<String, Template>();
        Hashtable<String, String> listTemplates = null;
        if (collection != null && !collection.isEmpty()) {
            addCollection(collection, templates);
            listTemplates = Utils.sortList(templates);
            if (id != null) {
                collection = getTemplateByFilter(listTemplates);
                if (collection.size() == 1) {
                    Template t = (Template) collection.toArray()[0];
                    if (t != null) {
                        return Printer.printTemplate(t);
                    } else {
                        return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
                    }
                } else if (collection.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
                } else {
                    if (!templates.isEmpty())
                        templates.clear();
                    addCollection(collection, templates);
                    listTemplates = Utils.filterList(templates, listTemplates);
                }
            }
            return Printer.print(listTemplates, command, id);
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Templates available!", command);
    }

    /**
     *
     * @return
     */
    private String listModuleTypes() {
        Collection collection = null;
        Hashtable<String, ModuleType> moduleTypes = new Hashtable<String, ModuleType>();
        Hashtable<String, String> listModuleTypes = null;
        collection = autoCommands.getModuleTypes(TriggerType.class, locale);
        addCollection(collection, moduleTypes);
        collection = autoCommands.getModuleTypes(ConditionType.class, locale);
        addCollection(collection, moduleTypes);
        collection = autoCommands.getModuleTypes(ActionType.class, locale);
        addCollection(collection, moduleTypes);
        collection = autoCommands.getModuleTypes(CompositeTriggerType.class, locale);
        addCollection(collection, moduleTypes);
        collection = autoCommands.getModuleTypes(CompositeConditionType.class, locale);
        addCollection(collection, moduleTypes);
        collection = autoCommands.getModuleTypes(CompositeActionType.class, locale);
        addCollection(collection, moduleTypes);
        listModuleTypes = Utils.sortList(moduleTypes);
        if (id != null) {
            collection = getModuleTypeByFilter(listModuleTypes);
            if (collection.size() == 1) {
                ModuleType mt = (ModuleType) collection.toArray()[0];
                if (mt != null) {
                    return Printer.printModuleType(mt);
                } else {
                    return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
                }
            } else if (collection.isEmpty()) {
                return String.format("[Automation Commands : Command \"%s\"] Nonexistent ID: %s", command, id);
            } else {
                if (!moduleTypes.isEmpty())
                    moduleTypes.clear();
                addCollection(collection, moduleTypes);
                listModuleTypes = Utils.filterList(moduleTypes, listModuleTypes);
            }
        }
        if (listModuleTypes != null && !listModuleTypes.isEmpty()) {
            return Printer.print(listModuleTypes, command, id);
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Module Types available!", command);
    }

    /**
     *
     * @param ruleRegistry
     * @param list
     * @return
     */
    private Collection<Rule> getRuleByFilter(Hashtable<String, String> list) {
        Collection<Rule> rules = new ArrayList();
        if (!list.isEmpty()) {
            Rule r = null;
            String uid = list.get(id);
            if (uid != null) {
                r = autoCommands.getRule(uid);
                if (r != null) {
                    rules.add(r);
                    return rules;
                }
            } else {
                r = autoCommands.getRule(id);
                if (r != null) {
                    rules.add(r);
                    return rules;
                } else {
                    for (String ruleUID : list.keySet()) {
                        if (ruleUID.indexOf(id) != -1) {
                            rules.add(autoCommands.getRule(ruleUID));
                        }
                    }
                    if (rules.isEmpty()) {
                        return autoCommands.getRules(id);
                    } else {
                        return rules;
                    }
                }
            }
        }
        return rules;
    }

    /**
     *
     * @param templateRegistry
     * @param list
     * @return
     */
    private Collection<Template> getTemplateByFilter(Hashtable<String, String> list) {
        Collection<Template> templates = new ArrayList();
        if (!list.isEmpty()) {
            Template t = null;
            String uid = list.get(id);
            if (uid != null) {
                t = autoCommands.getTemplate(uid, locale);
                if (t != null) {
                    templates.add(t);
                    return templates;
                }
            } else {
                t = autoCommands.getTemplate(id, locale);
                if (t != null) {
                    templates.add(t);
                    return templates;
                } else {
                    for (String templateUID : list.keySet()) {
                        if (templateUID.indexOf(id) != -1) {
                            templates.add(autoCommands.getTemplate(templateUID, locale));
                        }
                    }
                }
            }
        }
        return templates;
    }

    /**
     *
     * @param moduleRegistry
     * @param list
     * @return
     */
    private Collection<ModuleType> getModuleTypeByFilter(Hashtable<String, String> list) {
        Collection<ModuleType> moduleTypes = new ArrayList();
        if (!list.isEmpty()) {
            ModuleType mt = null;
            String uid = list.get(id);
            if (uid != null) {
                mt = autoCommands.getModuleType(uid, locale);
                if (mt != null) {
                    moduleTypes.add(mt);
                    return moduleTypes;
                }
            } else {
                mt = autoCommands.getModuleType(id, locale);
                if (mt != null) {
                    moduleTypes.add(mt);
                    return moduleTypes;
                } else {
                    for (String typeUID : list.values()) {
                        if (typeUID.indexOf(id) != -1) {
                            moduleTypes.add(autoCommands.getModuleType(typeUID, locale));
                        }
                    }
                }
            }
        }
        return moduleTypes;
    }

    private void addCollection(Collection collection, Hashtable list) {
        if (collection != null && !collection.isEmpty()) {
            Iterator i = collection.iterator();
            while (i.hasNext()) {
                Object element = i.next();
                if (element instanceof ModuleType) {
                    list.put(((ModuleType) element).getUID(), element);
                }
                if (element instanceof RuleTemplate) {
                    list.put(((RuleTemplate) element).getUID(), element);
                }
                if (element instanceof Rule) {
                    list.put(((Rule) element).getUID(), element);
                }
            }
        }
    }

}
