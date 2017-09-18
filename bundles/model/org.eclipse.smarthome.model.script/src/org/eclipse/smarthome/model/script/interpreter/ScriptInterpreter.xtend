/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.interpreter;

import com.google.inject.Inject
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemNotFoundException
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.types.Type
import org.eclipse.smarthome.model.script.lib.NumberExtensions
import org.eclipse.smarthome.model.script.scoping.StateAndCommandProvider
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmIdentifiableElement
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.xbase.XAbstractFeatureCall
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.interpreter.IEvaluationContext
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations

/**
 * The script interpreter handles ESH specific script components, which are not known
 * to the standard Xbase interpreter.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */
@SuppressWarnings("restriction")
public class ScriptInterpreter extends XbaseInterpreter {

	@Inject
	ItemRegistry itemRegistry

	@Inject
	StateAndCommandProvider stateAndCommandProvider

	@Inject
	extension IJvmModelAssociations

	override protected _invokeFeature(JvmField jvmField, XAbstractFeatureCall featureCall, Object receiver,
		IEvaluationContext context, CancelIndicator indicator) {

		// Check if the JvmField is inferred
		val sourceElement = jvmField.sourceElements.head
		if (sourceElement != null) {
			val value = context.getValue(QualifiedName.create(jvmField.simpleName))
			value ?: {

				// Looks like we have an state, command or item field
				val fieldName = jvmField.simpleName
				fieldName.stateOrCommand ?: fieldName.item
			}
		} else {
			super._invokeFeature(jvmField, featureCall, receiver, context, indicator)
		}

	}

	override protected invokeFeature(JvmIdentifiableElement feature, XAbstractFeatureCall featureCall,
		Object receiverObj, IEvaluationContext context, CancelIndicator indicator) {
		if (feature != null && feature.eIsProxy) {
			throw new RuntimeException(
				"The name '" + featureCall.toString() + "' cannot be resolved to an item or type.");
		}
		super.invokeFeature(feature, featureCall, receiverObj, context, indicator)
	}

	def protected Type getStateOrCommand(String name) {
		for (Type type : stateAndCommandProvider.getAllTypes()) {
			if (type.toString == name) {
				return type
			}
		}
	}

	def protected Item getItem(String name) {
		try {
			return itemRegistry.getItem(name);
		} catch (ItemNotFoundException e) {
			return null;
		}
	}

	override protected boolean eq(Object a, Object b) {
		if (a instanceof Type && b instanceof Number) {
			return NumberExtensions.operator_equals(a as Type, b as Number);
		} else if (a instanceof Number && b instanceof Type) {
			return NumberExtensions.operator_equals(b as Type, a as Number);
		} else {
			return super.eq(a, b);
		}
	}

	override _assignValueTo(JvmField jvmField, XAbstractFeatureCall assignment, Object value,
		IEvaluationContext context, CancelIndicator indicator) {

		// Check if the JvmField is inferred
		val sourceElement = jvmField.sourceElements.head
		if (sourceElement != null) {
			context.assignValue(QualifiedName.create(jvmField.simpleName), value)
			value
		} else {
			super._assignValueTo(jvmField, assignment, value, context, indicator)
		}
	}
	
    override protected doEvaluate(XExpression expression, IEvaluationContext context, CancelIndicator indicator) {
        if (expression == null) {
            return null
        }
        return super.doEvaluate(expression, context, indicator)
    }

}
