package org.eclipse.smarthome.model.script.scoping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.script.internal.engine.ItemRegistryProvider;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureNames;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureScopeSession;
import org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareReentrantTypeResolver;
import org.eclipse.xtext.xbase.typesystem.internal.ResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class ScriptReentrantTypeResolver extends
		LogicalContainerAwareReentrantTypeResolver {
	
	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;
	
	@Inject
	private ItemRegistryProvider itemRegistryProvider;
	
	@Override
	protected Map<JvmIdentifiableElement, ResolvedTypes> prepare(
			ResolvedTypes resolvedTypes,
			IFeatureScopeSession featureScopeSession) {
		addItems(featureScopeSession, resolvedTypes.getReferenceOwner(), (JvmDeclaredType)getRootJvmType());
		return super.prepare(resolvedTypes, featureScopeSession);
	}

	
	protected IFeatureScopeSession addItems(IFeatureScopeSession session, ITypeReferenceOwner owner, JvmDeclaredType thisType) {
		IFeatureScopeSession childSession = null;
		IJvmTypeProvider provider = typeProviderFactory.findOrCreateTypeProvider(thisType.eResource().getResourceSet());
		List<IEObjectDescription> descriptions = new ArrayList<IEObjectDescription>();
		ItemRegistry itemRegistry = itemRegistryProvider.get();
		if(itemRegistry!=null) {
			ImmutableMap.Builder<QualifiedName, JvmIdentifiableElement> builder = ImmutableMap.builder();
			for(Item item : itemRegistry.getItems()) {
				builder.put(QualifiedName.create(item.getName()), provider.findTypeByName(item.getClass().getCanonicalName()));
			}
			childSession = session.addLocalElements(builder.build(), owner);
		}
		childSession = addThisTypeToStaticScope(childSession, thisType);
		return childSession;
	}

}
