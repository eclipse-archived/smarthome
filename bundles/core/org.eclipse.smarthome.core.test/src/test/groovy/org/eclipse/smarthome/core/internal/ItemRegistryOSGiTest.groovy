package org.eclipse.smarthome.core.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil

class ItemRegistryOSGiTest extends OSGiTest {

	ItemRegistry itemRegistry
	ItemProvider itemProvider
	def ITEM_NAME = "switchItem"

	@Before
	void setUp() {
		itemRegistry = getService(ItemRegistry)
		itemProvider = [
			getItems: {[new SwitchItem(ITEM_NAME)]}, 
			addItemChangeListener: {def itemCHangeListener -> },
			removeItemChangeListener: {def itemCHangeListener -> }] as ItemProvider
	}

	@Test
	void 'assert getItems returns item from registered ItemProvider'() {

		assertThat itemRegistry.getItems().size, is(0)

		registerService itemProvider

		def items = itemRegistry.getItems()
		assertThat items.size, is(1)
		assertThat items.first().name, is(equalTo(ITEM_NAME))

		unregisterService itemProvider

		assertThat itemRegistry.getItems().size, is(0)
	}
}
