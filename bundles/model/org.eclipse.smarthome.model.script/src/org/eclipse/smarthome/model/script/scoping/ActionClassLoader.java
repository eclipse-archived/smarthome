/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.eclipse.smarthome.model.script.internal.ScriptActivator;

/**
 * This is a special class loader that tries to resolve classes from available {@link ActionService}s,
 * if the class cannot be resolved from the normal classpath.
 *
 * @author Kai Kreuzer
 *
 */
final public class ActionClassLoader extends ClassLoader {

    public ActionClassLoader(ClassLoader cl) {
        super(cl);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            Class<?> clazz = getParent().loadClass(name);
            return clazz;
        } catch (ClassNotFoundException e) {
            Object[] services = ScriptActivator.actionServiceTracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ActionService actionService = (ActionService) service;
                    if (actionService.getActionClassName().equals(name)) {
                        return actionService.getActionClass();
                    }
                }
            }
        }
        throw new ClassNotFoundException();
    }
}