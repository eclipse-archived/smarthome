package org.eclipse.smarthome.core.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.internal.CoreActivator;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link ManagedItemProvider} is an OSGi service, that allows to add or remove
 * items at runtime by calling {@link ManagedItemProvider#addItem(Item)} or
 * {@link ManagedItemProvider#removeItem(Item)}. An added item is automatically
 * exposed to the {@link ItemRegistry}. Persistence of added Items is handled by
 * a {@link StorageService}. Items are being restored using the given 
 * {@link ItemFactory}s.
 * 
 * @author Dennis Nobel - Initial contribution
 * @author Thomas Eichstaedt-Engelen
 */
public class ManagedItemProvider extends AbstractItemProvider {
	
	private static final Logger logger =
		LoggerFactory.getLogger(ManagedItemProvider.class);

	private Collection<StorageService> storageServiceCandidates = new CopyOnWriteArrayList<StorageService>();
	private Storage<String> itemStorage;
	private Collection<ItemFactory> itemFactories = new CopyOnWriteArrayList<ItemFactory>();
	
	
	public void addStorageService(StorageService storageService) {
		storageServiceCandidates.add(storageService);
		
		// small optimization - if there is just one StorageService available 
		// we don't have to select amongst others. 
		if (storageServiceCandidates.size() == 1) {
			itemStorage = storageService.getStorage(Item.class.getName());
		} else {
			itemStorage = findStorageServiceByPriority();
		}
	}
	
	public void removeStorageService(StorageService storageService) {
		storageServiceCandidates.remove(storageService);
		
		// if there are still StorageService left, we have to select
		// a new one to take over ...
		if (storageServiceCandidates.size() > 0) {
			itemStorage = findStorageServiceByPriority();
		} else {
			itemStorage = null;
		}
	}
	
	private Storage<String> findStorageServiceByPriority() {
		ServiceReference<?> reference = 
			CoreActivator.getContext().getServiceReference(StorageService.class);
		if (reference != null) {
			StorageService service = 
				(StorageService) CoreActivator.getContext().getService(reference);			
			Storage<String> storage = service.getStorage(Item.class.getName());
			
			// free the service instance since we don't need it anymore
			service = null;
			return storage;
		}
		
		// no service of type StorageService available
		throw new IllegalStateException("There is no Service of type 'StorageService' available. This should not happen!");
	}
	
	public void addItemFactory(ItemFactory itemFactory) {
		itemFactories.add(itemFactory);
	}
	
	public void removeItemFactory(ItemFactory itemFactory) {
		itemFactories.remove(itemFactory);
	}
	

    public void addItem(Item item) {
    	if (item == null) {
    		throw new IllegalArgumentException("Cannot add null Item");
    	}
		itemStorage.put(item.getName(), toItemFactoryName(item));
        notifyItemChangeListenersAboutAddedItem(item);
    }
    
    /**
     * Translates the Items class simple name into a type name understandable
     * by the {@link ItemFactory}s.
     * 
     * @param item  the Item to translate the name
     * @return the translated ItemTypeName understandable by the {@link ItemFactory}s
     */
    private String toItemFactoryName(Item item) {
        // TODO: TEE: think about a solution to enhance the ItemFactory
    	// in a more generic way ...
    	return item.getClass().getSimpleName().replaceAll("Item", "");
    }

    public void removeItem(Item item) {
    	if (item == null) {
    		throw new IllegalArgumentException("Cannot remove null Item");
    	}
    	itemStorage.remove(item.getName());
        notifyItemChangeListenersAboutRemovedItem(item);
    }

    /**
     * Returns all Items of this {@link ItemProvider} being restored
     * from the underlying {@link StorageService} and instantiated using
     * the appropriate {@link ItemFactory}s.
     * 
     * {@inheritDoc}
     */
	@Override
	public Collection<Item> getItems() {
		Collection<Item> storedItems = new ArrayList<Item>();
		for (String itemName : itemStorage.getKeys()) {
			String itemTypeName = itemStorage.get(itemName);
			for (ItemFactory itemFactory : itemFactories) {
				GenericItem item = itemFactory.createItem(itemTypeName, itemName);
				if (item != null) {
					storedItems.add(item);
				} else {
					logger.debug("Couldn't restore item '{}' of type '{}' ~ there is no appropriate ItemFactory available.", itemName, itemTypeName);
				}
			}
		}
		return storedItems;
	}
	
	
}
