/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class provides common functionality of commands:
 * <ul>
 * <p>
 * {@link AutomationCommands#REMOVE_MODULE_TYPES}
 * <p>
 * {@link AutomationCommands#REMOVE_TEMPLATES}
 * <p>
 * {@link AutomationCommands#REMOVE_RULES}
 * <p>
 * {@link AutomationCommands#REMOVE_RULE}
 * </ul>
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class AutomationCommandRemove extends AutomationCommand {

    /**
     * This field keeps the UID of the {@link Rule} if command is {@link AutomationCommands#REMOVE_RULE}
     */
    private String id;

    /**
     * This field keeps URL of the source of automation objects that has to be removed.
     */
    private URL url;

    /**
     * @see AutomationCommand#AutomationCommand(String, String[], int, AutomationCommandsPluggable)
     */
    public AutomationCommandRemove(String command, String[] params, int providerType,
            AutomationCommandsPluggable autoCommands) {
        super(command, params, providerType, autoCommands);
    }

    /**
     * This method is responsible for execution of commands:
     * <ul>
     * <p>
     * {@link AutomationCommands#REMOVE_MODULE_TYPES}
     * <p>
     * {@link AutomationCommands#REMOVE_TEMPLATES}
     * <p>
     * {@link AutomationCommands#REMOVE_RULES}
     * <p>
     * {@link AutomationCommands#REMOVE_RULE}
     * </ul>
     */
    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        switch (providerType) {
            case AutomationCommands.MODULE_TYPE_PROVIDER:
                if (autoCommands.remove(AutomationCommands.MODULE_TYPE_PROVIDER, url)) {
                    return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                }
                return String.format("[Automation Commands : Command \"%s\"] %s! ModuleTypeProvider not available!",
                        command, FAIL);
            case AutomationCommands.TEMPLATE_PROVIDER:
                if (autoCommands.remove(AutomationCommands.TEMPLATE_PROVIDER, url)) {
                    return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                }
                return String.format("[Automation Commands : Command \"%s\"] %s! TemplateProvider not available!",
                        command, FAIL);
            case AutomationCommands.RULE_PROVIDER:
                if (command == AutomationCommands.REMOVE_RULE) {
                    if (autoCommands.removeRule(id)) {
                        return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                    }
                } else {
                    if (autoCommands.removeRules(id)) {
                        return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                    }
                }
                return String.format("[Automation Commands : Command \"%s\"] %s! RuleRegistry not available!", command,
                        FAIL);
        }
        return FAIL;
    }

    /**
     * This method serves to create an {@link URL} object or {@link File} object from a string that is passed as
     * a parameter of the command. From the {@link File} object the URL is constructed.
     * 
     * @param parameterValue is a string that is passed as parameter of the command and it supposed to be an URL
     *            representation.
     * @return an {@link URL} object created from the string that is passed as parameter of the command or <b>null</b>
     *         if either no legal protocol could be found in the specified string or the string could not be parsed.
     */
    private URL initURL(String parameterValue) {
        try {
            return new URL(parameterValue);
        } catch (MalformedURLException mue) {
            File f = new File(parameterValue);
            if (f.isFile()) {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException e) {
                }
            }
        }
        return null;
    }

    /**
     * This method is invoked from the constructor to parse all parameters and options of the command <b>REMOVE</b>.
     * This command has:
     * <p>
     * <b>Options:</b>
     * <ul>
     * <b>PrintStackTrace</b> which is common for all commands
     * </ul>
     * <p>
     * <b>Parameters:</b>
     * <ul>
     * <b>id</b> which is required for {@link AutomationCommands#REMOVE_RULE} command
     * <p>
     * <b>url</b> which is required for all <b>REMOVE</b> commands, except {@link AutomationCommands#REMOVE_RULE}. If it
     * is present for {@link AutomationCommands#REMOVE_RULE} it will be treated as redundant.
     * </ul>
     * If there are redundant parameters or options or the required are missing the result will be the failure of the
     * command.
     */
    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
        boolean getUrl = true;
        boolean getId = true;
        if (providerType == AutomationCommands.RULE_PROVIDER) {
            getUrl = false;
        } else {
            getId = false;
        }
        for (int i = 0; i < parameterValues.length; i++) {
            if (null == parameterValues[i]) {
                continue;
            }
            if (parameterValues[i].equals(OPTION_ST)) {
                st = true;
            } else if (parameterValues[i].charAt(0) == '-') {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        parameterValues[i]);
            } else if (getUrl) {
                url = initURL(parameterValues[i]);
                if (url != null) {
                    getUrl = false;
                }
            } else if (getId) {
                id = parameterValues[i];
                if (id != null) {
                    getId = false;
                }
            } else {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        parameterValues[i]);
            }
        }
        if (getUrl) {
            return String.format("[Automation Commands : Command \"%s\"] Missing source URL parameter!", command);
        }
        if (getId) {
            return String.format("[Automation Commands : Command \"%s\"] Missing UID parameter!", command);
        }
        return SUCCESS;
    }

}
