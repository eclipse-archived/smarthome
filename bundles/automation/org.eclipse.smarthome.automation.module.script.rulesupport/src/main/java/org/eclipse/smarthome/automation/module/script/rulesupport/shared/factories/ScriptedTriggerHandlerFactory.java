/**
 * Copyright (c) 2015-2017 Simon Merschjohann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.rulesupport.shared.factories;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.module.script.rulesupport.shared.ScriptedHandler;

/**
 *
 * @author Simon Merschjohann - Initial contribution
 */
public interface ScriptedTriggerHandlerFactory extends ScriptedHandler {
    public TriggerHandler get(Trigger module);
}
