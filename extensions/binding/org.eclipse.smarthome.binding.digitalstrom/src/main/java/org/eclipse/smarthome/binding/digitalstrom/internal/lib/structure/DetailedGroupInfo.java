/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import java.util.List;

/**
 * The {@link DetailedGroupInfo} represents a digitalSTROM-Group with a list of all dSUID's of the included
 * digitalSTROM-Devices.
 *
 * @author Alexander Betker
 */
public interface DetailedGroupInfo extends Group {

    /**
     * Returns the list of all dSUID's of the included digitalSTROM-Devices.
     *
     * @return list of all dSUID
     */
    public List<String> getDeviceList();
}
