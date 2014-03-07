/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import java.net.URL;
import java.util.List;

import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.model.script.engine.IActionServiceProvider;
import org.eclipse.smarthome.model.script.internal.ScriptActivator;

import com.google.inject.Inject;

/**
 * This is a special class loader that tries to resolve classes from available {@link ActionService}s,
 * if the class cannot be resolved from the normal classpath.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */
final public class ActionClassLoader extends ClassLoader {
	
	public ActionClassLoader(ClassLoader cl) {
		super(cl);
	}

	IActionServiceProvider actionServiceProvider;
	
	@Inject
	public void setActionServiceProvider(IActionServiceProvider actionServiceProvider) {
		this.actionServiceProvider = actionServiceProvider;
	}
	
	@Override
	public Class<?> loadClass(String name)
			throws ClassNotFoundException {
		try {
			Class<?> clazz = getParent().loadClass(name);
			return clazz;
		} catch(ClassNotFoundException e) {
			List<ActionService> services = actionServiceProvider.get();
			if(services!=null) {
				for(ActionService actionService : services) {
					if(actionService.getActionClassName().equals(name)) {
						return actionService.getActionClass();
					}
				}
			}
		}
		throw new ClassNotFoundException();
	}
	
	@Override
	protected URL findResource(String name) {
		List<ActionService> services = actionServiceProvider.get();
		if(services!=null) {
			for(ActionService actionService : services) {
				URL url = actionService.getActionClass().getClassLoader().getResource(name);
				if (url != null) {
					return url;
				}
			}
		}
		return null;
	}
}