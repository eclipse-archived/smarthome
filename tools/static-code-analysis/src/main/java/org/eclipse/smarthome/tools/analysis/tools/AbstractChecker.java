/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
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

    @Component
    protected MavenProject mavenProject;

    @Component
    protected MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * The Plugin Descriptor
     */
    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    protected PluginDescriptor plugin;

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

    /**
     * Create an array that contains all necessary dependencies in order to run some the static code analysis tools.
     * It adds the static-code-analysis artifact as dependency as well, so all checks included there can be used from
     * the analysis tools.
     *
     * @param additionalArtifacts - artifacts that contain custom checks
     * @param coreDependency - a dependency to the tool itself, used to specify a certain version of the used tool
     * @return dependencies
     */
    protected Dependency[] getDependencies(Dependency[] additionalArtifacts, Dependency coreDependency) {
        List<Dependency> dependencies = new LinkedList<Dependency>();

        // First add the core dependency
        if (coreDependency != null) {
            getLog().info("Adding dependency to " + coreDependency.getArtifactId() + ":" + coreDependency.getVersion());
            dependencies.add(coreDependency);
        }

        // Add the static-code-analysis artifact as dependency to findbugs-maven-plugin, because this
        // plugin contains custom checks
        Dependency staticCode = dependency(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion());
        dependencies.add(staticCode);

        // Add additional dependencies
        if (additionalArtifacts != null) {
            for (Dependency dependency : additionalArtifacts) {
                getLog().info("Adding dependency to " + dependency.getArtifactId() + ":" + dependency.getVersion());
                dependencies.add(dependency);
            }
        }
        return dependencies.toArray(new Dependency[dependencies.size()]);
    }
}
