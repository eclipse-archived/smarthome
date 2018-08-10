/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.core.internal;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.core.util.ReferenceResolver;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This class defines connection between {@link Input} of the current {@link Module} and {@link Output} of the external
 * one. The current module is the module containing {@link Connection} instance and the external one is the module where
 * the current is connected to. <br>
 * The input of the current module is defined by name of the {@link Input}. The {@link Output} of the external module is
 * defined by id of the module and name of the output.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class Connection {

    public static final String REF_IDENTIFIER = "$";

    private final String ouputModuleId;
    private final String outputName;
    private final String inputName;
    private final String[] referenceTokens;

    /**
     * This constructor is responsible for creation of connections between modules in the rule.
     *
     * @param inputName     is an unique name of the {@code Input} in scope of the {@link Module}.
     * @param ouputModuleId is an unique id of the {@code Module} in scope of the {@link Rule}.
     * @param outputName    is an unique name of the {@code Output} in scope of the {@link Module}.
     */
    public Connection(String inputName, String ouputModuleId, String outputName) {
        validate("inputName", inputName);
        validate("outputName", outputName);
        int beginIndex = outputName.indexOf('.');
        int squareBracketIndex = outputName.indexOf('[');
        if (squareBracketIndex != -1 && squareBracketIndex < beginIndex) {
            beginIndex = squareBracketIndex;
        }
        if (beginIndex == -1) {
            beginIndex = squareBracketIndex;
        }
        // We have only output name
        if (beginIndex == -1) {
            this.outputName = outputName;
            referenceTokens = null;
        } else {
            // Set the name of the output.
            this.outputName = outputName.substring(0, beginIndex);
            // Substring to get the reference
            if (outputName.charAt(beginIndex) == '[') {
                referenceTokens = ReferenceResolver.splitReferenceToTokens(outputName.substring(beginIndex));
            } else {
                referenceTokens = ReferenceResolver.splitReferenceToTokens(outputName.substring(beginIndex + 1));
            }
        }
        this.inputName = inputName;
        this.ouputModuleId = ouputModuleId;
    }

    /**
     * Gets the identifier of external {@link Module} of this connection.
     *
     * @return id of external {@link Module}
     */
    public String getOuputModuleId() {
        return ouputModuleId;
    }

    /**
     * Gets the output name of external {@link Module} of this connection.
     *
     * @return name of {@link Output} of external {@link Module}.
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Gets input name of current {@link Module} of this connection.
     *
     * @return name {@link Input} of the current {@link Module}
     */
    public String getInputName() {
        return inputName;
    }

    /**
     * Gets the reference tokens of this connection.
     *
     * @return the reference tokens.
     */
    public String[] getReferenceTokens() {
        return referenceTokens;
    }

    /**
     * Compares two connection objects. Two objects are equal if they own equal {@code inputName}.
     *
     * @return {@code true} when own equal {@code inputName} and {@code false} in the opposite.
     */
    @Override
    public boolean equals(Object obj) {
        return (inputName != null && obj instanceof Connection) && inputName.equals(((Connection) obj).getInputName());
    };

    /**
     * Returns the hash code of this object depends on the hash code of the {@code inputName} that it owns.
     */
    @Override
    public int hashCode() {
        return inputName.hashCode();
    };

    /**
     * Validates the connection.
     *
     * @param field serves to construct an understandable message that indicates what property of the connection is not
     *              correct.
     * @param id    is the value of the specified property. It can't be empty string.
     */
    private void validate(String field, String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("Invalid identifier for " + field);
        }
    }

    @Override
    public String toString() {
        return "Connection " + ouputModuleId + "." + outputName + "->" + inputName;
    }
}
