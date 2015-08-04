/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.core;

import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.TriggerHandler;

/**
 * @author Yordan Mihaylov - Initial Contribution
 */
public class TriggerImpl extends ModuleImpl<TriggerHandler>implements Trigger, SourceModule {

    private Map<String, ?> outputs;

    public TriggerImpl(String id, String typeUID, Map<String, ?> configuration) {
        super(id, typeUID, configuration);
    }

    protected TriggerImpl(TriggerImpl t) {
        super(t);
    }

    public void setOutputs(Map<String, ?> outputs) {
        this.outputs = outputs;
    }

    public Object getOutputValue(String outName) {
        return outputs != null ? outputs.get(outName) : null;
    }

}
