/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.smarthome.model.core.guice;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * The {@link AbstractGuiceAwareExecutableExtensionFactory} is responsible for providing Guice-aware instances of Equinox extensions.
 * 
 * In Xtext this class is included in an ui bundle which has a couple of dependencies Eclipse SmartHome does not need in its runtime environment, see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=364323">Xtext Bugzilla</a>.
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @author Oliver Libutzki - Copied class from org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory to {@link AbstractGuiceAwareExecutableExtensionFactory}, added getBundle() implementation and some Javadoc
 */
public abstract class AbstractGuiceAwareExecutableExtensionFactory implements IExecutableExtensionFactory, IExecutableExtension {
	public static final String GUICEKEY = "guicekey";
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String clazzName;
	protected IConfigurationElement config;

	@SuppressWarnings({ "unchecked" })
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException {
		if (data instanceof String) {
			clazzName = (String) data;
		} else if (data instanceof Map<?, ?>) {
			clazzName = ((Map<String, String>)data).get(GUICEKEY);
		}
		if (clazzName == null) {
			throw new IllegalArgumentException("couldn't handle passed data : "+data);
		}
		this.config = config;
	}
	
	public Object create() throws CoreException {
		try {
			final Class<?> clazz = getBundle().loadClass(clazzName);
			final Injector injector = getInjector();
			final Object result = injector.getInstance(clazz);
			if (result instanceof IExecutableExtension)
				((IExecutableExtension) result).setInitializationData(config, null, null);
			return result;
		}
		catch (Exception e) {
			log.error("An error occurred while creating the extension.", e);
			throw new CoreException(new Status(IStatus.ERROR, getBundle().getSymbolicName(), e.getMessage() + " ExtensionFactory: "+ getClass().getName(), e));
		}
	}
	
	protected Bundle getBundle() {
		return FrameworkUtil.getBundle(getClass());
	}
	
	protected abstract Injector getInjector();
}
