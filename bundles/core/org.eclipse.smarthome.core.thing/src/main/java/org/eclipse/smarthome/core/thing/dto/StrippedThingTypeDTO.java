/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.dto;

import java.util.List;

/**
 * This is a data transfer object that is used to serialize stripped thing types.
 * Stripped thing types exclude the parameters, configDescription and channels
 *
 * @author Miki Jankov - Initial contribution
 *
 */
public class StrippedThingTypeDTO {

    public String UID;
    public String label;
    public String description;
    public String category;
    public boolean listed;
    public List<String> supportedBridgeTypeUIDs;
    public boolean bridge;

    public StrippedThingTypeDTO() {
    }

    public StrippedThingTypeDTO(String UID, String label, String description, String category, boolean listed,
            List<String> supportedBridgeTypeUIDs, boolean bridge) {
        this.UID = UID;
        this.label = label;
        this.description = description;
        this.category = category;
        this.listed = listed;
        this.supportedBridgeTypeUIDs = supportedBridgeTypeUIDs;
        this.bridge = bridge;
    }
}
