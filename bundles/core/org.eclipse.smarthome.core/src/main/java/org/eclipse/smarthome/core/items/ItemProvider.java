/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.eclipse.smarthome.core.common.registry.Provider;

/**
 * An item provider provides instances of {@link GenericItem}. These
 * items can be constructed from some static configuration files or
 * they can be derived from some dynamic logic.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ItemProvider extends Provider<Item> {

}
