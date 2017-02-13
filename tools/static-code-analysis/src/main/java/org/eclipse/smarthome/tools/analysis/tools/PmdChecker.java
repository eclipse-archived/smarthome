/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executes the
 * <a href="https://maven.apache.org/plugins/maven-pmd-plugin/index.html">maven-pmd-plugin</a> with
 * a
 * predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */
@Mojo(name = "pmd", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PmdChecker extends AbstractChecker {

    /**
     * The type of the ruleset that will be used
     */
    @Parameter(property = "ruleset", defaultValue = "bundle")
    protected String rulesetType;

    /**
     * The version of the maven-pmd-plugin that will be used
     */
    @Parameter(property = "maven.pmd.version", defaultValue = "3.7")
    private String mavenPmdVersion;

    /**
     * A list with artifacts that contain additional checks for PMD
     */
    @Parameter
    private Dependency[] pmdPlugins;

    /**
     * Location of the properties files that contains configuration options for the maven-pmd-plugin
     */
    private static final String PMD_PROPERTIES_FILE = "configuration/pmd.properties";
    /**
     * Directory where the ruleset files are located
     */
    private static final String PMD_RULESET_DIR = "rulesets/pmd";

    private static final String MAVEN_PMD_PLUGIN_ARTIFACT_ID = "maven-pmd-plugin";
    private static final String MAVEN_PMD_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_PMD_PLUGIN_GOAL = "pmd";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        ClassLoader cl = getMavenRuntimeClasspathClassLoader();
        Properties userProps = loadPropertiesFromFile(cl, PMD_PROPERTIES_FILE);

        // Get the ruleset file location and configure the plugin
        String ruleset = PMD_RULESET_DIR + "/" + rulesetType + ".xml";
        String rulesetPath = cl.getResource(ruleset).toString();

        // These configuration properties are not exposed from the maven-pmd-plugin as user properties,
        // so they have to be set direct in the configuration
        Xpp3Dom configuration = configuration(
                element("targetDirectory", userProps.getProperty("pmd.custom.targetDirectory")),
                element("rulesets", element("ruleset", rulesetPath)));

        Dependency[] allDependencies = getDependencies(pmdPlugins, null);

        executeCheck(MAVEN_PMD_PLUGIN_GROUP_ID, MAVEN_PMD_PLUGIN_ARTIFACT_ID, mavenPmdVersion, MAVEN_PMD_PLUGIN_GOAL,
                configuration, allDependencies);

        log.debug("PMD execution has been finished.");

    }

}
