/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.interpreter;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.model.script.internal.engine.ItemRegistryProvider;
import org.eclipse.smarthome.model.script.lib.NumberExtensions;
import org.eclipse.smarthome.model.script.scoping.StateAndCommandProvider;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.PolymorphicDispatcher;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.interpreter.IEvaluationContext;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;

import com.google.inject.Inject;

/**
 * The script interpreter handles the openHAB specific script components, which are not known
 * to the standard Xbase interpreter.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@SuppressWarnings("restriction")
public class ScriptInterpreter extends XbaseInterpreter {

	@Inject
	ItemRegistryProvider itemRegistryProvider;
	
	@Inject
	StateAndCommandProvider stateAndCommandProvider;
		
	private PolymorphicDispatcher<Object> featureCallDispatcher = createFeatureCallDispatcher();

	protected Object _featureCallJvmIdentifyableElement(JvmIdentifiableElement identifiable, XFeatureCall featureCall, Object receiver,
			IEvaluationContext context, CancelIndicator indicator) {
		Object value = super._featureCallJvmIdentifyableElement(identifiable, featureCall, receiver, context, indicator);
		if(value==null && receiver==null) {
			for(Type type : stateAndCommandProvider.getAllTypes()) {
				if(type.toString().equals(featureCall.toString())) {
					return type;
				}
			}
			value = getItem(featureCall.toString());
		}
		return value;
	}
	
	protected Object internalFeatureCallDispatch(XAbstractFeatureCall featureCall, Object receiverObj,
			IEvaluationContext context, CancelIndicator indicator) {
		if(featureCall.getFeature().eIsProxy()) {
			throw new RuntimeException("The name '" + featureCall.toString() + "' cannot be resolved to an item or type.");
		}
		return featureCallDispatcher.invoke(featureCall.getFeature(), featureCall, receiverObj, context, indicator);
	}

	protected Object _assignValue(XVariableDeclaration variable, XAssignment assignment, Object value,
			IEvaluationContext context, CancelIndicator indicator) {
		context.assignValue(QualifiedName.create(variable.getName()), value);
		return value;
	}

	protected Item getItem(String itemName) {
		ItemRegistry itemRegistry = itemRegistryProvider.get();
		try {
			return itemRegistry.getItem(itemName);
		} catch (ItemNotFoundException e) {
			return null;
		}
	}
	
	@Override
	protected boolean eq(Object a, Object b) {
		if(a instanceof Type && b instanceof Number) { 
			return NumberExtensions.operator_equals((Type) a, (Number) b);
		} else if(a instanceof Number && b instanceof Type) {
			return NumberExtensions.operator_equals((Type) b, (Number) a);
		} else {
			return super.eq(a, b);
		}
	}
}
