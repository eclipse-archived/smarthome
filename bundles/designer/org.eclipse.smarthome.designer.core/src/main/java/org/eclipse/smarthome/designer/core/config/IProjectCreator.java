package org.eclipse.smarthome.designer.core.config;

import org.eclipse.core.resources.IProject;

public interface IProjectCreator {
	IProject createProject(String projectName);
}
