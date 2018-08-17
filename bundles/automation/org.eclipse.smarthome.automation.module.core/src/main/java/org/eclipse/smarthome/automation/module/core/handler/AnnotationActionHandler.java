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
package org.eclipse.smarthome.automation.module.core.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.annotation.ActionInput;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionHandler which is dynamically created upon annotation on services
 *
 * @author Stefan Triller - initial contribution
 *
 */
public class AnnotationActionHandler extends BaseModuleHandler<Action> implements ActionHandler {

    private final Logger logger = LoggerFactory.getLogger(AnnotationActionHandler.class);

    private final Method method;
    private final ActionType moduleType;
    private final Object actionProvider;

    public AnnotationActionHandler(Action module, ActionType mt, Method method, Object actionProvider) {
        super(module);

        this.method = method;
        this.moduleType = mt;
        this.actionProvider = actionProvider;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        Map<String, Object> output = new HashMap<>();

        Annotation[][] annots = method.getParameterAnnotations();
        List<Object> args = new ArrayList<>();

        for (int i = 0; i < annots.length; i++) {
            Annotation[] annotationsOnParam = annots[i];
            if (annotationsOnParam != null && annotationsOnParam.length == 1) {
                if (annotationsOnParam[0] instanceof ActionInput) {
                    ActionInput inputAnnotation = (ActionInput) annotationsOnParam[0];
                    // check if the moduleType has a configdescription with this input
                    if (hasInput(moduleType, inputAnnotation.name())) {
                        args.add(i, context.get(inputAnnotation.name()));
                    } else {
                        logger.error(
                                "Annotated method defines input '{}' but the module type '{}' does not specify an input with this name.",
                                inputAnnotation.name(), moduleType);
                        return output;
                    }
                }
            } else {
                // TODO: sync "p+i" into constant with AnnotationActionModuleTypeProvider
                // no annotation on parameter, try to fetch the generic parameter from the context
                args.add(i, context.get("p" + i));
            }
        }

        Object result = null;
        try {
            result = method.invoke(this.actionProvider, args.toArray());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Could not call method '{}' from module type '{}'.", method, moduleType.getUID(), e);
        }
        if (result != null) {
            if (result instanceof Map<?, ?>) {
                try {
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    for (Entry<String, Object> entry : resultMap.entrySet()) {
                        if (hasOutput(moduleType, entry.getKey())) {
                            output.put(entry.getKey(), entry.getValue());
                        }
                    }
                } catch (ClassCastException ex) {
                    logger.error(
                            "The return type of action method '{}' from module type '{}' should be Map<String, Object>, because {}",
                            method, moduleType.getUID(), ex.getMessage());
                }
                // we allow simple data types as return values and put them under the context key "result".
            } else if (result instanceof Boolean) {
                output.put("result", (boolean) result);
            } else if (result instanceof String) {
                output.put("result", result);
            } else if (result instanceof Integer) {
                output.put("result", result);
            } else if (result instanceof Double) {
                output.put("result", (double) result);
            } else if (result instanceof Float) {
                output.put("result", (float) result);
            } else {
                logger.error("Non compatible return type '{}' on action method.", result.getClass());
            }
        }

        return output;
    }

    private boolean hasInput(ActionType moduleType, String in) {
        for (Input i : moduleType.getInputs()) {
            if (i.getName().equals(in)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOutput(ActionType moduleType, String out) {
        for (Output o : moduleType.getOutputs()) {
            if (o.getName().equals(out)) {
                return true;
            }
        }
        return false;
    }
}
