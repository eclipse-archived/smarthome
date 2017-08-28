/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lsp.internal;

import org.eclipse.smarthome.model.ide.ItemsIdeSetup;
import org.eclipse.smarthome.model.ide.SitemapIdeSetup;
import org.eclipse.smarthome.model.persistence.ide.PersistenceIdeSetup;
import org.eclipse.smarthome.model.rule.ide.RulesIdeSetup;
import org.eclipse.smarthome.model.script.ide.ScriptIdeSetup;
import org.eclipse.smarthome.model.thing.ide.ThingIdeSetup;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Provides the Xtext Registry for the Language Server.
 *
 * It just piggy-backs the static Resgitry instance that the runtime bundles are using anyway.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Singleton
public class RegistryProvider implements Provider<IResourceServiceProvider.Registry> {

    private static IResourceServiceProvider.Registry registry = createRegistry();

    @Override
    public IResourceServiceProvider.Registry get() {
        return registry;
    }

    private static Registry createRegistry() {
        IResourceServiceProvider.Registry registry = new ResourceServiceProviderRegistryImpl();
        register(registry, new ItemsIdeSetup().createInjector());
        register(registry, new PersistenceIdeSetup().createInjector());
        register(registry, new RulesIdeSetup().createInjector());
        register(registry, new ScriptIdeSetup().createInjector());
        register(registry, new SitemapIdeSetup().createInjector());
        register(registry, new ThingIdeSetup().createInjector());
        return registry;
    }

    private static void register(IResourceServiceProvider.Registry registry, Injector injector) {
        IResourceServiceProvider resourceServiceProvider = injector.getInstance(IResourceServiceProvider.class);
        FileExtensionProvider extensionProvider = injector.getInstance(FileExtensionProvider.class);
        for (String ext : extensionProvider.getFileExtensions()) {
            if (registry.getExtensionToFactoryMap().containsKey(ext)) {
                if (extensionProvider.getPrimaryFileExtension() == ext) {
                    registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
                }
            } else {
                registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
            }
        }
    }

}
