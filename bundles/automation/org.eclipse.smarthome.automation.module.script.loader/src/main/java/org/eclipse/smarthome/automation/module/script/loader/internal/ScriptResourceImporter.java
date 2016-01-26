/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.script.loader.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is implementation of {@link ScriptResourceImporter}. It serves for providing {@link Rule}s by loading
 * bundle resources. It extends functionality of {@link AbstractResourceBundleProvider} by specifying:
 * <ul>
 * <li>the path to resources, corresponding to the {@link Rule}s - root directory
 * {@link AbstractResourceBundleProvider#PATH} with sub-directory "rules".
 * <li>type of the {@link Parser}s, corresponding to the {@link Rule}s - {@link Parser#PARSER_RULE}
 * <li>specific functionality for loading the {@link Rule}s
 * <li>tracking the managing service of the {@link Rule}s.
 * </ul>
 *
 * @author Simon Merschjohann
 *
 */
public class ScriptResourceImporter {
    /**
     * This static field provides a root directory for automation object resources in the bundle resources.
     * It is common for all resources - {@link ModuleType}s, {@link RuleTemplate}s and {@link Rule}s.
     */
    protected static String PATH = "ESH-INF/automation/scripts/";

    protected Logger logger = LoggerFactory.getLogger(ScriptResourceImporter.class);

    private BundleContext bc;

    private ScriptAutomationResourceBundlesEventQueue queue;

    private ScriptManager scriptManager;

    /**
     * This constructor is responsible for initializing the path to resources and tracking the managing service of the
     * {@link Rule}s.
     *
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public ScriptResourceImporter(BundleContext context, ScriptManager scriptManager) {
        bc = context;
        this.scriptManager = scriptManager;
    }

    public void setQueue(ScriptAutomationResourceBundlesEventQueue queue) {
        this.queue = queue;
    }

    public boolean isReady() {
        return queue != null;
    }

    /**
     * This method is used to determine which script type to be used.
     *
     * @param url the URL of the source of data for parsing.
     * @return the type of the script.
     */
    protected String getScriptType(URL url) {
        String fileName = url.getPath();
        int fileExtesionStartIndex = fileName.lastIndexOf(".") + 1;
        if (fileExtesionStartIndex == -1) {
            return null;
        }

        String fileExtesion = fileName.substring(fileExtesionStartIndex);
        return fileExtesion;
    }

    /**
     * This method provides functionality for processing the bundles with rule resources.
     * <p>
     * Checks for availability of the needed {@link Parser} and for availability of the rules managing service. If one
     * of them is not available - the bundle is added into {@link #waitingProviders} and the execution of the method
     * ends.
     * <p>
     * Continues with loading the rules. If a rule already exists, it is updated, otherwise it is added.
     * <p>
     * The loading can fail because of {@link IOException}.
     *
     * @param bundle it is a {@link Bundle} which has to be processed, because it provides resources for automation
     *            rules.
     */
    protected void processAutomationProvider(Bundle bundle) {
        logger.debug("Load scripts from bundle '{}' ", bundle.getSymbolicName());

        Enumeration<URL> urlEnum = bundle.findEntries(PATH, null, false);
        if (urlEnum == null) {
            return;
        }

        String idVendor = String.format("%s;%s;", bundle.getSymbolicName(), bundle.getVersion().toString());
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            String scriptType = getScriptType(url);

            if (scriptManager.isSupported(scriptType)) {
                try {
                    String identifier = idVendor + url.toString();
                    scriptManager.loadScript(identifier, scriptType, new InputStreamReader(url.openStream()));

                } catch (IOException e) {
                    logger.error(
                            "Can't read from resource of bundle with ID " + bundle.getBundleId() + ". URL is " + url,
                            e);
                }
            } else {
                logger.info("ignoring file in bundle: " + url);
            }
        }
    }

    public void processAutomationProviderUninstalled(Bundle bundle) {
        logger.debug("Unload scripts from bundle '{}' ", bundle.getSymbolicName());

        Enumeration<URL> urlEnum = bundle.findEntries(PATH, null, false);
        if (urlEnum == null) {
            return;
        }

        String idVendor = String.format("%s;%s;", bundle.getSymbolicName(), bundle.getVersion().toString());
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            String scriptType = getScriptType(url);

            if (scriptManager.isSupported(scriptType)) {
                scriptManager.unloadScript(idVendor + url.toString());
            }
        }
    }

}