/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;
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
 * This class provides common functionality of commands:
 * <ul>
 * <li>{@link AutomationCommands#LIST_MODULE_TYPES}
 * <li>{@link AutomationCommands#LIST_TEMPLATES}
 * <li>{@link AutomationCommands#LIST_RULES}
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandList extends AutomationCommand {

    /**
     * This field serves to keep the UID of a {@link Rule}, {@link Template} or {@link ModuleType}, or part of it, or
     * sequence number of a {@link Rule}, {@link Template}, or {@link ModuleType} in the list.
     */
    private String id;

    /**
     * This field is used to search for rules, templates or types of modules, which have been translated to the language
     * from the locale.
     */
    private Locale locale = Locale.getDefault(); // For now is initialized with the default locale, but when the
                                                 // localization is implemented, it will be initialized with a parameter
                                                 // of the command.

    /**
     * @see AutomationCommand#AutomationCommand(String, String[], int, AutomationCommandsPluggable)
     */
    public AutomationCommandList(String command, String[] params, int adminType,
            AutomationCommandsPluggable autoCommands) {
        super(command, params, adminType, autoCommands);
    }

    /**
     * This method is responsible for execution of commands:
     * <ul>
     * <li>{@link AutomationCommands#LIST_MODULE_TYPES}
     * <li>{@link AutomationCommands#LIST_TEMPLATES}
     * <li>{@link AutomationCommands#LIST_RULES}
     * </ul>
     */
    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        if (providerType == AutomationCommands.MODULE_TYPE_PROVIDER) {
            return listModuleTypes();
        }
        if (providerType == AutomationCommands.TEMPLATE_PROVIDER) {
            return listTemplates();
        }
        if (providerType == AutomationCommands.RULE_PROVIDER) {
            return listRules();
        }
        return FAIL;
    }

    /**
     * This method is invoked from the constructor to parse all parameters and options of the command <b>LIST</b>.
     * If there are redundant parameters or options the result will be the failure of the command. This command has:
     * <ul>
     * <b>Options:</b>
     * <ul>
     * <li><b>PrintStackTrace</b> is common for all commands and its presence triggers printing of stack trace in case
     * of exception.
     * </ul>
     * </ul>
     * <ul>
     * <b>Parameters:</b>
     * <ul>
     * <li><b>id</b> is optional and its presence triggers printing of details on specified automation object.
     * </ul>
     * </ul>
     */
    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
        boolean getId = true;
        for (int i = 0; i < parameterValues.length; i++) {
            if (null == parameterValues[i]) {
                continue;
            }
            if (parameterValues[i].charAt(0) == '-') {
                if (parameterValues[i].equals(OPTION_ST)) {
                    st = true;
                    continue;
                }
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        parameterValues[i]);
            }
            if (getId) {
                id = parameterValues[i];
                getId = false;
            }
            if (getId)
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        parameterValues[i]);
        }
        return SUCCESS;
    }

    /**
     * This method is responsible for execution of command {@link AutomationCommands#LIST_RULES}.
     *
     * @return a string representing understandable for the user message containing information on the outcome of the
     *         command {@link AutomationCommands#LIST_RULES}.
     */
    private String listRules() {
        Collection<Rule> collection = autoCommands.getRules();
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
                        RuleStatus status = autoCommands.getRuleStatus(r.getUID());
                        return Printer.printRule(r, status);
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
            if (listRules != null && !listRules.isEmpty()) {
                return Printer.print(listRules);
            }
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Rules available!", command);
    }

    /**
     * This method is responsible for execution of command {@link AutomationCommands#LIST_TEMPLATES}.
     *
     * @return a string representing understandable for the user message containing information on the outcome of the
     *         command {@link AutomationCommands#LIST_TEMPLATES}.
     */
    private String listTemplates() {
        Collection<Template> collection = autoCommands.getTemplates(locale);
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
            if (listTemplates != null && !listTemplates.isEmpty()) {
                return Printer.print(listTemplates);
            }
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Templates available!", command);
    }

    /**
     * This method is responsible for execution of command {@link AutomationCommands#LIST_MODULE_TYPES}.
     *
     * @return a string representing understandable for the user message containing information on the outcome of the
     *         command {@link AutomationCommands#LIST_MODULE_TYPES}.
     */
    private String listModuleTypes() {
        Collection<? extends ModuleType> collection = null;
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
            return Printer.print(listModuleTypes);
        }
        return String.format("[Automation Commands : Command \"%s\"] There are no Module Types available!", command);
    }

    /**
     * This method reduces the list of {@link Rule}s so that their unique identifier or part of it to match the
     * {@link #id} or
     * the index in the <tt>list</tt> to match the {@link #id}.
     *
     * @param list is the list of {@link Rule}s for reducing.
     * @return a collection of {@link Rule}s that match the filter.
     */
    private Collection<Rule> getRuleByFilter(Hashtable<String, String> list) {
        Collection<Rule> rules = new ArrayList<Rule>();
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
                    for (String ruleUID : list.values()) {
                        if (ruleUID.indexOf(id) > -1) {
                            rules.add(autoCommands.getRule(ruleUID));
                        }
                    }
                }
            }
        }
        return rules;
    }

    /**
     * This method reduces the list of {@link Template}s so that their unique identifier or part of it to match the
     * {@link #id} or
     * the index in the <tt>list</tt> to match the {@link #id}.
     *
     * @param list is the list of {@link Template}s for reducing.
     * @return a collection of {@link Template}s that match the filter.
     */
    private Collection<Template> getTemplateByFilter(Hashtable<String, String> list) {
        Collection<Template> templates = new ArrayList<Template>();
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
     * This method reduces the list of {@link ModuleType}s so that their unique identifier or part of it to match the
     * {@link #id} or
     * the index in the <tt>list</tt> to match the {@link #id}.
     *
     * @param list is the list of {@link ModuleType}s for reducing.
     * @return a collection of {@link ModuleType}s that match the filter.
     */
    private Collection<ModuleType> getModuleTypeByFilter(Hashtable<String, String> list) {
        Collection<ModuleType> moduleTypes = new ArrayList<ModuleType>();
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

    /**
     * This method converts a {@link Collection} of {@link Rule}s, {@link Template}s or {@link ModuleType}s to a
     * {@link Hashtable} with keys - the UID of the object and values - the object.
     *
     * @param collection is the {@link Collection} of {@link Rule}s, {@link Template}s or {@link ModuleType}s which
     *            must be converted.
     * @param list is the {@link Hashtable} with keys - the UID of the object and values - the object, which must be
     *            filled with the objects from <tt>collection</tt>.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
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

    public String getRuleStatus(RuleStatus status) {
        if (status != null) {
            StringBuffer writer = new StringBuffer();
            writer.append(" [ ").append(status).append(" ] ");
            return writer.toString();
        }
        return null;
    }

}
