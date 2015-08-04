/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Trigger modules are used in the 'ON' section of {@link Rule} definition. They
 * defines what fires the {@link Rule} (what starts execution of the {@link Rule}). The triggers don't have
 * {@link Input} elements. They only
 * have: {@link ConfigDescriptionParameter}s and {@link Output}s defined by {@link TriggerType}.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public interface Trigger extends Module {

}
