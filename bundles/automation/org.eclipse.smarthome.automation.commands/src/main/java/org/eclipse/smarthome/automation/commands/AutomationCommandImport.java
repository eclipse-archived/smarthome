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
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.handler.parser.Status;

/**
 * @author Ana Dimova - Initial Contribution
 *
 */
public class AutomationCommandImport extends AutomationCommand {

    private static final String OPTION_P = "-p";

    private String parserType = "json"; // parser type
    private URL url; // input url

    /**
     *
     * @param command
     * @param params
     * @param adminType
     * @param autoCommands
     */
    public AutomationCommandImport(String command, String[] params, int adminType,
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
        Set<Status> status = null;
        switch (adminType) {
            case AutomationCommands.MODULE_TYPE_ADMIN:
                status = autoCommands.importModuleTypes(parserType, url);
                if (status == null || status.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] %s : Parser %s not available",
                            command, FAIL, parserType);
                }
                break;
            case AutomationCommands.TEMPLATE_ADMIN:
                status = autoCommands.importTemplates(parserType, url);
                if (status == null || status.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] %s : Parser %s not available",
                            command, FAIL, parserType);
                }
                break;
            case AutomationCommands.RULE_ADMIN:
                status = autoCommands.importRules(parserType, url);
                if (status == null || status.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] %s : Parser %s not available",
                            command, FAIL, parserType);
                }
                break;
        }
        if (status != null && !status.isEmpty()) {
            StringBuilder writer = new StringBuilder();
            for (Status s : status) {
                if (st && s.hasErrors()) {
                    Map<String, Throwable> errors = s.getErrors();
                    for (String key : errors.keySet()) {
                        Throwable t = errors.get(key);
                        writer.append(key + "\n");
                        StackTraceElement[] ste = t.getStackTrace();
                        for (int i = 0; i < ste.length; i++) {
                            writer.append(ste[i].toString() + "\n");
                        }
                    }
                } else {
                    writer.append(s.toString() + "\n");
                }
            }
            return writer.toString();
        }
        return FAIL;
    }

    /**
     *
     * @param command
     * @param param
     * @param writer
     * @return
     */
    private URL initURL(String param) {
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
        String command = this.command;
        boolean getUrl = true;
        for (int i = 0; i < params.length; i++) {
            if (null == params[i]) {
                continue;
            }
            if (params[i].equals(OPTION_ST)) {
                st = true;
            } else if (params[i].equalsIgnoreCase(OPTION_P)) {
                i++;
                if (i >= params.length) {
                    return String
                            .format("[Automation Commands : Command \"%s\"] The option [%s] should be followed by value for the parser type.",
                                    command, OPTION_P);
                }
                parserType = params[i];
            } else if (params[i].charAt(0) == '-') {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        params[i]);
            } else if (getUrl) {
                url = initURL(params[i]);
                if (url != null) {
                    getUrl = false;
                }
            } else {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        params[i]);
            }
        }
        if (getUrl) {
            return String.format("[Automation Commands : Command \"%s\"] Missing source URL parameter!", command);
        }
        return SUCCESS;
    }

}
