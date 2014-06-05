package org.eclipse.smarthome.core.thing.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ItemChannelBindingRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingHelper {

	private final static Logger logger = LoggerFactory.getLogger(ThingHelper.class);
	
	private BundleContext bundleContext;
	
	public ThingHelper(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	/**
	 * @param thing
	 */
	public void createAndBindItems(Thing thing) {
		List<ItemFactory> itemFactories = getItemFactories();
		
		if (itemFactories.isEmpty()) {
			logger.warn("No ItemFactory has been registered. It's not possible to create items.");
			return;
		}
		
		ManagedItemProvider managedItemProvider = getManagedItemProvider();
		
		ItemChannelBindingRegistry itemChannelBindingRegistry = getItemChannelBindingRegistry();
		
		List<Channel> channels = thing.getChannels();
		
		for (Channel channel : channels) {
			String acceptedItemType = channel.getAcceptedItemType();
			ItemFactory itemFactory = getItemFactoryForItemType(itemFactories, acceptedItemType);
			if (itemFactory == null) {
				logger.warn("No ItemFactory supports the item type '{}'. It's not possible to create an item for the channel '{}'.", acceptedItemType, channel.getUID());
			} else {
				GenericItem item = itemFactory.createItem(acceptedItemType, channel.getUID().toString().replace(":", "_"));
				if (item == null) {
					logger.error("The item of type '{}' has not been created by the ItemFactory '{}'.", acceptedItemType, itemFactory.getClass().getName());
				} else {
					managedItemProvider.addItem(item);
					itemChannelBindingRegistry.bind(item.getName(), channel.getUID());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private ManagedItemProvider getManagedItemProvider() {
		ServiceReference<ManagedItemProvider> managedItemProviderServiceRef = (ServiceReference<ManagedItemProvider>) bundleContext.getServiceReference(ManagedItemProvider.class.getName());
		if (managedItemProviderServiceRef == null) {
			return null;
		}
		ManagedItemProvider managedItemProvider = bundleContext.getService(managedItemProviderServiceRef);
		return managedItemProvider;
	}
	
	@SuppressWarnings("unchecked")
	private ItemChannelBindingRegistry getItemChannelBindingRegistry() {
		ServiceReference<ItemChannelBindingRegistry> itemChannelBindingRegistryServiceRef = (ServiceReference<ItemChannelBindingRegistry>) bundleContext.getServiceReference(ItemChannelBindingRegistry.class.getName());
		if (itemChannelBindingRegistryServiceRef == null) {
			return null;
		}
		ItemChannelBindingRegistry itemChannelBindingRegistry = bundleContext.getService(itemChannelBindingRegistryServiceRef);
		return itemChannelBindingRegistry;
	}
	
	@SuppressWarnings("unchecked")
	private List<ItemFactory> getItemFactories() {
		List<ItemFactory> itemFactories = new ArrayList<>();
		ServiceReference<ItemFactory>[] itemFactoryServiceRefs = null;
		try {
			itemFactoryServiceRefs = (ServiceReference<ItemFactory>[]) bundleContext.getServiceReferences(ItemFactory.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("The item factories cannot be obtained.", e);
		}
		if (itemFactoryServiceRefs == null) {
			return itemFactories;
		}
		for (ServiceReference<ItemFactory> serviceReference : itemFactoryServiceRefs) {
			itemFactories.add(bundleContext.getService(serviceReference));
		}
		return itemFactories;
	}
	
	private ItemFactory getItemFactoryForItemType(List<ItemFactory> itemFactories, String itemType) {
		for (ItemFactory itemFactory : itemFactories) {
			String[] supportedItemTypes = itemFactory.getSupportedItemTypes();
			for (int i = 0; i < supportedItemTypes.length; i++) {
				String supportedItemType = supportedItemTypes[i];
				if (supportedItemType.equals(itemType) ) {
					return itemFactory;
				}
			}
		}
		return null;
	}
	
	
	
}
