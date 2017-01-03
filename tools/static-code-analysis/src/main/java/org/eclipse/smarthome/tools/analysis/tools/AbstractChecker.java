/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Base class for MOJOs that call Maven plugins
 *
 * @author Svilen Valkanov
 *
 */
public abstract class AbstractChecker extends AbstractMojo {

    protected static final String SMARTHOME_TOOLS_ARTIFACT_ID = "static-code-analysis";
    protected static final String SMARTHOME_TOOLS_GROUP_ID = "org.eclipse.smarthome.tools";

    @Component
    protected MavenProject mavenProject;

    @Component
    protected MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    private static URLClassLoader mavenRuntimeClasspathClassLoader = null;

    /**
     * Can be used to load resources from the Maven plugin project
     */
    protected URLClassLoader getMavenRuntimeClasspathClassLoader() throws MojoExecutionException {
        if (mavenRuntimeClasspathClassLoader == null) {
            try {
                @SuppressWarnings("rawtypes")
                List runtimeClasspathElements = mavenProject.getRuntimeClasspathElements();
                URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
                for (int i = 0; i < runtimeClasspathElements.size(); i++) {
                    String element = (String) runtimeClasspathElements.get(i);
                    runtimeUrls[i] = new File(element).toURI().toURL();
                }
                mavenRuntimeClasspathClassLoader = new URLClassLoader(runtimeUrls,
                        Thread.currentThread().getContextClassLoader());

            } catch (DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("Can't create custom class loader!", e);
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Claspathentry does not exist!", e);
            }
        }
        return mavenRuntimeClasspathClassLoader;
    }

    /**
     * Loads properties from file into the Maven user properties
     *
     * @param cl - ClassLoader that can load the properties file
     * @param relativePath - relative path to the properties file
     * @return - the loaded properties
     * @throws MojoExecutionException - when the properties file can not be found or loaded
     */
    protected Properties loadPropertiesFromFile(ClassLoader cl, String relativePath) throws MojoExecutionException {
        InputStream inputStream = cl.getResourceAsStream(relativePath);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException | NullPointerException e) {
            throw new MojoExecutionException("Can't load properties form file " + relativePath, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                getLog().warn("Failed to close Input Stream ", e);
            }
        }

        Properties userProps = mavenSession.getUserProperties();

        Enumeration<?> e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            userProps.setProperty(key, properties.getProperty(key));
        }

        getLog().debug("Properties file " + relativePath + " loaded and properties set.");
        return userProps;
    }

    /**
     * Executes a Maven plugin using the {@link MojoExecutor}
     *
     * @param groupId - groupId of the plugin
     * @param artifactId - artifactId of the plugin
     * @param version - version of the plugin
     * @param goal - plugin goal to be executed
     * @param configuration - configuration of the plugin
     * @param dependencies - plugin dependencies
     * @throws MojoExecutionException - If there are any exceptions locating or executing the MOJO
     */
    protected void executeCheck(String groupId, String artifactId, String version, String goal, Xpp3Dom configuration,
            Dependency... dependencies) throws MojoExecutionException {
        List<Dependency> deps = new ArrayList<Dependency>();
        for (Dependency dependency : dependencies) {
            deps.add(dependency);
        }

        Plugin plugin = MojoExecutor.plugin(groupId, artifactId, version, deps);

        MojoExecutor.executeMojo(plugin, goal, configuration,
                MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager));
    }
}
