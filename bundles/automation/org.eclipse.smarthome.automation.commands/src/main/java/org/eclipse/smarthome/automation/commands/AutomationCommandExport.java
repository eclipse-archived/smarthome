/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * This class provides common functionality of commands:
 * <ul>
 * <p>
 * {@link AutomationCommands#EXPORT_MODULE_TYPES}
 * <p>
 * {@link AutomationCommands#EXPORT_TEMPLATES}
 * <p>
 * {@link AutomationCommands#EXPORT_RULES}
 * </ul>
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class AutomationCommandExport extends AutomationCommand {

    private static final String OPTION_P = "-p";

    private String parserType = Parser.FORMAT_JSON; // parser type
    private File file; // output file

    private Locale locale = Locale.getDefault();

    /**
     * @see AutomationCommand#AutomationCommand(String, String[], int, AutomationCommandsPluggable)
     */
    public AutomationCommandExport(String command, String[] params, int providerType,
            AutomationCommandsPluggable autoCommands) {
        super(command, params, providerType, autoCommands);
    }

    /**
     * This method is responsible for execution of commands:
     * <ul>
     * <p>
     * {@link AutomationCommands#EXPORT_MODULE_TYPES}
     * <p>
     * {@link AutomationCommands#EXPORT_TEMPLATES}
     * <p>
     * {@link AutomationCommands#EXPORT_RULES}
     * </ul>
     */
    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        Set set = new LinkedHashSet();
        switch (providerType) {
            case AutomationCommands.MODULE_TYPE_PROVIDER:
                set.addAll(autoCommands.getModuleTypes(TriggerType.class, locale));
                set.addAll(autoCommands.getModuleTypes(CompositeTriggerType.class, locale));
                set.addAll(autoCommands.getModuleTypes(ConditionType.class, locale));
                set.addAll(autoCommands.getModuleTypes(CompositeConditionType.class, locale));
                set.addAll(autoCommands.getModuleTypes(ActionType.class, locale));
                set.addAll(autoCommands.getModuleTypes(CompositeActionType.class, locale));
                Status s = autoCommands.exportModuleTypes(parserType, set, file);
                if (set.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] There are no ModuleTypes available!",
                            command);
                }
                if (s.hasErrors()) {
                    return s.toString();
                }
                return SUCCESS;
            case AutomationCommands.TEMPLATE_PROVIDER:
                set.addAll(autoCommands.getTemplates(locale));
                s = autoCommands.exportTemplates(parserType, set, file);
                if (set.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] There are no Templates available!",
                            command);
                }
                if (s.hasErrors()) {
                    return s.toString();
                }
                return SUCCESS;
            case AutomationCommands.RULE_PROVIDER:
                set.addAll(autoCommands.getRules());
                s = autoCommands.exportRules(parserType, set, file);
                if (set.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] There are no Rules available!",
                            command);
                }
                if (s.hasErrors()) {
                    return s.toString();
                }
                return SUCCESS;
        }
        return FAIL;
    }

    /**
     * This method serves to create a {@link File} object from a string that is passed as a parameter of the command.
     * 
     * @param parameterValue is a string that is passed as parameter of the command and it supposed to be a file
     *            representation.
     * @return a {@link File} object created from the string that is passed as a parameter of the command or <b>null</b>
     *         if the parent directory could not be found or created or the string could not be parsed.
     */
    private File initFile(String parameterValue) {
        File f = new File(parameterValue);
        File parent = f.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            return null;
        }
        return f;
    }

    /**
     * This method is invoked from the constructor to parse all parameters and options of the command <b>EXPORT</b>.
     * This command has:
     * <p>
     * <b>Options:</b>
     * <ul>
     * <b>PrintStackTrace</b> which is common for all commands
     * </ul>
     * <p>
     * <b>Parameters:</b>
     * <ul>
     * <b>parserType</b> which is optional and by default its value is {@link Parser#FORMAT_JSON}.
     * <p>
     * <b>file</b> which is required
     * </ul>
     * If there are redundant parameters or options or the required is missing the result will be the failure of the
     * command.
     */
    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
        String command = this.command;
        boolean getFile = true;
        for (int i = 0; i < parameterValues.length; i++) {
            if (null == parameterValues[i]) {
                continue;
            }
            if (parameterValues[i].equals(OPTION_ST)) {
                st = true;
            } else if (parameterValues[i].equalsIgnoreCase(OPTION_P)) {
                i++;
                if (i >= parameterValues.length) {
                    return String.format(
                            "[Automation Commands : Command \"%s\"] The option [%s] should be followed by value for the parser type.",
                            command, OPTION_P);
                }
                parserType = parameterValues[i];
            } else if (parameterValues[i].charAt(0) == '-') {
                return String.format("[Automation Commands : Command \"{0}\"] Unsupported option: {1}", command,
                        parameterValues[i]);
            } else if (getFile) {
                file = initFile(parameterValues[i]);
                if (file != null) {
                    getFile = false;
                }
            } else {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        parameterValues[i]);
            }
        }
        if (getFile) {
            return String.format("[Automation Commands : Command \"%s\"] Missing destination file parameter!", command);
        }
        return SUCCESS;
    }

}
