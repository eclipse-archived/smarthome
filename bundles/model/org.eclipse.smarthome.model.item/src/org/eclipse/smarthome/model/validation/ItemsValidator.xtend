/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
		if (item === null || item.name === null) {
			return
		}
		if (item.name.contains("-")) {
			error('Item name must not contain dashes.', ItemsPackage.Literals.MODEL_ITEM__NAME)
		}
	}
}
