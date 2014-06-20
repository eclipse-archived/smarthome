package org.eclipse.smarthome.core.items;

import static org.eclipse.smarthome.core.internal.CoreActivator.getContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
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
 * @author Kai Kreuzer - improved return values
*/
public class ManagedItemProvider extends AbstractItemProvider {

	private static final Logger logger = 
		LoggerFactory.getLogger(ManagedItemProvider.class);

	private Storage<String> itemStorage;
	private Collection<ItemFactory> itemFactories = new CopyOnWriteArrayList<ItemFactory>();
	private EventPublisher eventPublisher;
	
	
	public void setStorageService(StorageService storageService) {
		this.itemStorage = 
			storageService.getStorage(getContext(), Item.class.getName());
	}

	public void unsetStorageService(StorageService storageService) {
		this.itemStorage = null;
	}

	public void addItemFactory(ItemFactory itemFactory) {
		itemFactories.add(itemFactory);
	}

	public void removeItemFactory(ItemFactory itemFactory) {
		itemFactories.remove(itemFactory);
	}
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}
	
	
	public Item addItem(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot add null Item.");
		}

		String oldItemType = itemStorage.put(item.getName(), item.getType());
		Item oldItem = null;
		if (oldItemType != null) {
			oldItem = instantiateItem(oldItemType, item.getName());
			notifyItemChangeListenersAboutRemovedItem(oldItem);
		}
		
		notifyItemChangeListenersAboutAddedItem(item);
		return oldItem;
	}
	
	public Item removeItem(String itemName) {
		if (itemName == null) {
			throw new IllegalArgumentException("Cannot remove null Item");
		}

		String removedItemType = itemStorage.remove(itemName);
		if (removedItemType != null) {
			Item removedItem = instantiateItem(removedItemType, itemName);
			notifyItemChangeListenersAboutRemovedItem(removedItem);
			return removedItem;
		}
		return null;
	}

	/**
	 * Returns all Items of this {@link ItemProvider} being restored from the
	 * underlying {@link StorageService} and instantiated using the appropriate
	 * {@link ItemFactory}s.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Item> getItems() {
		Collection<Item> storedItems = new ArrayList<Item>();
		for (String itemName : itemStorage.getKeys()) {
			String itemTypeName = itemStorage.get(itemName);
			storedItems.add(instantiateItem(itemTypeName, itemName));
		}
		return storedItems;
	}

	private Item instantiateItem(String itemTypeName, String itemName) {
		for (ItemFactory itemFactory : itemFactories) {
			GenericItem item = itemFactory.createItem(itemTypeName, itemName);
			if (item != null) {
				item.setEventPublisher(eventPublisher);
				return item;
			}
		}
		
		logger.debug(
			"Couldn't restore item '{}' of type '{}' ~ there is no appropriate ItemFactory available.",
			itemName, itemTypeName);
		return null;
	}

}
