package org.eclipse.smarthome.model.validation

import org.eclipse.smarthome.model.items.ModelItem
import org.eclipse.xtext.validation.Check
import org.eclipse.smarthome.model.items.ItemsPackage

/**
 * Custom validation rules. 
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 */
class ItemsValidator extends AbstractItemsValidator {

	@Check
	def checkItemName(ModelItem item) {
		if (item === null || item.name === null || item.name.empty) {
			return
		}
		if (item.name.contains("-") || item.name.substring(0, 1).matches("[0-9]")) {
			error('Name must not contain dash and must not start with a number', ItemsPackage.Literals.MODEL_ITEM__NAME)
		}
	}
}
