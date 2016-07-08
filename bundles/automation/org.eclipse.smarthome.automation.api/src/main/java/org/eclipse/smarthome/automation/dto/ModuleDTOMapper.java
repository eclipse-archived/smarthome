/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.dto;

import org.eclipse.smarthome.automation.Module;

/**
 * This is a utility class to convert between the respective object and its DTO.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ModuleDTOMapper {

    protected static void fillProperties(final Module from, final ModuleDTO to) {
        to.id = from.getId();
        to.label = from.getLabel();
        to.description = from.getDescription();
        to.configuration = from.getConfiguration().getProperties();
        to.type = from.getTypeUID();
    }

    protected static void fillProperties(final ModuleDTO from, final Module to) {
        to.setLabel(from.label);
        to.setDescription(from.description);
        // to.setConfiguration(new Configuration(from.configuration));
    }

}
