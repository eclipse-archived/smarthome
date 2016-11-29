/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Plamen Peev - Bosch Software Innovations GmbH - Please refer to git log
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.sample.moduletype.commands;

/**
 * This class is base for the commands in this demo automation commands. It defines common functionality for the
 * commands.
 */
public abstract class DemoCommand {

    /**
     * This constant is used as a part of the string representing understandable for the user message containing
     * information for the success of the command.
     */
    protected static final String SUCCESS = "SUCCESS";

    /**
     * This constant is used as a part of the string representing understandable for the user message containing
     * information for the failure of the command.
     */
    protected static final String FAIL = "FAIL";

    /**
     * This field is used to contain the parsing result of the command. If something with the parsing of the command is
     * wrong this {@link String} is returned as a result of the execution.
     */
    protected String parsingResult;

    /**
     * This constructor is responsible for initialising the common properties for each demo command.
     *
     * @param parameterValues is an array of strings which are basis for initialising the parameters of the command.
     */
    public DemoCommand(String[] parameterValues) {
        parseOptionsAndParameters(parameterValues);
    }

    /**
     * This method is common for all demo commands and it is responsible for execution of every particular
     * command.
     *
     * @return a {@link String} representing understandable for the user message containing information on the outcome
     *         of the
     *         command.
     */
    public abstract String execute();

    /**
     * This method is used to determine the options and the parameters for every particular command. If there are
     * redundant options and parameters or the required are missing the execution of the command will be ended and the
     * parsing
     * result will be returned as a result of the command.
     *
     * @param parameterValues is an array of strings which are basis for initializing the options and parameters of the
     *            command. The order for their description is a random.
     */
    protected abstract void parseOptionsAndParameters(String[] parameterValues);

}
