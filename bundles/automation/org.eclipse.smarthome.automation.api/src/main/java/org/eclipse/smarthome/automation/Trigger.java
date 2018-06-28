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
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Trigger modules are used in the 'ON' section of {@link Rule} definition. They defines what fires the {@link Rule}
 * (what starts execution of the {@link Rule}). The triggers don't have {@link Input} elements. They only have:
 * {@link ConfigDescriptionParameter}s and {@link Output}s defined by {@link TriggerType}.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface Trigger extends Module {

}
