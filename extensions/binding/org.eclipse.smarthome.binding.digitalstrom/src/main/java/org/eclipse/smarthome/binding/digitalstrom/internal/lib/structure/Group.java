/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

/**
 * The {@link Group} represents a digitalSTROM-Group.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface Group {

    /**
     * Returns the group id of this {@link Group}.
     *
     * @return group id
     */
    short getGroupID();

    /**
     * Returns the name of this {@link Group}.
     *
     * @return group name
     */
    String getGroupName();
}
