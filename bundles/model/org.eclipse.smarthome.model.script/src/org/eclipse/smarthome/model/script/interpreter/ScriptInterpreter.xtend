/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschr??nkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.interpreter;

import com.google.inject.Inject
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemNotFoundException
import org.eclipse.smarthome.core.types.Type
import org.eclipse.smarthome.model.script.engine.ItemRegistryProvider
import org.eclipse.smarthome.model.script.lib.NumberExtensions
import org.eclipse.smarthome.model.script.scoping.StateAndCommandProvider
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

/**
 * The script interpreter handles the openHAB specific script components, which are not known
 * to the standard Xbase interpreter.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */
@SuppressWarnings("restriction")
public class ScriptInterpreter extends XbaseInterpreter {

	@Inject
	ItemRegistryProvider itemRegistryProvider
	
	@Inject
	StateAndCommandProvider stateAndCommandProvider
	
	@Inject 
	extension IJvmModelAssociations 

	override protected Object featureCallField(JvmField jvmField, Object receiver) {
		// Check if the JvmField is inferred
		val sourceElement = jvmField.sourceElements.head
		if (sourceElement != null) {
			// Looks like we have an item field
			for(Type type : stateAndCommandProvider.getAllTypes()) {
				if (type.toString == jvmField.simpleName) {
					return type
				}
			}
			return getItem(jvmField.simpleName)
		} else {
			super.featureCallField(jvmField, receiver)
		}
	}


	def protected Item getItem(String itemName) {
		val itemRegistry = itemRegistryProvider.get()
		try {
			return itemRegistry.getItem(itemName);
		} catch (ItemNotFoundException e) {
			return null;
		}
	}
	
	override protected boolean eq(Object a, Object b) {
		if(a instanceof Type && b instanceof Number) { 
			return NumberExtensions.operator_equals( a as Type, b as Number);
		} else if(a instanceof Number && b instanceof Type) {
			return NumberExtensions.operator_equals( b as Type, a as Number);
		} else {
			return super.eq(a, b);
		}
	}
}
