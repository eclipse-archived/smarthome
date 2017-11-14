/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.items.CallItem;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.ImageItem;
import org.eclipse.smarthome.core.library.items.LocationItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.PlayerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.osgi.service.component.annotations.Component;

/**
 * {@link CoreItemFactory}-Implementation for the core ItemTypes
 *
 * @author Thomas.Eichstaedt-Engelen
 * @author Kai Kreuzer
 * @author Alexander Kostadinov
 */
@Component(immediate = true)
public class CoreItemFactory implements ItemFactory {

    public static final @NonNull String CALL = "Call";
    public static final @NonNull String COLOR = "Color";
    public static final @NonNull String CONTACT = "Contact";
    public static final @NonNull String DATETIME = "DateTime";
    public static final @NonNull String DIMMER = "Dimmer";
    public static final @NonNull String IMAGE = "Image";
    public static final @NonNull String LOCATION = "Location";
    public static final @NonNull String NUMBER = "Number";
    public static final @NonNull String PLAYER = "Player";
    public static final @NonNull String ROLLERSHUTTER = "Rollershutter";
    public static final @NonNull String STRING = "String";
    public static final @NonNull String SWITCH = "Switch";

    @Override
    public GenericItem createItem(String itemTypeName, String itemName) {
        switch (itemTypeName) {
            case CALL:
                return new CallItem(itemName);
            case COLOR:
                return new ColorItem(itemName);
            case CONTACT:
                return new ContactItem(itemName);
            case DATETIME:
                return new DateTimeItem(itemName);
            case DIMMER:
                return new DimmerItem(itemName);
            case IMAGE:
                return new ImageItem(itemName);
            case LOCATION:
                return new LocationItem(itemName);
            case NUMBER:
                return new NumberItem(itemName);
            case PLAYER:
                return new PlayerItem(itemName);
            case ROLLERSHUTTER:
                return new RollershutterItem(itemName);
            case STRING:
                return new StringItem(itemName);
            case SWITCH:
                return new SwitchItem(itemName);
            default:
                return null;
        }
    }

    @Override
    public String[] getSupportedItemTypes() {
        return new String[] { SWITCH, ROLLERSHUTTER, CONTACT, STRING, NUMBER, DIMMER, DATETIME, COLOR, IMAGE, PLAYER,
                LOCATION, CALL };
    }

}
