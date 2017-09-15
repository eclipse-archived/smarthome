/** 
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script

import com.google.inject.Guice
import com.google.inject.Injector
import org.eclipse.smarthome.model.script.engine.ScriptEngine
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xtext.resource.IResourceServiceProvider

/** 
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
class ScriptStandaloneSetup extends ScriptStandaloneSetupGenerated {
    
    static Injector injector

    private ScriptServiceUtil scriptServiceUtil;
    private ScriptEngine scriptEngine;
    
    def ScriptStandaloneSetup setScriptServiceUtil(ScriptServiceUtil scriptServiceUtil) {
        this.scriptServiceUtil = scriptServiceUtil;
        return this;
    }
    
    def ScriptStandaloneSetup setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }
    
    def ScriptServiceUtil getScriptServiceUtil() {
        return scriptServiceUtil;
    }
    
    def ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
    
    override createInjector() {
        return Guice.createInjector(new ServiceModule(scriptServiceUtil, scriptEngine), new ScriptRuntimeModule());
    }
    
    def static void doSetup(ScriptServiceUtil scriptServiceUtil, ScriptEngine scriptEngine) {
        if (injector === null) {
            injector = new ScriptStandaloneSetup().setScriptServiceUtil(scriptServiceUtil).setScriptEngine(scriptEngine).createInjectorAndDoEMFRegistration()
        }
    }

    def static Injector getInjector() {
        return injector
    }
    
    def static void doSetup() {
        throw new IllegalStateException();
    }
    
    def static void unregister() {
        EPackage.Registry.INSTANCE.remove("http://www.eclipse.org/smarthome/model/Script");
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().remove("script");
        IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().remove("script");
        
        injector = null;
    }
    
}

