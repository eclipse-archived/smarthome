package org.eclipse.smarthome.model.script.tests.mock

import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.model.script.engine.IItemRegistryProvider

class ItemRegistryProviderMock implements IItemRegistryProvider {
	
	val itemRegistry = new MapBasedItemRegistry => [
		val group1 = new GroupItem("Group1")
		val switch1 = new SwitchItem("Switch1")
		val switch2 = new SwitchItem("Switch2")
		group1.members += switch1
		
		add(group1)
		add(switch1)
		add(switch2)
		
	]
	
	override get() {
		itemRegistry
	}
	
}