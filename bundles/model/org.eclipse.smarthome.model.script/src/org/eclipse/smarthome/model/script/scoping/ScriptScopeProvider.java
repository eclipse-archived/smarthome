/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.model.script.engine.ItemRegistryProvider;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.MapBasedScope;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.scoping.XbaseScopeProvider;

import com.google.inject.Inject;

/**
 * This scope provider adds all items, states and commands to the scope, so that
 * these are always available in the scripts.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@SuppressWarnings("all")
public class ScriptScopeProvider extends XbaseScopeProvider {

	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;
	
	@Inject
	private ItemRegistryProvider itemRegistryProvider;
	
	@Inject
	private StateAndCommandProvider stateAndCommandProvider;

	public ScriptScopeProvider() {
	}

	@Override
	public IScope createSimpleFeatureCallScope(EObject context,
			EReference reference, Resource resource,
			boolean includeCurrentBlock, int idx) {
		IScope parent =  super.createSimpleFeatureCallScope(context, reference, resource,
				includeCurrentBlock, idx);
		List<IEObjectDescription> descriptions = new ArrayList<IEObjectDescription>();
		descriptions.addAll(createItemFeatures(resource.getResourceSet()));
		descriptions.addAll(createTypeFeatures(resource.getResourceSet()));

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