/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ItemUtil} class contains utility methods for {@link Item} objects.
 * <p>
 * This class cannot be instantiated, it only contains static methods.
 *
 * @author Michael Grammling - Initial contribution and API
 * @author Simon Kaufmann - added type conversion
 * @author Martin van Wingerden - when converting types convert null to UnDefType.NULL
 */
public class ItemUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemUtil.class);

    /**
     * The constructor is private.
     * This class cannot be instantiated.
     */
    private ItemUtil() {
        // nothing to do
    }

    /**
     * Returns {@code true} if the specified name is a valid item name, otherwise {@code false}.
     * <p>
     * A valid item name must <i>only</i> only consists of the following characters:
     * <ul>
     * <li>a-z</li>
     * <li>A-Z</li>
     * <li>0..9</li>
     * <li>_ (underscore)</li>
     * </ul>
     *
     * @param itemName the name of the item to be checked (could be null or empty)
     *
     * @return true if the specified name is a valid item name, otherwise false
     */
    public static boolean isValidItemName(final String itemName) {
        return StringUtils.isNotEmpty(itemName) && itemName.matches("[a-zA-Z0-9_]*");
    }

    /**
     * Ensures that the specified name of the item is valid.
     * <p>
     * If the name of the item is invalid an {@link IllegalArgumentException} is thrown, otherwise this method returns
     * silently.
     * <p>
     * A valid item name must <i>only</i> only consists of the following characters:
     * <ul>
     * <li>a-z</li>
     * <li>A-Z</li>
     * <li>0..9</li>
     * <li>_ (underscore)</li>
     * </ul>
     *
     * @param itemName the name of the item to be checked (could be null or empty)
     *
     * @throws IllegalArgumentException if the name of the item is invalid
     */
    public static void assertValidItemName(String itemName) throws IllegalArgumentException {
        if (!isValidItemName(itemName)) {
            throw new IllegalArgumentException("The specified name of the item '" + itemName + "' is not valid!");
        }
    }

    public static State convertToAcceptedState(State state, Item item) {
        if (state == null) {
            LOGGER.error("A conversion of null was requested", new NullPointerException("state should not be null")); // NOPMD
            return UnDefType.NULL;
        }

        if (item != null && !isAccepted(item, state)) {
            for (Class<? extends State> acceptedType : item.getAcceptedDataTypes()) {
                State convertedState = state.as(acceptedType);
                if (convertedState != null) {
                    LOGGER.debug("Converting {} '{}' to {} '{}' for item '{}'", state.getClass().getSimpleName(), state,
                            convertedState.getClass().getSimpleName(), convertedState, item.getName());
                    return convertedState;
                }
            }
        }
        return state;
    }

    private static boolean isAccepted(Item item, State state) {
        return item.getAcceptedDataTypes().contains(state.getClass());
    }

}
