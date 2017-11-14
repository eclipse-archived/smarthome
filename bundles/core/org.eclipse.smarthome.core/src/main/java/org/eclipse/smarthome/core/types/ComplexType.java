/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.types;

import java.util.SortedMap;

/**
 * A complex type consists out of a sorted list of primitive constituents.
 * Each constituent can be referred to by a unique name.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ComplexType extends Type {

    /**
     * Returns all constituents with their names as a sorted map
     * 
     * @return all constituents with their names
     */
    public SortedMap<String, PrimitiveType> getConstituents();

}
