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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandRemove extends AutomationCommand {

    private String id = null; // uid of rule, template, etc., or filter, or sequence number
    private URL url = null; // input url

    /**
     *
     * @param command
     * @param params
     * @param adminType
     * @param options
     */
    public AutomationCommandRemove(String command, String[] params, int adminType,
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
        switch (adminType) {
            case AutomationCommands.MODULE_TYPE_ADMIN:
                if (autoCommands.remove(AutomationCommands.MODULE_TYPE_ADMIN, url)) {
                    return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                }
                return String.format("[Automation Commands : Command \"%s\"] %s! ModuleTypeProvider not available!",
                        command, FAIL);
            case AutomationCommands.TEMPLATE_ADMIN:
                if (autoCommands.remove(AutomationCommands.TEMPLATE_ADMIN, url)) {
                    return String.format("[Automation Commands : Command \"%s\"] %s", command, SUCCESS);
                }
                return String.format("[Automation Commands : Command \"%s\"] %s! TemplateProvider not available!",
                        command, FAIL);
            case AutomationCommands.RULE_ADMIN:
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
     *
     * @param command
     * @param param
     * @return
     */
    private URL initURL(String command, String param) {
        try {
            return new URL(param);
        } catch (MalformedURLException mue) {
            File f = new File(param);
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
     * @see org.eclipse.smarthome.automation.commands.AutomationCommand#parseOptionsAndParameters(PrintStream, String[])
     */
    @Override
    protected String parseOptionsAndParameters(String[] params) {
        boolean getUrl = true;
        boolean getId = true;
        if (adminType == AutomationCommands.RULE_ADMIN) {
            getUrl = false;
        } else {
            getId = false;
        }
        for (int i = 0; i < params.length; i++) {
            if (null == params[i]) {
                continue;
            }
            if (params[i].equals(OPTION_ST)) {
                st = true;
            } else if (params[i].charAt(0) == '-') {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        params[i]);
            } else if (getUrl) {
                url = initURL(command, params[i]);
                if (url != null) {
                    getUrl = false;
                }
            } else if (getId) {
                id = params[i];
                if (id != null) {
                    getId = false;
                }
            } else {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        params[i]);
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
