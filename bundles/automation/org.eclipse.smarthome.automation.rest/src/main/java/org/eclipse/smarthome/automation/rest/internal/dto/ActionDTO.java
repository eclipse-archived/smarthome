/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ActionDTO extends ModuleDTO {

    public Map<String, String> inputs;

    public ActionDTO(final Action action) {
        super(action);
    }

    public Action createAction() {
        final Action action = new Action(id, type, new Configuration(configuration), inputs);
        action.setLabel(label);
        action.setDescription(description);
        return action;
    }

}
