package org.eclipse.smarthome.model.script.scoping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.smarthome.core.persistence.extensions.PersistenceExtensions;
import org.eclipse.smarthome.core.scriptengine.action.ActionService;
import org.eclipse.smarthome.model.script.actions.BusEvent;
import org.eclipse.smarthome.model.script.actions.LogAction;
import org.eclipse.smarthome.model.script.actions.ScriptExecution;
import org.eclipse.smarthome.model.script.internal.ScriptActivator;
import org.eclipse.smarthome.model.script.lib.NumberExtensions;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedTypes;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;


/**
 * This class registers all statically available functions as well as the
 * extensions for specific jvm types, which should only be available in rules,
 * but not in scripts
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */

@SuppressWarnings("restriction")
@Singleton
public class ScriptImplicitlyImportedTypes extends ImplicitlyImportedTypes {

	private List<Class<?>> actionClasses = null;
	
	private int trackingCount = -1;
	
	@Override
	protected List<Class<?>> getExtensionClasses() {
		List<Class<?>> result = super.getExtensionClasses();
		result.remove(Comparable.class);
		result.remove(Double.class);
		result.remove(Integer.class);
		result.remove(BigInteger.class);
		result.remove(BigDecimal.class);
		result.remove(double.class);
		result.add(NumberExtensions.class);
		result.add(StringUtils.class);
		result.add(URLEncoder.class);
		result.add(PersistenceExtensions.class);
		result.add(BusEvent.class);
		return result;
	}
	
//	@Override
//	public List<JvmType> getStaticImportClasses(Resource context) {
//		List<JvmType> result = super.getStaticImportClasses(context);
//		
//		List<Class<?>> actionClasses = getActionClasses();
//		result.addAll(getTypes(actionClasses, context));
//		return result;
//	}
//	
//	protected Collection<JvmType> getClassNameTypes(Collection<String> classNames, Resource context) {
//		List<JvmType> result = Lists.newArrayListWithCapacity(classNames.size());
//		for(String className: classNames) {
//			JvmType type = typeReferences.findDeclaredType(className, context);
//			if (type != null)
//				result.add(type);
//		}
//		return result;
//	}
	
	@Override
	protected List<Class<?>> getStaticImportClasses() {
		List<Class<?>> result = super.getStaticImportClasses();
		result.add(BusEvent.class);
		result.add(ScriptExecution.class);
		result.add(LogAction.class);

		// jodatime static functions
		result.add(DateTime.class);
		result.add(DateMidnight.class);
		
		result.addAll(getActionClasses());
		return result;
	}
	
	protected List<Class<?>> getActionClasses() {
		
		int currentTrackingCount = ScriptActivator.actionServiceTracker.getTrackingCount();
		
		// if something has changed about the tracked services, recompute the list
		if(trackingCount != currentTrackingCount) {
			trackingCount = currentTrackingCount;
			List<Class<?>> privateActionClasses = new ArrayList<Class<?>>();
			// add all actions that are contributed as OSGi services
			Object[] services = ScriptActivator.actionServiceTracker.getServices();
			if(services!=null) {
				for(Object service : services) {
					ActionService actionService = (ActionService) service;
					privateActionClasses.add(actionService.getActionClass());
				}
			}
			this.actionClasses=privateActionClasses;
		}
		return this.actionClasses;
	}
}