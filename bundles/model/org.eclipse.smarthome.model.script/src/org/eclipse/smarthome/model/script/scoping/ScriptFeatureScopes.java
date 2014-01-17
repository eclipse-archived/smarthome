package org.eclipse.smarthome.model.script.scoping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.model.script.internal.engine.ItemRegistryProvider;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.MapBasedScope;
import org.eclipse.xtext.xbase.scoping.batch.FeatureScopes;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureScopeSession;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;

import com.google.inject.Inject;

public class ScriptFeatureScopes extends FeatureScopes {

	
	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;
	
	@Inject
	private ItemRegistryProvider itemRegistryProvider;
	
	@Inject
	private StateAndCommandProvider stateAndCommandProvider;

	@Override
	public IScope createSimpleFeatureCallScope(EObject context,
			EReference reference, IFeatureScopeSession session,
			IResolvedTypes resolvedTypes) {
		IScope parent =  super.createSimpleFeatureCallScope(context, reference, session,
				resolvedTypes);
		List<IEObjectDescription> descriptions = new ArrayList<IEObjectDescription>();
		descriptions.addAll(createItemFeatures(context.eResource().getResourceSet()));
		descriptions.addAll(createTypeFeatures(context.eResource().getResourceSet()));

		return MapBasedScope.createScope(parent, descriptions);
	}
	
	private Collection<? extends IEObjectDescription> createTypeFeatures(ResourceSet rs) {
		
		List<IEObjectDescription> descriptions = new ArrayList<IEObjectDescription>();
		IJvmTypeProvider provider = typeProviderFactory.findOrCreateTypeProvider(rs);
		for(Type type : stateAndCommandProvider.getAllTypes()) {
			descriptions.add(EObjectDescription.create(type.toString(), provider.findTypeByName(type.getClass().getCanonicalName())));
		}
		
		return descriptions;
	}

	private List<IEObjectDescription> createItemFeatures(ResourceSet rs) {
		IJvmTypeProvider provider = typeProviderFactory.findOrCreateTypeProvider(rs);
		List<IEObjectDescription> descriptions = new ArrayList<IEObjectDescription>();
		ItemRegistry itemRegistry = itemRegistryProvider.get();
		if(itemRegistry!=null) {
			for(Item item : itemRegistry.getItems()) {
				descriptions.add(EObjectDescription.create(item.getName(), provider.findTypeByName(item.getClass().getCanonicalName())));
			}
		}
		return descriptions;
	}
}
