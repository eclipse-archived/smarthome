package org.eclipse.smarthome.model.rule.jvmmodel

import com.google.inject.Inject
import java.util.Map
import java.util.Set
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.model.rule.internal.engine.RuleContextHelper
import org.eclipse.smarthome.model.rule.internal.engine.RuleEngine
import org.eclipse.smarthome.model.rule.rules.ChangedEventTrigger
import org.eclipse.smarthome.model.rule.rules.CommandEventTrigger
import org.eclipse.smarthome.model.rule.rules.EventTrigger
import org.eclipse.smarthome.model.rule.rules.Rule
import org.eclipse.smarthome.model.rule.rules.RuleModel
import org.eclipse.smarthome.model.script.engine.ItemRegistryProvider
import org.eclipse.smarthome.model.script.jvmmodel.ScriptJvmModelInferrer
import org.eclipse.smarthome.model.script.scoping.StateAndCommandProvider
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class RulesJvmModelInferrer extends ScriptJvmModelInferrer {



	static private final Logger logger = LoggerFactory.getLogger(RulesJvmModelInferrer)
    /**
     * conveninence API to build and initialize JvmTypes and their members.
     */
	@Inject extension JvmTypesBuilder
	@Inject extension IQualifiedNameProvider	

	@Inject
	ItemRegistryProvider itemRegistryProvider

	@Inject
	StateAndCommandProvider stateAndCommandProvider

	/**
	 * Is called for each instance of the first argument's type contained in a resource.
	 * 
	 * @param element - the model to create one or more JvmDeclaredTypes from.
	 * @param acceptor - each created JvmDeclaredType without a container should be passed to the acceptor in order get attached to the
	 *                   current resource.
	 * @param isPreLinkingPhase - whether the method is called in a pre linking phase, i.e. when the global index isn't fully updated. You
	 *        must not rely on linking using the index if iPrelinkingPhase is <code>true</code>
	 */
	 
	
	 def dispatch void infer(RuleModel ruleModel, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(ruleModel.toClass("test.TestClass")).initializeLater [
			members += ruleModel.variables.filter(XVariableDeclaration).map[
				toField(name, type.cloneWithProxies) => [
					static = true
				]
			]
			
			val Set<String> fieldNames = newHashSet()
			 
			val types = stateAndCommandProvider.allTypes
			types.forEach [ type |
				val name = type.toString
				if (fieldNames.add(name)) {
					members += ruleModel.toField(name, ruleModel.newTypeRef(type.class)) [
						static = true
					]
				} else {
					logger.warn("Duplicate field: '{}'. Ignoring '{}'.", name, type.class.name)
				}
			]

			 val itemRegistry = itemRegistryProvider.get
			 itemRegistry.items.forEach[ item |
			 	val name = item.name
					if (fieldNames.add(name)) {
			 		members += 	ruleModel.toField(item.name, ruleModel.newTypeRef(item.class)) [
				 		static = true
				 	]
			 	} else {
			 		logger.warn("Duplicate field: '{}'. Ignoring '{}'.", item.name, item.class.name)
			 	}
			 ]
			 
			members += ruleModel.rules.map[ rule |
				rule.toMethod("_" + rule.name, ruleModel.newTypeRef(Void.TYPE)) [
					static = true
					if(containsCommandTrigger(rule)) {
						val commandTypeRef = ruleModel.newTypeRef(Command)
						parameters += rule.toParameter(RuleContextHelper.VAR_RECEIVED_COMMAND, commandTypeRef) 
					}
					if(containsStateChangeTrigger(rule)) {
						val stateTypeRef = ruleModel.newTypeRef(State)
						parameters += rule.toParameter(RuleContextHelper.VAR_PREVIOUS_STATE, stateTypeRef) 
					}
					
					body = rule.script
				]
			]
		]
	 }


	def private boolean containsCommandTrigger(Rule rule) {
		for(EventTrigger trigger : rule.getEventtrigger()) {
			if(trigger instanceof CommandEventTrigger) {
				return true;
			}
		}
		return false;
	}

	def private boolean containsStateChangeTrigger(Rule rule) {
		for(EventTrigger trigger : rule.getEventtrigger()) {
			if(trigger instanceof ChangedEventTrigger) {
				return true;
			}
		}
		return false;
	}


}
