/**
 * Copyright (c) 2016 Markus Rathgeb
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.automation.rest.internal.dto;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Markus Rathgeb - Initial contribution and API
 */
public class TriggerDTO extends ModuleDTO {

    public TriggerDTO(final Trigger trigger) {
        super(trigger);
    }

    public Trigger createTrigger() {
        final Trigger trigger = new Trigger(id, type, new Configuration(configuration));
        trigger.setLabel(label);
        trigger.setDescription(description);
        return trigger;
    }

}
