/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.type.Output;

/**
 * This class is used to get value of specific output. The {@link Output} is defined by the {@link Module} containing
 * the {@link Output} and its name. The module has to implement {@link SourceModule} interface. This internal interface
 * is implemented by module implementations which have outputs ( {@link TriggerImpl} and {@link ActionImpl}).
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
class OutputRef {

    private String outputName;
    private SourceModule sourceModule;

    /**
     * Constructs pair of output name and module containing this output.
     *
     * @param outputName name of output
     * @param sm source module
     */
    public OutputRef(String outputName, SourceModule sm) {
        this.outputName = outputName;
        this.sourceModule = sm;
    }

    /**
     * Gets value of defined output.
     *
     * @return current value of the {@link Output}
     */
    public Object getValue() {
        return sourceModule.getOutputValue(outputName);
    }

    @Override
    public String toString() {
        return "OutputValue:" + outputName + "=" + getValue();
    }
}