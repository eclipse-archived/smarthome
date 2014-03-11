/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.ImageItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;


/**
 * {@link CoreItemFactory}-Implementation for the core ItemTypes 
 * 
 * @author Thomas.Eichstaedt-Engelen
 */
public class CoreItemFactory implements ItemFactory {
	
	private static String[] ITEM_TYPES = new String[] { "Switch", "Rollershutter", "Contact", "String", "Number", "Dimmer", "DateTime", "Color", "Image" };

	/**
	 * @{inheritDoc}
	 */
	public GenericItem createItem(String itemTypeName, String itemName) {
		if (itemTypeName.equals(ITEM_TYPES[0])) return new SwitchItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[1])) return new RollershutterItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[2])) return new ContactItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[3])) return new StringItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[4])) return new NumberItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[5])) return new DimmerItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[6])) return new DateTimeItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[7])) return new ColorItem(itemName);
		if (itemTypeName.equals(ITEM_TYPES[8])) return new ImageItem(itemName);
		else return null;
	}
	
	/**
	 * @{inheritDoc}
	 */
	public String[] getSupportedItemTypes() {
		return ITEM_TYPES;
	}

}
