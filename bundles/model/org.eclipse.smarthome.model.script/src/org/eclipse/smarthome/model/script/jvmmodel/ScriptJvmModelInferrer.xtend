package org.eclipse.smarthome.model.script.jvmmodel

import com.google.inject.Inject
import java.util.Set
import org.eclipse.smarthome.model.script.engine.IItemRegistryProvider
import org.eclipse.smarthome.model.script.scoping.StateAndCommandProvider
import org.eclipse.smarthome.model.script.script.Script
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>
 * 
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *      
 */
class ScriptJvmModelInferrer extends AbstractModelInferrer {

	static private final Logger logger = LoggerFactory.getLogger(ScriptJvmModelInferrer)

    /**
     * conveninence API to build and initialize JvmTypes and their members.
     */
	@Inject extension JvmTypesBuilder
	

	@Inject
	IItemRegistryProvider itemRegistryProvider

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
	 def dispatch void infer(Script script, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
	 	val className = script.eResource.URI.lastSegment.split("\\.").head.toFirstUpper + "Script"
		acceptor.accept(script.toClass(className)).initializeLater [
		
			
		val Set<String> fieldNames = newHashSet()
		 
		val types = stateAndCommandProvider.allTypes
		types.forEach [ type |
			val name = type.toString
			if (fieldNames.add(name)) {
				members += script.toField(name, script.newTypeRef(type.class)) [
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
		 		members += 	script.toField(item.name, script.newTypeRef(item.class)) [
			 		static = true
			 	]
		 	} else {
		 		logger.warn("Duplicate field: '{}'. Ignoring '{}'.", item.name, item.class.name)
		 	}
		 ]
		 
		members += script.toMethod("_script", null) [
				static = true
				body = script
			]
		]
	 }
}
