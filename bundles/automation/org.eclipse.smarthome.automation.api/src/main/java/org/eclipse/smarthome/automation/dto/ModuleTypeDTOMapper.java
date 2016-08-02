/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import org.eclipse.smarthome.automation.type.ModuleType;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ModuleTypeDTOMapper {

    protected static void fillProperties(final ModuleType from, final ModuleTypeDTO to) {
        to.uid = from.getUID();
        to.visibility = from.getVisibility();
        to.tags = from.getTags();
        to.label = from.getLabel();
        to.description = from.getDescription();
        to.configDescriptions = from.getConfigurationDescriptions();
    }

}
