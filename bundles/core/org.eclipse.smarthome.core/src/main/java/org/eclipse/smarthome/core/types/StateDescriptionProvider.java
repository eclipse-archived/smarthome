/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

import java.util.Locale;

/**
 * A {@link StateDescriptionProvider} provides localized {@link StateDescription}s for items.
 *
 * @author Dennis Nobel - Initial contribution
 */
public interface StateDescriptionProvider {

    /**
     * Returns the state description for an item name
     *
     * @param itemName
     *            item name (must not be null)
     * @param locale
     *            locale (can be null)
     * @return state description or null if no state description could be found
     */
    StateDescription getStateDescription(String itemName, Locale locale);

}
