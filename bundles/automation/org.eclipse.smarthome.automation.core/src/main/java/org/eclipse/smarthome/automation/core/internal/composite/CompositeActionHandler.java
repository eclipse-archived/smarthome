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

/**
 * This class is a handler implementation for {@link CompositeActionType}. The action of type
 * {@link CompositeActionType} has to execute handlers of all child actions. The handler has to return outputs of the
 * action, base on the outputs of the child actions, into rule context. The outputs of the child actions are not
 * visible out of the context of the action.
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public class CompositeActionHandler extends AbstractCompositeModuleHandler<Action, CompositeActionType, ActionHandler>
        implements ActionHandler {

    public final static String REFERENCE = "reference";

    private Map<String, String> compositeOutputs;

    /**
     * Create a system handler for modules of {@link CompositeActionType} type.
     *
     * @param action parent action module instance. The action which has {@link CompositeActionType} type.
     * @param mt {@link CompositeActionType} instance of the parent module
     * @param mapModuleToHandler map of pairs child action module to its action handler
     * @param ruleUID UID of rule where the parent action is part of.
     */
    public CompositeActionHandler(Action action, CompositeActionType mt,
            LinkedHashMap<Action, ActionHandler> mapModuleToHandler, String ruleUID) {
        super(action, mt, mapModuleToHandler);
        compositeOutputs = getCompositeOutputMap(moduleType.getOutputs());
    }

    /**
     * The method calls handlers of child action, collect their outputs and sets the output of the parent action.
     *
     * @see org.eclipse.smarthome.automation.handler.ActionHandler#execute(java.util.Map)
     */
    @Override
    public Map<String, Object> execute(Map<String, ?> context) {
        Map<String, Object> internalContext = new HashMap<>(context);
        Map<String, Object> result = new HashMap<>();
        List<Action> children = moduleType.getModules();
        for (Action child : children) {
            Map<String, ?> compositeContext = getCompositeContext(internalContext);
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

    /**
     * Create a map of links between child outputs and parent outputs. These links are base on the refecences defined in
     * the outputs of parent action.
     *
     * @param outputs outputs of the parent action. The action of {@link CompositeActionType}
     * @return map of links between child action outputs and parent output
     */
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
