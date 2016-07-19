/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.core.config;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class GeneralProjectCreator implements IProjectCreator {

    @Override
    public IProject createProject(String projectName) {
        IProject defaultProject = ResourcesPlugin.getWorkspace().getRoot().getProject("config");
        if (!defaultProject.exists()) {
            initialize(defaultProject);
        }
        return defaultProject;
    }

    private void initialize(IProject project) {
        try {
            IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
            desc.setNatureIds(new String[] { "org.eclipse.xtext.ui.shared.xtextNature" });
            project.create(desc, null);
            project.open(null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

}
