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
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;

/**
 * This class provides common functionality of commands:
 * <ul>
 * <p>
 * {@link AutomationCommands#IMPORT_MODULE_TYPES}
 * <p>
 * {@link AutomationCommands#IMPORT_TEMPLATES}
 * <p>
 * {@link AutomationCommands#IMPORT_RULES}
 * </ul>
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class AutomationCommandImport extends AutomationCommand {

    /**
     * This constant is used for detection of <tt>ParserType</tt> parameter. If some of the parameters of the command
     * is equal to this constant, then the <tt>ParserType</tt> parameter is present and its value is the next one.
     */
    private static final String OPTION_P = "-p";

    /**
     * This field keeps the value of the <tt>ParserType</tt> parameter and it is initialized as
     * {@link Parser#FORMAT_JSON} by default.
     */
    private String parserType = Parser.FORMAT_JSON;

    /**
     * This field keeps URL of the source of automation objects that has to be imported.
     */
    private URL url;

    /**
     * @see AutomationCommand#AutomationCommand(String, String[], int, AutomationCommandsPluggable)
     */
    public AutomationCommandImport(String command, String[] params, int adminType,
            AutomationCommandsPluggable autoCommands) {
        super(command, params, adminType, autoCommands);
    }

    /**
     * This method is responsible for execution of commands:
     * <ul>
     * <p>
     * {@link AutomationCommands#IMPORT_MODULE_TYPES}
     * <p>
     * {@link AutomationCommands#IMPORT_TEMPLATES}
     * <p>
     * {@link AutomationCommands#IMPORT_RULES}
     * </ul>
     */
    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        Set<Status> status = null;
        switch (providerType) {
            case AutomationCommands.MODULE_TYPE_PROVIDER:
                status = autoCommands.importModuleTypes(parserType, url);
                if (status == null || status.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] %s : Parser %s not available",
                            command, FAIL, parserType);
                }
                break;
            case AutomationCommands.TEMPLATE_PROVIDER:
                status = autoCommands.importTemplates(parserType, url);
                if (status == null || status.isEmpty()) {
                    return String.format("[Automation Commands : Command \"%s\"] %s : Parser %s not available",
                            command, FAIL, parserType);
                }
                break;
            case AutomationCommands.RULE_PROVIDER:
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
     * <b>url</b> which is required
     * </ul>
     * If there are redundant parameters or options or the required is missing the result will be the failure of the
     * command.
     */
    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
        String command = this.command;
        boolean getUrl = true;
        for (int i = 0; i < parameterValues.length; i++) {
            if (null == parameterValues[i]) {
                continue;
            }
            if (parameterValues[i].equals(OPTION_ST)) {
                st = true;
            } else if (parameterValues[i].equalsIgnoreCase(OPTION_P)) {
                i++;
                if (i >= parameterValues.length) {
                    return String
                            .format("[Automation Commands : Command \"%s\"] The option [%s] should be followed by value for the parser type.",
                                    command, OPTION_P);
                }
                parserType = parameterValues[i];
            } else if (parameterValues[i].charAt(0) == '-') {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        parameterValues[i]);
            } else if (getUrl) {
                url = initURL(parameterValues[i]);
                if (url != null) {
                    getUrl = false;
                }
            } else {
                return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                        parameterValues[i]);
            }
        }
        if (getUrl) {
            return String.format("[Automation Commands : Command \"%s\"] Missing source URL parameter!", command);
        }
        return SUCCESS;
    }

}
