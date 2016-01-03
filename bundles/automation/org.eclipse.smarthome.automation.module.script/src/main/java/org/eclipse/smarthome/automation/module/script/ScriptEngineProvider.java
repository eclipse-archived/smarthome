package org.eclipse.smarthome.automation.module.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.eclipse.smarthome.automation.module.script.internal.ScriptExtensionManager;
import org.eclipse.smarthome.automation.module.script.internal.handler.AbstractScriptModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ScriptEngineProvider {
    private static final Logger logger = LoggerFactory.getLogger(ScriptEngineProvider.class);

    private final static ScriptEngineManager engineManager = new ScriptEngineManager();

    private static Set<ScriptScopeProvider> scriptScopeProviders = new CopyOnWriteArraySet<ScriptScopeProvider>();

    private static ScriptEngine nashornEngine = null;

    // global binding which is used for the Nashorn-ScriptEngine
    private static SimpleBindings nashornGlobalBinding;

    public static List<String> getScriptLanguages() {
        ArrayList<String> languages = new ArrayList<>();

        for (ScriptEngineFactory f : engineManager.getEngineFactories()) {
            languages.addAll(f.getExtensions());
        }

        return languages;
    }

    public static boolean isNashorn(ScriptEngine engine) {
        return engine != null && engine.getFactory().getEngineName().toLowerCase().endsWith("nashorn");
    }

    /**
     * Gets the instance of a script engine of a given type
     *
     * @param type the mime type of the desired script engine
     *
     * @return a script engine that supports scripts of the given mime type
     */
    public static ScriptEngine getScriptEngine(String type) {
        ScriptEngine engine = engineManager.getEngineByMimeType(type);
        if (engine == null) {
            engine = engineManager.getEngineByName(type);
        }

        if (engine == null) {
            engine = engineManager.getEngineByExtension(type);
        }

        if (engine != null) {
            if (isNashorn(engine)) {
                if (nashornEngine == null) {
                    nashornEngine = engineManager.getEngineByExtension("js");
                    nashornGlobalBinding = new SimpleBindings();

                    for (ScriptScopeProvider provider : scriptScopeProviders) {
                        initializeNashornScope(nashornEngine, provider);
                    }
                }

                engine.setBindings(nashornGlobalBinding, ScriptContext.GLOBAL_SCOPE);
            }

            // import default presets from script extensions

            boolean isNashorn = isNashorn(engine);
            for (ScriptExtensionProvider provider : ScriptExtensionManager.getScriptExtensionProviders()) {
                Collection<String> presets = provider.getDefaultPresets();
                if (presets.size() > 0) {
                    logger.debug("importing presets: {}", presets);

                    for (String preset : presets) {
                        scopeValues(engine, provider.importPreset(engine.hashCode(), preset), isNashorn);
                    }
                }
            }

            HashMap<String, Object> scriptExtension = new HashMap<>(1);
            scriptExtension.put("ScriptExtension", new ScriptExtensionManager(engine));
            scopeValues(engine, scriptExtension, isNashorn);

        } else {
            logger.error("unknown script language: {}", type);
        }

        return engine;
    }

    public static void removeEngine(ScriptEngine engine) {
        try {
            ScriptExtensionManager.dispose(engine.hashCode());
        } catch (Exception ex) {
            logger.error("error removing engine", ex);
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
     *
     * functionality:
     *
     * As we already have the classes loaded into the global scope even for the Nashorn engine, we can simply convert
     * them to Java types by adding ".static",
     * otherwise we could get OSGI classloader issues.
     *
     * So we use a dedicated ScriptEngine instance to generate our Nashorn global scope. This will replace each class
     * instance in the context by java type instances.
     * This context will be assigned to each Nashorn GLOBAL SCOPE.
     *
     * @param engine the script engine to initialize
     * @param provider the provider holding the elements that should be added to the scope
     */
    private static void initializeNashornScope(ScriptEngine engine, ScriptScopeProvider provider) {
        if (!AbstractScriptModuleHandler.class.getClassLoader().getParent().toString().contains("ExtClassLoader")) {
            logger.warn(
                    "Found wrong classloader: To prevent class loading problems use this directive: -Dorg.osgi.framework.bundle.parent=ext");
        }
        logger.debug("initializing script scope from '{}' for engine '{}'.", provider.getClass().getSimpleName(),
                engine.getFactory().getEngineName());

        nashornScopeValues(engine, provider.getScopeElements());

        Bindings bindings = nashornEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        logger.debug("new nashorn object keyset: " + bindings.keySet());

        for (String key : bindings.keySet()) {
            nashornGlobalBinding.put(key, bindings.get(key));
        }

        logger.debug("nashorn global keyset: " + nashornGlobalBinding.keySet());
    }

    public static void scopeValues(ScriptEngine engine, Map<String, Object> entries) {
        scopeValues(engine, entries, isNashorn(engine));
    }

    public static void scopeValues(ScriptEngine engine, Map<String, Object> entries, boolean isNashorn) {
        if (isNashorn) {
            nashornScopeValues(engine, entries);
        } else {
            generalScopeValues(engine, entries);
        }
    }

    public static void generalScopeValues(ScriptEngine engine, Map<String, Object> entries) {
        for (Entry<String, Object> entry : entries.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
    }

    public static void nashornScopeValues(ScriptEngine engine, Map<String, Object> entries) {
        Set<String> expressions = new HashSet<String>();

        for (Entry<String, Object> entry : entries.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());

            if (entry.getValue() instanceof Class) {
                @SuppressWarnings("unchecked")
                Class<Object> c = (Class<Object>) entry.getValue();
                expressions.add(String.format("%s = %s.static;", entry.getKey(), entry.getKey()));
            }
        }
        String scriptToEval = Joiner.on("\n").join(expressions);
        try {
            engine.eval(scriptToEval);
        } catch (ScriptException e) {
            logger.error("ScriptException while importing scope: {}", e.getMessage());
        }
    }

    /**
     * Code for any other scriptengine than Nashorn
     *
     * @param engine the script engine to initialize
     * @param provider the provider holding the elements that should be added to the scope
     */
    private static void initializeGeneralScope(ScriptScopeProvider provider) {
        logger.debug("initializing script scope from '{}'.", new Object[] { provider.getClass().getSimpleName() });

        Bindings bindings = engineManager.getBindings();

        for (Entry<String, Object> entry : provider.getScopeElements().entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
    }

    public static void addScopeProvider(ScriptScopeProvider provider) {
        scriptScopeProviders.add(provider);
        initializeGeneralScope(provider);

        // we only need to execute nashorn specific scope generation if there already exist a Nashorn-ScriptEngine,
        // otherwise the scope will be generated on first ScriptEngine request
        if (nashornEngine != null) {
            initializeNashornScope(nashornEngine, provider);
        }
    }

    public static void removeScopeProvider(ScriptScopeProvider provider) {
        scriptScopeProviders.remove(provider);

        Bindings bindings = engineManager.getBindings();

        for (String key : provider.getScopeElements().keySet()) {
            bindings.remove(key);

            if (nashornGlobalBinding != null) {
                nashornGlobalBinding.remove(key);
            }
        }
    }

    public static void clearProviders() {
        scriptScopeProviders.clear();
    }
}
