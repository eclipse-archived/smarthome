package org.eclipse.smarthome.model.script.engine;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.script.internal.engine.ServiceTrackerItemRegistryProvider;

import com.google.inject.ImplementedBy;
import com.google.inject.Provider;

@ImplementedBy(ServiceTrackerItemRegistryProvider.class)
public interface IItemRegistryProvider extends Provider<ItemRegistry>{

}
