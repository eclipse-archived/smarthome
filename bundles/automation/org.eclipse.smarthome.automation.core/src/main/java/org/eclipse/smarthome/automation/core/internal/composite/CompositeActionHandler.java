/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.Output;

public class CompositeActionHandler extends AbstractCompositeModuleHandler<Action, CompositeActionType, ActionHandler>
        implements ActionHandler {

    public final static String REFERENCE = "reference";

    private Map<String, String> compositeOutputs;

    public CompositeActionHandler(Action action, CompositeActionType mt,
            LinkedHashMap<Action, ActionHandler> mapModuleToHandler, String ruleUID) {
        super(action, mt, mapModuleToHandler);
        compositeOutputs = getCompositeOutputMap(moduleType.getOutputs());
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> context) {
        Map<String, Object> internalContext = new HashMap<>(context);
        Map<String, Object> result = new HashMap<>();
        List<Action> children = moduleType.getModules();
        for (Action child : children) {
            Map<String, ?> compositeContext = getCompositeContext(internalContext, module);
            Map<String, Object> originalConfig = new HashMap<>(child.getConfiguration());
            updateChildConfig(child, compositeContext);
            ActionHandler childHandler = moduleHandlerMap.get(child);
            Map<String, Object> childResults = childHandler.execute(compositeContext);
            child.setConfiguration(originalConfig); // restore original config (restore links in config)
            if (childResults != null) {
                for (Entry<String, Object> childResult : childResults.entrySet()) {
                    String childOuputName = childResult.getKey();
                    String childOuputRef = child.getId() + "." + childOuputName;
                    String compositeOutputName = compositeOutputs.get(childOuputRef);
                    if (compositeOutputName != null) {
                        result.put(compositeOutputName, childResult.getValue());
                    }
                }
            }

        }
        return result.size() > 0 ? result : null;
    }

    protected Map<String, String> getCompositeOutputMap(Set<Output> outputs) {
        Map<String, String> result = new LinkedHashMap<>(11);
        if (outputs != null) {
            for (Output output : outputs) {
                String refs = output.getReference();
                if (refs != null) {
                    String ref;
                    StringTokenizer st = new StringTokenizer(refs, ",");
                    while (st.hasMoreTokens()) {
                        ref = st.nextToken().trim();
                        result.put(ref, output.getName());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
