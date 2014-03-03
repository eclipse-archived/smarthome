/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschr??nkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import org.eclipse.xtext.common.types.access.ClasspathTypeProviderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class makes use of the {@link ActionClassLoader} instead of a normal one.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@SuppressWarnings("restriction")
@Singleton
public class ActionClasspathTypeProviderFactory extends ClasspathTypeProviderFactory {

	@Inject
	public ActionClasspathTypeProviderFactory(ClassLoader classLoader) {
		super(classLoader);
		// TODO Auto-generated constructor stub
	}

//	@Inject
//	public ActionClasspathTypeProviderFactory(ClassLoader classLoader) {
//		super(new ActionClassLoader(classLoader));
//	}
//
//	@Override
//	protected ClasspathTypeProvider createClasspathTypeProvider(ResourceSet resourceSet) {
//		return new ClasspathTypeProvider(new ActionClassLoader(super.getClassLoader(resourceSet)), resourceSet, getIndexedJvmTypeAccess());
//	}

}
