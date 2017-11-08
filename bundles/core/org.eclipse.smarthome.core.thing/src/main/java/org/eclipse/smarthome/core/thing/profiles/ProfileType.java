/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.common.registry.Identifiable;

/**
 * Describes a profile type.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface ProfileType extends Identifiable<ProfileTypeUID> {

    /**
     * Constant for mathing *ANY* item type.
     */
    public static final Collection<String> ANY_ITEM_TYPE = new ArrayList<>(0);

    /**
     *
     * @return a collection of item types or {@link #ANY_ITEM_TYPE}.
     */
    Collection<String> getSupportedItemTypes();

    /**
     * Get a human readable description.
     *
     * @return the label
     */
    String getLabel();

}
