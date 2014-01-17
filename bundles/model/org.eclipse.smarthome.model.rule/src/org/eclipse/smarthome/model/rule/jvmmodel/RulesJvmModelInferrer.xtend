package org.eclipse.smarthome.model.rule.jvmmodel

import com.google.inject.Inject
import org.eclipse.smarthome.model.rule.rules.ChangedEventTrigger
import org.eclipse.smarthome.model.rule.rules.CommandEventTrigger
import org.eclipse.smarthome.model.rule.rules.EventTrigger
import org.eclipse.smarthome.model.rule.rules.Rule
import org.eclipse.smarthome.model.rule.rules.RuleModel
import org.eclipse.smarthome.model.script.jvmmodel.ScriptJvmModelInferrer
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.model.rule.internal.engine.RuleContextHelper
import org.eclipse.smarthome.core.types.State

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class RulesJvmModelInferrer extends ScriptJvmModelInferrer {


    /**
     * conveninence API to build and initialize JvmTypes and their members.
     */
	@Inject extension JvmTypesBuilder
	@Inject extension IQualifiedNameProvider	

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
				toField(name, type.cloneWithProxies)
			]
			members += ruleModel.rules.map[ rule |
				rule.toMethod(rule.name, ruleModel.newTypeRef(Void.TYPE)) [
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
