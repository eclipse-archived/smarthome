/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
class OutputValue {

    private String outputName;
    private SourceModule sourceModule;

    /**
     * @param outputName
     * @param ds
     */
    public OutputValue(String outputName, SourceModule ds) {
        this.outputName = outputName;
        this.sourceModule = ds;
    }

    public Object getValue() {
        return sourceModule.getOutputValue(outputName);
    }

    @Override
    public String toString() {
        return "OutputValue:" + outputName + "=" + getValue();
    }
}