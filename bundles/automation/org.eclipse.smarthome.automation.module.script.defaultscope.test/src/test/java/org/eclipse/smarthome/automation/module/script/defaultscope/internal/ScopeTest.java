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
package org.eclipse.smarthome.automation.module.script.defaultscope.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.smarthome.automation.module.script.ScriptEngineContainer;
import org.eclipse.smarthome.automation.module.script.ScriptEngineManager;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.Before;
import org.junit.Test;

/**
 * This tests the script modules
 *
 * @author Kai Kreuzer - initial contribution
 *
 */
public class ScopeTest extends JavaOSGiTest {

    private ScriptEngine engine;

    private final String path = "ESH-INF" + File.separator + "automation" + File.separator + "jsr223";
    private final String workingFile = "scopeWorking.js";
    private final String failureFile = "scopeFailure.js";

    @Before
    public void init() {
        ScriptEngineManager scriptManager = getService(ScriptEngineManager.class);
        ScriptEngineContainer container = scriptManager.createScriptEngine("js", "myJSEngine");
        engine = container.getScriptEngine();
    }

    @Test
    public void testScopeDefinesItemTypes() throws FileNotFoundException, ScriptException {
        engine.eval(new FileReader(new File(path + File.separator + workingFile)));
    }

    @Test(expected = ScriptException.class)
    public void testScopeDoesNotDefineFoobar() throws FileNotFoundException, ScriptException {
        engine.eval(new FileReader(new File(path + File.separator + failureFile)));
    }
}
