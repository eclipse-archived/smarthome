/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class ConditionDTO extends ModuleDTO {

    public Map<String, String> inputs;

    public ConditionDTO(final Condition condition) {
        super(condition);
    }

    public Condition createCondition() {
        final Condition condition = new Condition(id, type, new Configuration(configuration), inputs);
        condition.setLabel(label);
        condition.setDescription(description);
        return condition;
    }

}
