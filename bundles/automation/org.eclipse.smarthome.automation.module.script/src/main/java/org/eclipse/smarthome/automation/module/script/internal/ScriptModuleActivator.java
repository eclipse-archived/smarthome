/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.ScriptScopeProvider;
import org.eclipse.smarthome.automation.module.script.internal.factory.ScriptModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.script.internal.handler.AbstractScriptModuleHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * ScriptModuleActivator class for script automation modules
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Simon Merschjohann - original code from openHAB 1
 */
public class ScriptModuleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(ScriptModuleActivator.class);
    private BundleContext context;
    private ScriptModuleHandlerFactory moduleHandlerFactory;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration factoryRegistration;
    @SuppressWarnings("rawtypes")
    private ServiceTracker scriptScopeProviderServiceTracker;
    static private Set<ScriptScopeProvider> scriptScopeProviders;

    private final static ScriptEngineManager engineManager = new ScriptEngineManager();

    protected final static Map<String, ScriptEngine> engines = new HashMap<>();

    public BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
     * BundleContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        this.moduleHandlerFactory = new ScriptModuleHandlerFactory();
        this.moduleHandlerFactory.activate(context);
        this.factoryRegistration = bundleContext.registerService(ModuleHandlerFactory.class.getName(),
                this.moduleHandlerFactory, null);
        scriptScopeProviders = new CopyOnWriteArraySet<ScriptScopeProvider>();
        scriptScopeProviderServiceTracker = new ServiceTracker(bundleContext, ScriptScopeProvider.class.getName(),
                new ServiceTrackerCustomizer() {

                    @Override
                    public Object addingService(ServiceReference reference) {
                        Object service = bundleContext.getService(reference);
                        if (service instanceof ScriptScopeProvider) {
                            ScriptScopeProvider provider = (ScriptScopeProvider) service;
                            scriptScopeProviders.add(provider);
                            for (ScriptEngine engine : engines.values()) {
                                initializeGeneralScope(engine, provider);
                            }
                            return service;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service) {
                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service) {
                        if (service instanceof ScriptScopeProvider) {
                            ScriptScopeProvider provider = (ScriptScopeProvider) service;
                            scriptScopeProviders.remove(provider);
                            for (ScriptEngine engine : engines.values()) {
                                for (String key : provider.getScopeElements().keySet()) {
                                    engine.getBindings(ScriptContext.ENGINE_SCOPE).remove(key);
                                }
                            }
                        }
                    }
                });
        scriptScopeProviderServiceTracker.open();

        logger.debug("Started script automation support");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.context = null;
        this.moduleHandlerFactory.dispose();
        if (this.factoryRegistration != null) {
            this.factoryRegistration.unregister();
        }
        this.moduleHandlerFactory = null;
        this.scriptScopeProviderServiceTracker.close();
        ScriptModuleActivator.scriptScopeProviders.clear();
        ScriptModuleActivator.scriptScopeProviders = null;

    }

    /**
     * Gets the instance of a script engine of a given type
     *
     * @param type the mime type of the desired script engine
     *
     * @return a script engine that supports scripts of the given mime type
     */
    public static synchronized ScriptEngine getScriptEngine(String type) {
        ScriptEngine engine = engines.get(type);
        if (engine == null) {
            engine = engineManager.getEngineByMimeType(type);
            for (ScriptScopeProvider provider : scriptScopeProviders) {
                initializeScope(engine, provider);
            }
            engines.put(type, engine);
        }
        return engine;
    }

    /**
     * Adds elements from a provider to the engine scope
     *
     * @param engine the script engine to initialize
     * @param provider the provider holding the elements that should be added to the scope
     */
    private static void initializeScope(ScriptEngine engine, ScriptScopeProvider provider) {
        if (engine.getFactory().getEngineName().toLowerCase().endsWith("nashorn")) {
            initializeNashornScope(engine, provider);
        } else {
            initializeGeneralScope(engine, provider);
        }
    }

    /**
     * initializes Globals for Oracle Nashorn in conjunction with Java 8
     *
     * To prevent Class loading Problems use this directive: -Dorg.osgi.framework.bundle.parent=ext
     * further information:
     * http://apache-felix.18485.x6.nabble.com/org-osgi-framework-bootdelegation-and-org-osgi-framework-system-packages-
     * extra-td4946354.html
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=466683
     * http://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-osgi/
     * http://osdir.com/ml/users-felix-apache/2015-02/msg00067.html
     * http://stackoverflow.com/questions/30225398/java-8-scriptengine-across-classloaders
     *
     * later we will get auto imports for Classes in Nashorn:
     * further information:
     * http://nashorn.36665.n7.nabble.com/8u60-8085937-add-autoimports-sample-script-to-easily-explore-Java-classes-in-
     * interactive-mode-td4705.html
     *
     * Later in a pure Java 8/9 environment:
     * http://mail.openjdk.java.net/pipermail/nashorn-dev/2015-February/004177.html
     * Using Nashorn with interfaces loaded from custom classloaders, "script function" as a Java lambda:
     *
     * engine.put("JavaClass", (Function<String, Class>)
     * s -> {
     * try {
     * // replace this whatever Class finding logic here
     * // say, using your own class loader(s) based search
     * Class<?> c = Class.forName(s);
     * logger.error("Class " + c.getName());
     * logger.error("s " + s);
     * return Class.forName(s);
     * } catch (ClassNotFoundException cnfe) {
     * throw new RuntimeException(cnfe);
     * }
     * });
     * engine.eval("var System = JavaClass('java.lang.System').static");
     * engine.eval("System.out.println('hello world')");
     *
     * @param engine the script engine to initialize
     * @param provider the provider holding the elements that should be added to the scope
     */
    private static void initializeNashornScope(ScriptEngine engine, ScriptScopeProvider provider) {
        if (!AbstractScriptModuleHandler.class.getClassLoader().getParent().toString().contains("ExtClassLoader")) {
            logger.warn(
                    "Found wrong classloader: To prevent class loading problems use this directive: -Dorg.osgi.framework.bundle.parent=ext");
        }
        logger.debug("initializing script scope from '{}' for engine '{}'.",
                new Object[] { provider.getClass().getSimpleName(), engine.getFactory().getEngineName() });

        Set<String> expressions = new HashSet<String>();
        for (Entry<String, Object> entry : provider.getScopeElements().entrySet()) {
            if (entry.getValue() instanceof Class) {
                @SuppressWarnings("unchecked")
                Class<Object> c = (Class<Object>) entry.getValue();
                expressions.add(entry.getKey() + " = Java.type('" + c.getCanonicalName() + "')");
            } else {
                engine.put(entry.getKey(), entry.getValue());
            }
        }
        String scriptToEval = Joiner.on(",\n").join(expressions);
        try {
            engine.eval(scriptToEval);
        } catch (Exception e) {
            logger.error("Exception while importing scope: {}", e.getMessage());
        }
    }

    /**
     * Code for any other scriptengine than Nashorn
     *
     * @param engine the script engine to initialize
     * @param provider the provider holding the elements that should be added to the scope
     */
    private static void initializeGeneralScope(ScriptEngine engine, ScriptScopeProvider provider) {
        logger.debug("initializing script scope from '{}' for engine '{}'.",
                new Object[] { provider.getClass().getSimpleName(), engine.getFactory().getEngineName() });

        for (Entry<String, Object> entry : provider.getScopeElements().entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
    }

}
