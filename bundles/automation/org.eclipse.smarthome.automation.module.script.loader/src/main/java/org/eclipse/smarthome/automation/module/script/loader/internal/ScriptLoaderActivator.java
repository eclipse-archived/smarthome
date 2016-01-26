package org.eclipse.smarthome.automation.module.script.loader.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.automation.module.script.ScriptEngineProvider;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables loading of scripts from local directory and bundles
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptLoaderActivator implements BundleActivator {
    private final Logger logger = LoggerFactory.getLogger(ScriptLoaderActivator.class);

    private ArrayList<ServiceRegistration<?>> regs = new ArrayList<>();

    private ScriptManager scriptManager;

    private Thread scriptUpdateWatcher;

    @Override
    public void start(BundleContext context) throws Exception {
        List<String> languages = ScriptEngineProvider.getScriptLanguages();
        logger.info("available languages: " + languages);

        scriptManager = new ScriptManager();
        ScriptResourceImporter importer = new ScriptResourceImporter(context, scriptManager);

        ScriptAutomationResourceBundlesEventQueue queue = new ScriptAutomationResourceBundlesEventQueue(context,
                importer);

        importer.setQueue(queue);

        queue.open();

        File folder = getFolder("scripts");

        if (folder.exists() && folder.isDirectory()) {
            loadScripts(folder);

            scriptUpdateWatcher = new Thread(new ScriptUpdateWatcher(scriptManager, folder));
            scriptUpdateWatcher.start();
        } else {
            logger.warn("Script directory: scripts missing, no scripts will be added!");
        }

    }

    /**
     * loads scripts from a given folder
     *
     * @param folder: the folder name
     */
    public void loadScripts(File folder) {
        for (File file : folder.listFiles()) {
            loadScript(file);
        }
    }

    /**
     * loads a script from the given File. It will ignore files which start by "." (hidden files) and files which are
     * not supported by any ScriptEngine.
     *
     * @param file: the file to load
     */
    public void loadScript(File file) {
        if (!file.isFile() || file.getName().startsWith(".")
                || ScriptUpdateWatcher.getScriptType(file.getName()) == null) {
            return;
        }

        try {
            scriptManager.loadScript(file.getAbsolutePath(), ScriptUpdateWatcher.getScriptType(file.getName()),
                    new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            logger.error("could not load file:" + file.getAbsolutePath());
        }
    }

    private File getFolder(String foldername) {
        File folder = new File(ConfigConstants.getConfigFolder() + File.separator + foldername);
        return folder;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> reg : regs) {
            reg.unregister();
        }

        regs.clear();
    }

}
