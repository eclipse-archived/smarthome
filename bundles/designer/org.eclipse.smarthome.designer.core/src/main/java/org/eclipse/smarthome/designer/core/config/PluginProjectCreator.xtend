/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core.config

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Collections
import java.util.Set
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.jdt.core.JavaCore
import org.eclipse.smarthome.designer.core.CoreActivator
import org.eclipse.smarthome.model.script.engine.action.ActionService

class PluginProjectCreator implements IProjectCreator {

	override createProject(String projectName) {
		val workspace = ResourcesPlugin.workspace
		val project = workspace.root.getProject(projectName)
		val javaProject = JavaCore.create(project)
		val projectDescription = if (project.exists) {
				project.description
			} else {
				workspace.newProjectDescription(projectName) => [
					location = null
					project.create(it, null)
				]
			}
		projectDescription.natureIds = #[
			JavaCore.NATURE_ID,
			"org.eclipse.pde.PluginNature",
			"org.eclipse.xtext.ui.shared.xtextNature"
		]

		val builders = newArrayList

		builders += projectDescription.newCommand => [
			builderName = JavaCore.BUILDER_ID
		]

		builders += projectDescription.newCommand => [
			builderName = "org.eclipse.pde.ManifestBuilder"
		]

		builders += projectDescription.newCommand => [
			builderName = "org.eclipse.pde.SchemaBuilder"
		]

		builders += projectDescription.newCommand => [
			builderName = "org.eclipse.xtext.ui.shared.xtextBuilder"
		]

		projectDescription.buildSpec = builders

		project.open(null)
		project.setDescription(projectDescription, null)

		val classpathEntries = newArrayList
		classpathEntries += JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER"))
		classpathEntries += JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"))

		javaProject.setRawClasspath(classpathEntries, null)

		javaProject.setOutputLocation(new Path("/" + projectName + "/bin"), null)
		createManifest(project)
		createBuildProps(project)
		setJavaVM
		project

	}

	def private setJavaVM() {
		val javaHome = System.getProperty("java.home")
		if (javaHome == null) {
			throw new RuntimeException("JAVA_HOME environment variable has to be specified!")
		}

		val jdtLaunchingPreferences = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.launching")
		val xmlValue = 
			'''
			<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			<vmSettings defaultVM="57,org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType13,1393576529277">
				<vmType id="org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType">
					<vm id="1393576529277" javadocURL="http://download.oracle.com/javase/7/docs/api/" name="JAVA_HOME_JRE" path="«javaHome»">
					</vm>
				</vmType>
			</vmSettings>
			'''
		
		jdtLaunchingPreferences.put("org.eclipse.jdt.launching.PREF_VM_XML", xmlValue)
		// forces the application to save the preferences
		jdtLaunchingPreferences.flush
	}

	def private void createManifest(IProject project) throws CoreException {
		val manifestContent = '''
			Manifest-Version: 1.0
			Bundle-ManifestVersion: 2
			Bundle-Name: Eclipse SmartHome Designer Project
			Bundle-SymbolicName: «project.name»;singleton:=true
			Bundle-Version: 1.0.0.qualifier
			Bundle-Vendor: Eclipse.org/SmartHome
			Require-Bundle: org.eclipse.xtext.xbase.lib
			Import-Package: org.osgi.service.cm,
			«FOR importPackage : allImportedPackages SEPARATOR ","»
				«" " + importPackage»
			«ENDFOR»
			Bundle-RequiredExecutionEnvironment: JavaSE-1.7
			Bundle-ActivationPolicy: lazy
		'''

		val metaInfFolder = project.getFolder("META-INF");
		if (!metaInfFolder.exists)
			metaInfFolder.create(false, true, null);
		createFile("MANIFEST.MF", metaInfFolder, manifestContent)
	}
	
	def Set<String> getAllImportedPackages() {
        val ret = newHashSet("org.eclipse.smarthome.model.script.actions",
            "org.joda.time",
            "org.eclipse.smarthome.core.library.types",
            "org.eclipse.smarthome.core.library.items",
            "org.eclipse.smarthome.core.items",
            "org.eclipse.smarthome.core.types",
            "org.eclipse.smarthome.core.persistence")
        ret.addAll(getImportedPackages())
        return ret
    }
	
	def private getImportedPackages() {
		val actionServices = CoreActivator.actionServiceTracker.services?.filter(ActionService)
		if (actionServices != null) {
		    return actionServices.map[it.actionClass.package.name].toSet
	    } else {
	        return Collections.emptySet
        }
	}
	
	def private void createBuildProps( IProject project) {
		val buildPropertiesContent = 
			'''
				bin.includes = META-INF/,\
				               .
			'''
		createFile("build.properties", project, buildPropertiesContent)
	}

	def public IFile createFile(String name, IContainer container, String content) {

		val file = container.getFile(new Path(name))
		val InputStream stream = new ByteArrayInputStream(content.getBytes(file.charset))
		if (file.exists()) {
			file.setContents(stream, true, true, null);
		}
		else {
			file.create(stream, true, null);
		}
		stream.close();

		return file

	}
	
	def private void assertExist(IContainer c) {
		if (!c.exists()) {
			if (!c.getParent().exists()) {
				assertExist(c.getParent());
			}
			if (c instanceof IFolder) {
				c.create(false, true, null)
			}

		}
	}
}
