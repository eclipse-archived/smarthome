/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

/**
 * The {@link Group} represents a digitalSTROM-Group.
 *
 * @author Alexander Betker
 */
public interface Group {

    /**
     * Returns the group id of this {@link Group}.
     *
     * @return group id
     */
    public short getGroupID();

    /**
     * Returns the name of this {@link Group}.
     *
     * @return group name
     */
    public String getGroupName();
}
