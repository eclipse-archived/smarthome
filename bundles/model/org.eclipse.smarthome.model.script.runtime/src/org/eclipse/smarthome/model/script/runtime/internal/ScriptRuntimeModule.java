/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.runtime.internal;

import org.eclipse.smarthome.model.script.engine.Script;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.runtime.internal.engine.ScriptEngineImpl;
import org.eclipse.smarthome.model.script.runtime.internal.engine.ScriptImpl;
import org.eclipse.xtext.service.AbstractGenericModule;

/**
 * The {@link ScriptRuntimeModule} provides Eclipse SmatrtHome runtime environment specific Guice bindings.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class ScriptRuntimeModule extends AbstractGenericModule {

    public Class<? extends ScriptEngine> bindScriptEngine() {
        return ScriptEngineImpl.class;
    }

    public Class<? extends Script> bindScript() {
        return ScriptImpl.class;
    }
}
