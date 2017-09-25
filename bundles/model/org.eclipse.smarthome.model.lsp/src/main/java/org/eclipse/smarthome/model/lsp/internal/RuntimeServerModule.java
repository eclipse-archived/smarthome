/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lsp.internal;

import java.util.concurrent.ExecutorService;

import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.ExecutorServiceProvider;
import org.eclipse.xtext.ide.server.DefaultProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.IProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.IWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.ProjectWorkspaceConfigFactory;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.ProjectDescriptionBasedContainerManager;

import com.google.inject.AbstractModule;

/**
 * This class configures the injector for the Language Server.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class RuntimeServerModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);

        bind(LanguageServer.class).to(LanguageServerImpl.class);
        bind(IResourceServiceProvider.Registry.class).toProvider(RegistryProvider.class);
        bind(IWorkspaceConfigFactory.class).to(ProjectWorkspaceConfigFactory.class);
        bind(IProjectDescriptionFactory.class).to(DefaultProjectDescriptionFactory.class);
        bind(IContainer.Manager.class).to(ProjectDescriptionBasedContainerManager.class);
    }

}