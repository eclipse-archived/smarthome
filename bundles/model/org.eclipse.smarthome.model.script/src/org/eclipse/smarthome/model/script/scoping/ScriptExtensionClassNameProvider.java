/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.extensions.PersistenceExtensions;
import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.model.script.actions.BusEvent;
import org.eclipse.smarthome.model.script.actions.LogAction;
import org.eclipse.smarthome.model.script.actions.ScriptExecution;
import org.eclipse.smarthome.model.script.internal.ScriptActivator;
import org.eclipse.smarthome.model.script.lib.NumberExtensions;
import org.eclipse.xtext.xbase.scoping.featurecalls.StaticImplicitMethodsFeatureForTypeProvider.ExtensionClassNameProvider;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.common.collect.Multimap;
import com.google.inject.Singleton;

/**
 * This class registers all statically available functions as well as the
 * extensions for specific jvm types.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@SuppressWarnings("restriction")
@Singleton
public class ScriptExtensionClassNameProvider extends ExtensionClassNameProvider {

	private int trackingCount = -1;
	
	@Override
	protected Collection<String> getLiteralClassNames() {
		int currentTrackingCount = ScriptActivator.actionServiceTracker.getTrackingCount();
		
		// if something has changed about the tracked services, recompute the list
		if(trackingCount != currentTrackingCount) {
			trackingCount = currentTrackingCount;
			return computeLiteralClassNames();
		} else {
			return super.getLiteralClassNames();
		}
	}

	@Override
	protected Collection<String> computeLiteralClassNames() {
		Collection<String> extensions = super.computeLiteralClassNames();
		
		// add all actions that are contributed as OSGi services
		Object[] services = ScriptActivator.actionServiceTracker.getServices();
		if(services!=null) {
			for(Object service : services) {
				ActionService actionService = (ActionService) service;
				extensions.add(actionService.getActionClassName());
			}
		}
		
		extensions.add(BusEvent.class.getCanonicalName());
		extensions.add(ScriptExecution.class.getCanonicalName());
		extensions.add(LogAction.class.getCanonicalName());

		// jodatime static functions
		extensions.add(DateTime.class.getCanonicalName());
		extensions.add(DateMidnight.class.getCanonicalName());
		return extensions;
	}
	
	@Override
	protected Multimap<Class<?>, Class<?>> simpleComputeExtensionClasses() {
		Multimap<Class<?>, Class<?>> result = super.simpleComputeExtensionClasses();
		result.removeAll(Comparable.class);
		result.removeAll(Double.class);
		result.removeAll(Integer.class);
		result.removeAll(BigInteger.class);
		result.removeAll(BigDecimal.class);
		result.removeAll(double.class);
		result.put(Number.class, NumberExtensions.class);
		result.put(Type.class, NumberExtensions.class);
		result.put(Comparable.class, NumberExtensions.class);
		result.put(String.class, StringUtils.class);
		result.put(String.class, URLEncoder.class);
		result.put(Item.class, PersistenceExtensions.class);
		result.put(Item.class, BusEvent.class);
		return result;
	}
}
