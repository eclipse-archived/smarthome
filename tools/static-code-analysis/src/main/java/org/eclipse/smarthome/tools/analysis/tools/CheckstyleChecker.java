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
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executes the
 * <a href="https://maven.apache.org/components/plugins/maven-checkstyle-plugin/">maven-checkstyle-plugin</a> with a
 * predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */
@Mojo(name = "checkstyle", requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckstyleChecker extends AbstractChecker {

    /**
     * The type of the ruleset that will be used
     */
    @Parameter(property = "ruleset", defaultValue = "bundle")
    protected String rulesetType;

    /**
     * The version of the maven-checkstyle-plugin that will be used
     */
    @Parameter(property = "maven.checkstyle.version", defaultValue = "2.17")
    private String checkstyleMavenVersion;

    /**
     * Location of the properties file that contains configuration options for the maven-checkstyle-plugin
     */
    private static final String CHECKSTYLE_PROPERTIES_FILE = "configuration/checkstyle.properties";

    /**
     * Directory where the ruleset files for checkstyle are located
     */
    private static final String CHECKSTYLE_RULESET_DIR = "rulesets/checkstyle";

    // information about the maven-checkstyle-plugin
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GOAL = "checkstyle";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID = "maven-checkstyle-plugin";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    /**
     * This is a property in the maven-checkstyle-plugin that is used to describe the path to the ruleset file used from
     * the plugin. It can not be set in the checkstyle.properties file as it depends on the {@link #rulesetType}
     */
    private static final String CHECKSTYLE_RULESET_USER_PROPERTY = "checkstyle.config.location";

    @Override
    public void execute() throws MojoExecutionException {

        Log log = getLog();
        ClassLoader cl = getMavenRuntimeClasspathClassLoader();
        Properties userProps = loadPropertiesFromFile(cl, CHECKSTYLE_PROPERTIES_FILE);

        // Gets the absolute path to the ruleset file and sets the value of the corresponding user property
        String ruleset = CHECKSTYLE_RULESET_DIR + "/" + rulesetType + ".xml";

        String key = CHECKSTYLE_RULESET_USER_PROPERTY;
        URL rulesetLocation = cl.getResource(ruleset);
        String absoluteLocation = rulesetLocation.toString();
        log.debug("Config file found at " + absoluteLocation);
        userProps.setProperty(key, absoluteLocation);

        // Add the static-code-analysis plugin as dependency to maven-checkstyle-plugin, because this plugin contains
        // custom checks
        Dependency dep = dependency(SMARTHOME_TOOLS_GROUP_ID, SMARTHOME_TOOLS_ARTIFACT_ID, mavenProject.getVersion());
        
        // Maven may load an older version, if I not specify any
        Dependency checktyle = dependency("com.puppycrawl.tools", "checkstyle", "7.2");

        Xpp3Dom config = configuration(element("sourceDirectory", mavenProject.getBasedir().toString()));

        executeCheck(MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID, MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID, checkstyleMavenVersion,
                MAVEN_CHECKSTYLE_PLUGIN_GOAL, config, dep,checktyle);

        log.debug("Checkstyle execution has been finished.");

    }

}
