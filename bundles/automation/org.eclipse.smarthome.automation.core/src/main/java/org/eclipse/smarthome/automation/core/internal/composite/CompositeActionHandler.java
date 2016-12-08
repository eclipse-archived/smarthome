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
import java.util.StringTokenizer;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.core.internal.ReferenceResolverUtil;
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

    private Map<String, Output> compositeOutputs;

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
        final Map<String, Object> result = new HashMap<String, Object>();
        final List<Action> children = getChildren();
        final Map<String, Object> compositeContext = getCompositeContext(context);
        for (Action child : children) {
            ActionHandler childHandler = moduleHandlerMap.get(child);
            Map<String, Object> childContext = getChildContext(child, compositeContext);
            Map<String, Object> childResults = childHandler.execute(childContext);
            if (childResults != null) {
                for (Entry<String, Object> childResult : childResults.entrySet()) {
                    String childOuputName = child.getId() + "." + childResult.getKey();
                    Output output = compositeOutputs.get(childOuputName);
                    if (output != null) {
                        String childOuputRef = output.getReference();
                        if (childOuputRef != null && childOuputRef.length() > childOuputName.length()) {
                            childOuputRef = childOuputRef.substring(childOuputName.length());
                            result.put(output.getName(),
                                    ReferenceResolverUtil.getValue(childResult.getValue(), childOuputRef));
                        } else {
                            result.put(output.getName(), childResult.getValue());
                        }
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
    protected Map<String, Output> getCompositeOutputMap(List<Output> outputs) {
        Map<String, Output> result = new HashMap<String, Output>(11);
        if (outputs != null) {
            for (Output output : outputs) {
                String refs = output.getReference();
                if (refs != null) {
                    String ref;
                    StringTokenizer st = new StringTokenizer(refs, ",");
                    while (st.hasMoreTokens()) {
                        ref = st.nextToken().trim();
                        int i = ref.indexOf('.');
                        if (i != -1) {
                            int j = ReferenceResolverUtil.getNextRefToken(ref, i + 1);
                            if (j != -1) {
                                ref = ref.substring(0, j);
                            }
                        }
                        result.put(ref, output);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected List<Action> getChildren() {
        return moduleType.getChildren();
    }

}
