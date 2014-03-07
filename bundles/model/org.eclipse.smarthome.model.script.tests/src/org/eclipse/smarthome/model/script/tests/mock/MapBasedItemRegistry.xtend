package org.eclipse.smarthome.model.script.tests.mock

import java.util.Map
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemNotFoundException
import org.eclipse.smarthome.core.items.ItemNotUniqueException
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener

class MapBasedItemRegistry implements ItemRegistry{
	
	final Map<String, Item> itemMap = newHashMap

	def void add(Item item) {
		itemMap.put(item.name, item)
	}
	
	override addItemRegistryChangeListener(ItemRegistryChangeListener listener) {
		// ignore
	}
	
	override getItem(String name) throws ItemNotFoundException {
		if (!itemMap.containsKey(name)) {
			throw new ItemNotFoundException('''Item «name» not found.''')
		}
		itemMap.get(name)
	}
	
	override getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
		throw new UnsupportedOperationException("Not allowed in mock implementation")
	}
	
	override getItems() {
		itemMap.values
	}
	
	override getItems(String pattern) {
		throw new UnsupportedOperationException("Not allowed in mock implementation")
	}
	
	override isValidItemName(String itemName) {
		itemMap.containsKey(itemName)
	}
	
	override removeItemRegistryChangeListener(ItemRegistryChangeListener listener) {
		// ignore
	}
	
}