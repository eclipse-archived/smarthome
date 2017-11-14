/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine;

import com.google.inject.Singleton
import org.eclipse.smarthome.model.script.ScriptServiceUtil
import org.eclipse.smarthome.model.script.engine.IActionServiceProvider

@Singleton
class ServiceTrackerActionServiceProvider implements IActionServiceProvider {

    private val ScriptServiceUtil scriptServiceUtil
    
    new(ScriptServiceUtil scriptServiceUtil) {
        this.scriptServiceUtil = scriptServiceUtil;
    }

	override get() {
		return scriptServiceUtil.getActionServiceInstances();
	}

}
