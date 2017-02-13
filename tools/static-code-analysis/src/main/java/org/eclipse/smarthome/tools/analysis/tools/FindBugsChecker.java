/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * Executes the <a href="http://gleclaire.github.io/findbugs-maven-plugin/index.html">findbugs-maven-plugin</a>
 * with a predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */

@Mojo(name = "findbugs", requiresDependencyResolution = ResolutionScope.COMPILE)
public class FindBugsChecker extends AbstractChecker {

    /**
     * The type of the ruleset that will be used
     */
    @Parameter(property = "ruleset", defaultValue = "bundle")
    private String rulesetType;

    /**
     * The version of the findbugs-maven-plugin that will be used
     */
    @Parameter(property = "maven.findbugs.version", defaultValue = "3.0.1")
    private String findBugsPluginVersion;

    /**
     * A list with artifacts that contain additional checks for FindBugs
     */
    @Parameter
    private Dependency[] findbugsPlugins;

    /**
     * Location of the properties file that contains configuration options for the
     * findbugs-maven-plugin
     */
    private static final String FINDBUGS_PROPERTIES_FILE = "configuration/findbugs.properties";

    /**
     * Directory where the ruleset files are located
     */
    private static final String FINDBGUS_FILTER_DIR = "rulesets/findbugs";

    private static final String FINDBUGS_MAVEN_PLUGIN_GOAL = "findbugs";
    private static final String FINDBUGS_MAVEN_PLUGIN_ARTIFACT_ID = "findbugs-maven-plugin";
    private static final String FINDBUGS_MAVEN_PLUGIN_GROUP_ID = "org.codehaus.mojo";

    /**
     * This is a property in the findbugs-maven-plugin that is used to describe the path to the
     * include filter file used
     * from
     * the plugin. It can not be set in the findbugs.properties file as it depends on the
     * {@link #rulesetType}
     */
    private static final String FINDBUGS_INCLUDE_FILTER_USER_PROPERTY = "findbugs.includeFilterFile";

    /**
     * This is a property in the findbugs-maven-plugin that is used to describe the path to the
     * exclude filter file used
     * from
     * the plugin. It can not be set in the findbugs.properties file as it depends on the
     * {@link #rulesetType}
     */
    private static final String FINDBUGS_EXCLUDE_FILTER_USER_PROPERTY = "findbugs.excludeFilterFile";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        ClassLoader cl = getMavenRuntimeClasspathClassLoader();
        Properties userProps = loadPropertiesFromFile(cl, FINDBUGS_PROPERTIES_FILE);

        // Load the include filter file
        String includeFilter = FINDBGUS_FILTER_DIR + "/" + rulesetType + ".xml";
        String includeKey = FINDBUGS_INCLUDE_FILTER_USER_PROPERTY;
        URL includeLocation = cl.getResource(includeFilter);
        String absoluteLocation = includeLocation.toString();
        log.debug("Config file found at " + absoluteLocation);
        userProps.setProperty(includeKey, absoluteLocation);

        // Load the exclude filter file
        String excludeFilter = FINDBGUS_FILTER_DIR + "/" + "exclude-" + rulesetType + ".xml";
        String excludeKey = FINDBUGS_EXCLUDE_FILTER_USER_PROPERTY;
        URL rulesetLocation = cl.getResource(excludeFilter);
        String excludeLocation = rulesetLocation.toString();
        log.debug("Config file found at " + excludeLocation);
        userProps.setProperty(excludeKey, excludeLocation);

        String outputDir = userProps.getProperty("findbugs.report.dir");

        // These configuration properties are not exposed from the findbugs-maven-plugin as user
        // properties, so they have to be set direct in the configuration
        Xpp3Dom config = configuration(element("outputDirectory", outputDir), element("xmlOutputDirectory", outputDir),
                element("findbugsXmlOutputDirectory", outputDir), getFindBugsPlugins());

        // If this dependency is missing, findbugs can not load the core plugin because of classpath
        // issues
        Dependency findBugsDep = dependency("com.google.code.findbugs", "findbugs", findBugsPluginVersion);
        Dependency[] allDependencies = getDependencies(findbugsPlugins, findBugsDep);

        executeCheck(FINDBUGS_MAVEN_PLUGIN_GROUP_ID, FINDBUGS_MAVEN_PLUGIN_ARTIFACT_ID, findBugsPluginVersion,
                FINDBUGS_MAVEN_PLUGIN_GOAL, config, allDependencies);

        log.debug("FindBugs execution has been finished.");

    }

    /**
     * Creates a "plugins" element used in the findbugs-maven-plugin configuration
     */
    private Element getFindBugsPlugins() {
        List<Element> pluginList = new LinkedList<>();
        // Add static code analysis as plugin
        pluginList.add(createPlugin(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion()));

        // Add additional dependencies
        if (findbugsPlugins != null) {
            for (Dependency artifact : findbugsPlugins) {
                Element element = createPlugin(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                pluginList.add(element);
            }
        }
        Element plugins = new Element("plugins", pluginList.toArray(new Element[pluginList.size()]));
        return plugins;
    }

    private Element createPlugin(String groudId, String artifactId, String version) {
        return element("plugin", element("groupId", groudId), element("artifactId", artifactId),
                element("version", version));
    }

}
