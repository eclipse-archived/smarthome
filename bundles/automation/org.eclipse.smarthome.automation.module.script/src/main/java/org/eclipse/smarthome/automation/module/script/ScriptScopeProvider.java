/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script;

import java.util.Map;

/**
 * A {@link ScriptScopeProvider} defines variable and types that should be made available in general within scripts.
 * As this can heavily depend on the solution, this is done through (optional) providers and not hardcoded within the
 * script modules.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public interface ScriptScopeProvider {

    /**
     * <p>
     * Returns a map of variable or type names with the according object or class.
     * </p>
     * <p>
     * Example:<br/>
     * <code>"File" -> File.class</code><br/>
     * <code>"uuid" -> UUID.randomUUID()</code>
     * </p>
     *
     * @return a map with the elements to put in the scope
     */
    Map<String, Object> getScopeElements();
}
