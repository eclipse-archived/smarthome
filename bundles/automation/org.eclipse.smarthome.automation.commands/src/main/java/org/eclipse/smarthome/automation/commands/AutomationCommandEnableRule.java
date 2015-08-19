/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.commands;

import org.eclipse.smarthome.automation.RuleStatus;

/**
 * This class provides functionality of command {@link AutomationCommands#ENABLE_RULE}.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandEnableRule extends AutomationCommand {

    /**
     * This field keeps the value of "enable" parameter of the command.
     */
    private boolean enable;

    /**
     * This field indicates the presence of the "enable" parameter of the command.
     */
    private boolean hasEnable;

    /**
     * This field keeps the specified rule UID.
     */
    private String uid;

    public AutomationCommandEnableRule(String command, String[] parameterValues, int providerType,
            AutomationCommandsPluggable autoCommands) {
        super(command, parameterValues, providerType, autoCommands);
    }

    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        if (hasEnable) {
            autoCommands.setEnabled(uid, enable);
            return SUCCESS;
        } else {
            RuleStatus status = autoCommands.getRuleStatus(uid);
            if (status != null)
                return Printer.printRuleStatus(uid, status);
        }
        return FAIL;
    }

    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
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
            if (uid == null) {
                uid = parameterValues[i];
                continue;
                // Rule rule = autoCommands.getRule(parameterValues[i]);
                // if (rule != null) {
                // uid = rule.getUID();
                // continue;
                // }
            }
            getEnable(parameterValues[i]);
            if (hasEnable)
                continue;
            if (uid == null)
                return String.format("[Automation Commands : Command \"%s\"] Missing required parameter: Rule UID",
                        command);
            return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                    parameterValues[i]);
        }
        return SUCCESS;
    }

    /**
     * Utility method for parsing the command parameter - "enable".
     *
     * @param parameterValue is the value entered from command line.
     */
    private void getEnable(String parameterValue) {
        if (parameterValue.equals("true")) {
            enable = true;
            hasEnable = true;
        } else if (parameterValue.equals("false")) {
            enable = false;
            hasEnable = true;
        } else {
            hasEnable = false;
        }
    }

}
