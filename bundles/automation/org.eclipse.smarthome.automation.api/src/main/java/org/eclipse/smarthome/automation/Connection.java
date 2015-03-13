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

package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This class defines connection between {@link Input} of the current {@link Module} and {@link Output} of the external
 * one. The current module is
 * the module containing {@link Connection} instance and the external one is the
 * module where the current is connected to. <br>
 * The input of the current module is defined by name of the {@link Input}. The {@link Output} of the external module is
 * defined by id of the module and name
 * of the output.
 *
 * @author Yordan Mihaylov
 */
public class Connection {

    private String ouputModuleId;
    private String outputName;
    private String inputName;

    public Connection(String inputName, String ouputModuleId, String outputName) {
        validate("inputName", inputName);
        validate("ouputModuleId", ouputModuleId);
        validate("outputName", outputName);
        this.inputName = inputName;
        this.ouputModuleId = ouputModuleId;
        this.outputName = outputName;
    }

    /**
     * This method is used to get id of external {@link Module} of this
     * connection.
     *
     * @return id of external {@link Module}
     */
    public String getOuputModuleId() {
        return ouputModuleId;
    }

    /**
     * This method is used to get output name of external {@link Module} of this
     * connection.
     *
     * @return name of {@link Output} of external {@link Module}
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * This method is used to get input name of current {@link Module} of this
     * connection.
     *
     * @return name {@link Input} of the current {@link Module}
     */
    public String getInputName() {
        return inputName;
    }

    @Override
    public boolean equals(Object obj) {
        return (inputName != null && obj instanceof Connection) && inputName.equals(((Connection) obj).getInputName());

    };

    @Override
    public int hashCode() {
        return inputName.hashCode();
    };

    /**
     * @param inputName2
     */
    private void validate(String field, String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("Invalid identifier for " + field);
        }
    }
}
