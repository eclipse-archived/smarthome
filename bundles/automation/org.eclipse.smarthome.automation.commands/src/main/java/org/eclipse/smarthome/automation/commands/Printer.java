/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.FilterCriteria;
import org.eclipse.smarthome.config.core.ParameterOption;

/**
 * This class provides the functionality responsible for printing the automation objects as a result of commands.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Printer {

    /**
     * This method is responsible for printing the list with indexes and UIDs of the {@link Rule}s, {@link Template}s or
     * {@link ModuleType}s.
     *
     * @param list with indexes and UIDs of the {@link Rule}s, {@link Template}s or {@link ModuleType}s.
     * @return a formated string, representing the list with indexes and UIDs of the {@link Rule}s, {@link Template}s or
     *         {@link ModuleType}s.
     */
    static String print(Hashtable<String, String> list) {
        StringBuilder writer = new StringBuilder();
        writer.append("\nID");
        int size = Integer.toString(list.size()).length() + 4;
        printChars(writer, ' ', size - 2, false);
        writer.append("UID\n");
        printChars(writer, '-', 100, true);
        for (int i = 1; i <= list.size(); i++) {
            String key = Integer.toString(i);
            writer.append(key);
            printChars(writer, ' ', size - key.length(), false);
            writer.append(list.get(key) + "\n");
        }
        printChars(writer, '-', 100, true);
        return writer.toString();
    }

    /**
     * This method is responsible for printing the {@link Rule}.
     *
     * @param rule the {@link Rule} for printing.
     * @return a formated string, representing the {@link Rule}.
     */
    static String printRule(Rule rule, RuleStatus status) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, '-', 100, true);
        writer.append(rule.getUID());
        if (status != null) {
            writer.append(" [ ").append(status).append("]");
        }
        writer.append("\n");

        printChars(writer, '-', 100, true);

        writer.append("UID");
        printChars(writer, ' ', 27, false);
        writer.append(rule.getUID() + "\n");

        writer.append("NAME");
        printChars(writer, ' ', 26, false);
        writer.append(rule.getName() + "\n");

        String description = rule.getDescription();
        if (description != null) {
            writer.append("DESCRIPTION");
            printChars(writer, ' ', 19, false);
            writer.append(description + "\n");
        }
        Set<String> tags = rule.getTags();
        if (tags != null && !tags.isEmpty()) {
            writer.append("TAGS");
            printChars(writer, ' ', 26, false);
            writer.append(makeString(tags) + "\n");
        }
        Map<String, Object> config = rule.getConfiguration();
        if (config != null && !config.isEmpty()) {
            writer.append("CONFIGURATION");
            printChars(writer, ' ', 17, false);
            String str = "                              ";
            writer.append(makeString(str, config) + "\n");
        }
        Set<ConfigDescriptionParameter> cd = rule.getConfigurationDescriptions();
        if (cd != null && !cd.isEmpty()) {
            writer.append("CONFIGURATION_DESCRIPTIONS");
            printChars(writer, ' ', 4, false);
            writer.append(printConfigurationDescription(cd) + "\n");
        }
        List<Trigger> triggers = rule.getModules(Trigger.class);
        if (triggers != null && !triggers.isEmpty()) {
            writer.append("TRIGGERS");
            printChars(writer, ' ', 22, false);
            int last = triggers.size();
            for (Trigger trigger : triggers) {
                writer.append(printModule(trigger));
                last--;
                if (last > 0)
                    printChars(writer, ' ', 30, false);
            }
        }
        List<Condition> conditions = rule.getModules(Condition.class);
        if (conditions != null && !conditions.isEmpty()) {
            writer.append("CONDITIONS");
            printChars(writer, ' ', 20, false);
            int last = conditions.size();
            for (Condition condition : conditions) {
                writer.append(printModule(condition));
                last--;
                if (last > 0)
                    printChars(writer, ' ', 30, false);
            }
        }
        List<Action> actions = rule.getModules(Action.class);
        if (actions != null && !actions.isEmpty()) {
            writer.append("ACTIONS");
            printChars(writer, ' ', 23, false);
            int last = actions.size();
            for (Action action : actions) {
                writer.append(printModule(action));
                last--;
                if (last > 0)
                    printChars(writer, ' ', 30, false);
            }
        }
        printChars(writer, '-', 100, true);
        return writer.toString();
    }

    /**
     * This method is responsible for printing the {@link Template}.
     *
     * @param template the {@link Template} for printing.
     * @return a formated string, representing the {@link Template}.
     */
    static String printTemplate(Template template) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, '-', 100, true);
        writer.append(template.getUID() + "\n");
        printChars(writer, '-', 100, true);

        writer.append("UID");
        printChars(writer, ' ', 27, false);
        writer.append(template.getUID() + "\n");

        String label = template.getLabel();
        if (label != null) {
            writer.append("LABEL");
            printChars(writer, ' ', 25, false);
            writer.append(label + "n");
        }
        String description = template.getDescription();
        if (description != null) {
            writer.append("DESCRIPTION");
            printChars(writer, ' ', 19, false);
            writer.append(description + "\n");
        }
        writer.append("VISIBILITY");
        printChars(writer, ' ', 20, false);
        writer.append(template.getVisibility() + "\n");

        Set<String> tags = template.getTags();
        if (tags != null && !tags.isEmpty()) {
            writer.append("TAGS");
            printChars(writer, ' ', 26, false);
            writer.append(makeString(tags) + "\n");
        }
        if (template instanceof RuleTemplate) {
            Set<ConfigDescriptionParameter> cd = ((RuleTemplate) template).getConfigurationDescription();
            if (cd != null && !cd.isEmpty()) {
                writer.append("CONFIGURATION_DESCRIPTIONS");
                printChars(writer, ' ', 4, false);
                writer.append(printConfigurationDescription(cd) + "\n");
            }
            List<Trigger> triggers = ((RuleTemplate) template).getModules(Trigger.class);
            if (triggers != null && !triggers.isEmpty()) {
                writer.append("TRIGGERS");
                printChars(writer, ' ', 22, false);
                int last = triggers.size();
                for (Trigger trigger : triggers) {
                    writer.append(printModule(trigger));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
            List<Condition> conditions = ((RuleTemplate) template).getModules(Condition.class);
            if (conditions != null && !conditions.isEmpty()) {
                writer.append("CONDITIONS");
                printChars(writer, ' ', 20, false);
                int last = conditions.size();
                for (Condition condition : conditions) {
                    writer.append(printModule(condition));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
            List<Action> actions = ((RuleTemplate) template).getModules(Action.class);
            if (actions != null && !actions.isEmpty()) {
                writer.append("ACTIONS");
                printChars(writer, ' ', 23, false);
                int last = actions.size();
                for (Action action : actions) {
                    writer.append(printModule(action));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
        }
        printChars(writer, '-', 100, true);
        return writer.toString();
    }

    /**
     * This method is responsible for printing the {@link ModuleType}.
     *
     * @param moduleType the {@link ModuleType} for printing.
     * @return a formated string, representing the {@link ModuleType}.
     */
    static String printModuleType(ModuleType moduleType) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, '-', 100, true);
        writer.append(moduleType.getUID() + "\n");
        printChars(writer, '-', 100, true);

        writer.append("UID");
        printChars(writer, ' ', 27, false);
        writer.append(moduleType.getUID() + "\n");

        String label = moduleType.getLabel();
        if (label != null) {
            writer.append("LABEL");
            printChars(writer, ' ', 25, false);
            writer.append(label + "\n");
        }
        String description = moduleType.getDescription();
        if (description != null) {
            writer.append("DESCRIPTION");
            printChars(writer, ' ', 19, false);
            writer.append(description + "\n");
        }
        writer.append("VISIBILITY");
        printChars(writer, ' ', 20, false);
        writer.append(moduleType.getVisibility() + "\n");

        Set<String> tags = moduleType.getTags();
        if (tags != null && !tags.isEmpty()) {
            writer.append("TAGS");
            printChars(writer, ' ', 26, false);
            writer.append(makeString(tags) + "\n");
        }
        Set<ConfigDescriptionParameter> cd = moduleType.getConfigurationDescription();
        if (cd != null && !cd.isEmpty()) {
            writer.append("CONFIGURATION_DESCRIPTIONS");
            printChars(writer, ' ', 4, false);
            writer.append(printConfigurationDescription(cd) + "\n");
        }
        if (moduleType instanceof TriggerType) {
            Set<Output> outputs = ((TriggerType) moduleType).getOutputs();
            if (outputs != null && !outputs.isEmpty()) {
                writer.append("OUTPUTS");
                printChars(writer, ' ', 23, false);
                writer.append(makeString(outputs) + "\n");
            }
        }
        if (moduleType instanceof ConditionType) {
            Set<Input> inputs = ((ConditionType) moduleType).getInputs();
            if (inputs != null && !inputs.isEmpty()) {
                writer.append("INPUTS");
                printChars(writer, ' ', 24, false);
                writer.append(makeString(inputs) + "\n");
            }
        }
        if (moduleType instanceof ActionType) {
            Set<Input> inputs = ((ActionType) moduleType).getInputs();
            if (inputs != null && !inputs.isEmpty()) {
                writer.append("INPUTS");
                printChars(writer, ' ', 24, false);
                writer.append(makeString(inputs) + "\n");
            }
            Set<Output> outputs = ((ActionType) moduleType).getOutputs();
            if (outputs != null && !outputs.isEmpty()) {
                writer.append("OUTPUTS");
                printChars(writer, ' ', 23, false);
                writer.append(makeString(outputs) + "\n");
            }
        }
        if (moduleType instanceof CompositeTriggerType) {
            List<Trigger> triggers = ((CompositeTriggerType) moduleType).getModules();
            if (triggers != null && !triggers.isEmpty()) {
                writer.append("TRIGGERS");
                printChars(writer, ' ', 22, false);
                int last = triggers.size();
                for (Trigger trigger : triggers) {
                    writer.append(printModule(trigger));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
        }
        if (moduleType instanceof CompositeConditionType) {
            List<Condition> conditions = ((CompositeConditionType) moduleType).getModules();
            if (conditions != null && !conditions.isEmpty()) {
                writer.append("CONDITIONS");
                printChars(writer, ' ', 20, false);
                int last = conditions.size();
                for (Condition condition : conditions) {
                    writer.append(printModule(condition));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
        }
        if (moduleType instanceof CompositeActionType) {
            List<Action> actions = ((CompositeActionType) moduleType).getModules();
            if (actions != null && !actions.isEmpty()) {
                writer.append("ACTIONS");
                printChars(writer, ' ', 23, false);
                int last = actions.size();
                for (Action action : actions) {
                    writer.append(printModule(action));
                    last--;
                    if (last > 0)
                        printChars(writer, ' ', 30, false);
                }
            }
        }
        printChars(writer, '-', 100, true);
        return writer.toString();
    }

    /**
     * This method is responsible for printing the {@link Module}.
     *
     * @param module the {@link Module} for printing.
     * @return a formated string, representing the {@link Module}.
     */
    private static String printModule(Module module) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, '-', 70, true);
        printChars(writer, ' ', 30, false);
        writer.append(module.getId() + "\n");

        printChars(writer, ' ', 30, false);
        printChars(writer, '-', 70, true);

        printChars(writer, ' ', 30, false);
        writer.append("ID");
        printChars(writer, ' ', 28, false);
        writer.append(module.getId() + "\n");

        printChars(writer, ' ', 30, false);
        writer.append("TYPE_UID");
        printChars(writer, ' ', 22, false);
        writer.append(module.getTypeUID() + "\n");

        String label = module.getLabel();
        if (label != null) {
            printChars(writer, ' ', 30, false);
            writer.append("LABEL");
            printChars(writer, ' ', 22, false);
            writer.append(label + "\n");
        }
        String description = module.getDescription();
        if (description != null) {
            printChars(writer, ' ', 30, false);
            writer.append("DESCRIPTION" + "\n");
            printChars(writer, ' ', 16, false);
            writer.append(description + "\n");
        }
        Map<String, Object> config = module.getConfiguration();
        if (config != null && !config.isEmpty()) {
            printChars(writer, ' ', 30, false);
            writer.append("CONFIGURATION");
            printChars(writer, ' ', 17, false);
            String str = "                                                            ";
            writer.append(makeString(str, config));
        }
        Set<Connection> connections = null;
        if (module instanceof Condition) {
            connections = ((Condition) module).getConnections();
        }
        if (module instanceof Action) {
            connections = ((Action) module).getConnections();
        }
        if (connections != null && !connections.isEmpty()) {
            printChars(writer, ' ', 30, false);
            writer.append("CONNECTIONS");
            printChars(writer, ' ', 19, false);
            writer.append(makeString(connections) + "\n");
        }
        return writer.toString();
    }

    /**
     * This method is responsible for printing the set of {@link ConfigDescriptionParameter}s.
     *
     * @param configDescriptions set of {@link ConfigDescriptionParameter}s for printing.
     * @return a formated string, representing the set of {@link ConfigDescriptionParameter}s.
     */
    private static String printConfigurationDescription(Set<ConfigDescriptionParameter> configDescriptions) {
        StringBuilder writer = new StringBuilder();
        Iterator<ConfigDescriptionParameter> i = configDescriptions.iterator();
        ConfigDescriptionParameter parameter = i.next();
        writer.append(parameter.getName() + "\n");
        writer.append(printParameter(parameter));
        while (i.hasNext()) {
            parameter = i.next();
            printChars(writer, ' ', 30, false);
            writer.append(parameter.getName() + "\n");
            writer.append(printParameter(parameter));
        }
        return writer.toString();
    }

    /**
     * This method is responsible for printing the {@link ConfigDescriptionParameter}.
     *
     * @param parameter the {@link ConfigDescriptionParameter} for printing.
     * @return a formated string, representing the {@link ConfigDescriptionParameter}.
     */
    private static String printParameter(ConfigDescriptionParameter parameter) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, ' ', 34, false);
        writer.append("TYPE = ");
        writer.append(parameter.getType() + "\n");
        String label = parameter.getLabel();
        if (label != null) {
            printChars(writer, ' ', 34, false);
            writer.append("LABEL = ");
            writer.append(label + "\n");
        }
        String description = parameter.getDescription();
        if (description != null) {
            printChars(writer, ' ', 34, false);
            writer.append("DESCRIPTION = ");
            writer.append(description + "\n");
        }
        String defaultVal = parameter.getDefault();
        if (defaultVal != null) {
            printChars(writer, ' ', 34, false);
            writer.append("DEFAULT = ");
            writer.append(defaultVal + "\n");
        }
        String context = parameter.getContext();
        if (context != null) {
            printChars(writer, ' ', 34, false);
            writer.append("CONTEXT = ");
            writer.append(context + "\n");
        }
        String pattern = parameter.getPattern();
        if (pattern != null) {
            printChars(writer, ' ', 34, false);
            writer.append("PATTERN = ");
            writer.append(pattern + "\n");
        }
        BigDecimal stepSize = parameter.getStepSize();
        if (stepSize != null) {
            printChars(writer, ' ', 34, false);
            writer.append("STEP_SIZE = ");
            writer.append(stepSize + "\n");
        }
        BigDecimal min = parameter.getMinimum();
        if (min != null) {
            printChars(writer, ' ', 34, false);
            writer.append("MIN = ");
            writer.append(min + "\n");
        }
        BigDecimal max = parameter.getMaximum();
        if (max != null) {
            printChars(writer, ' ', 34, false);
            writer.append("MAX = ");
            writer.append(max + "\n");
        }
        List<ParameterOption> options = parameter.getOptions();
        if (options != null && !options.isEmpty()) {
            printChars(writer, ' ', 34, false);
            writer.append("OPTIONS = ");
            writer.append(makeString(options) + "\n");
        }
        List<FilterCriteria> filter = parameter.getFilterCriteria();
        if (filter != null && !filter.isEmpty()) {
            printChars(writer, ' ', 34, false);
            writer.append("FILTER_CRITERIA");
            writer.append(makeString(filter) + "\n");
        }
        return writer.toString();
    }

    /**
     * This method is responsible for printing the list of {@link FilterCriteria} or {@link ParameterOption}s.
     *
     * @param list is the list of {@link FilterCriteria} or {@link ParameterOption}s for printing.
     * @return a formated string, representing the list of {@link FilterCriteria} or {@link ParameterOption}s.
     */
    private static String makeString(List<?> list) {
        int index = list.size();
        String res = "[\n";
        for (Object element : list) {
            index--;
            String string = "                                      ";
            if (element instanceof FilterCriteria) {
                string = string + "name=\"" + ((FilterCriteria) element).getName() + "\", value=\""
                        + ((FilterCriteria) element).getValue() + "\"";
            } else if (element instanceof ParameterOption) {
                string = string + "value=\"" + ((ParameterOption) element).getValue() + "\", label=\""
                        + ((ParameterOption) element).getLabel() + "\"";
            }
            if (index > 0) {
                res = res + string + ",\n";
            } else {
                res = res + string + "\n";
            }
        }
        return res = res + "                                  ]";
    }

    /**
     * This method is responsible for printing the set of {@link Input}s or {@link Output}s or {@link Connection}s.
     *
     * @param set is the set of {@link Input}s or {@link Output}s or {@link Connection}s for printing.
     * @return a formated string, representing the set of {@link Input}s or {@link Output}s or {@link Connection}s.
     */
    private static String makeString(Set<?> set) {
        if (set == null || set.size() == 0) {
            return "[ ]";
        }
        String str = "                              ";
        String res = "[";
        Iterator<?> i = set.iterator();
        while (i.hasNext()) {
            Object element = i.next();
            if (element instanceof String) {
                if (i.hasNext()) {
                    res = res + element + ", ";
                } else {
                    res = res + element + "]";
                }
            } else if (element instanceof Input) {
                res = ((Input) element).getName();
                res = res + "\n    " + str + "TYPE = " + ((Input) element).getType();
                res = res + "\n    " + str + "LABEL = " + ((Input) element).getLabel();
                res = res + "\n    " + str + "DESCRIPTION = " + ((Input) element).getDescription();
                res = res + "\n    " + str + "DEFAULT = " + ((Input) element).getDefaultValue();
                res = res + "\n    " + str + "TAGS = " + makeString(((Input) element).getTags());
            } else if (element instanceof Output) {
                res = ((Output) element).getName();
                res = res + "\n    " + str + "TYPE = " + ((Output) element).getType();
                res = res + "\n    " + str + "LABEL = " + ((Output) element).getLabel();
                res = res + "\n    " + str + "DESCRIPTION = " + ((Output) element).getDescription();
                res = res + "\n    " + str + "DEFAULT = " + ((Output) element).getDefaultValue();
                res = res + "\n    " + str + "REFERENCE = " + ((Output) element).getReference();
                res = res + "\n    " + str + "TAGS = " + makeString(((Output) element).getTags());
            } else if (element instanceof Connection) {
                if (i.hasNext()) {
                    res = "\n" + str + str + ((Connection) element).getInputName() + " = "
                            + ((Connection) element).getOuputModuleId() + "." + ((Connection) element).getOutputName();
                } else {
                    res = ((Connection) element).getInputName() + " = " + ((Connection) element).getOuputModuleId()
                            + "." + ((Connection) element).getOutputName();
                }
            }
        }
        return res;
    }

    /**
     * This method is responsible for printing the configuration map.
     *
     * @param str is a prefix per each member of the map.
     * @param config is the configuration map for printing.
     * @return a formated string, representing the configuration map.
     */
    private static String makeString(String str, Map<String, Object> config) {
        int index = config.size();
        String res = "";
        for (String key : config.keySet()) {
            if (index == config.size()) {
                res = key + " = " + config.get(key) + "\n";
            } else {
                res = res + str + key + " = " + config.get(key) + "\n";
            }
            index--;
        }
        return res;
    }

    static String printRuleStatus(String ruleUID, RuleStatus status) {
        StringBuilder writer = new StringBuilder();
        printChars(writer, '-', 100, true);
        writer.append(ruleUID);
        if (status != null) {
            writer.append(" [ ").append(status.toString()).append("]");
        }
        writer.append("\n");
        printChars(writer, '-', 100, true);
        return writer.toString();
    }

    /**
     * This method is responsible for the printing a symbol - <tt>ch</tt> as many times as specified by the parameter of
     * the method
     * - <tt>count</tt> and skip a line or not depending on other parameter of the method - a <tt>nl</tt>.
     *
     * @param sb is a {@link StringBuffer} for appending the specified symbol.
     * @param ch the specified symbol.
     * @param count specifies how many times to append the specified symbol.
     * @param nl specifies to skip a line or not.
     */
    private static void printChars(StringBuilder sb, char ch, int count, boolean nl) {
        if (count < 1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        if (nl) {
            sb.append("\n");
        }
    }

}
