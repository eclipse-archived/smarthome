/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.xtext.resource.SynchronizedXtextResourceSet;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Oliver Libutzki - Added reloadAllModelsOfType method
 *
 */
public class ModelRepositoryImpl implements ModelRepository {

    private final Logger logger = LoggerFactory.getLogger(ModelRepositoryImpl.class);
    private final ResourceSet resourceSet;

    private final List<ModelRepositoryChangeListener> listeners = new CopyOnWriteArrayList<>();

    public ModelRepositoryImpl() {
        XtextResourceSet xtextResourceSet = new SynchronizedXtextResourceSet();
        xtextResourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
        this.resourceSet = xtextResourceSet;
        // don't use XMI as a default
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().remove("*");
    }

    @Override
    public EObject getModel(String name) {
        synchronized (resourceSet) {
            Resource resource = getResource(name);
            if (resource != null) {
                if (resource.getContents().size() > 0) {
                    return resource.getContents().get(0);
                } else {
                    logger.warn("Configuration model '{}' is either empty or cannot be parsed correctly!", name);
                    resourceSet.getResources().remove(resource);
                    return null;
                }
            } else {
                logger.trace("Configuration model '{}' can not be found", name);
                return null;
            }
        }
    }

    @Override
    public boolean addOrRefreshModel(String name, InputStream inputStream) {
        Resource resource = getResource(name);
        if (resource == null) {
            synchronized (resourceSet) {
                // try again to retrieve the resource as it might have been created by now
                resource = getResource(name);
                if (resource == null) {
                    // seems to be a new file
                    // don't use XMI as a default
                    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().remove("*");
                    resource = resourceSet.createResource(URI.createURI(name));
                    if (resource != null) {
                        logger.info("Loading model '{}'", name);
                        try {
                            Map<String, String> options = new HashMap<String, String>();
                            options.put(XtextResource.OPTION_ENCODING, "UTF-8");
                            if (inputStream == null) {
                                logger.warn(
                                        "Resource '{}' not found. You have to pass an inputStream to create the resource.",
                                        name);
                                return false;
                            }
                            resource.load(inputStream, options);
                            notifyListeners(name, EventType.ADDED);
                            return true;
                        } catch (IOException e) {
                            logger.warn("Configuration model '" + name + "' cannot be parsed correctly!", e);
                            resourceSet.getResources().remove(resource);
                        }
                    } else {
                        logger.warn("Ignoring file '{}' as we do not have a parser for it.", name);
                    }
                }
            }
        } else {
            synchronized (resourceSet) {
                resource.unload();
                try {
                    logger.info("Refreshing model '{}'", name);
                    if (inputStream != null) {
                        resource.load(inputStream, Collections.EMPTY_MAP);
                    } else {
                        resource.load(Collections.EMPTY_MAP);
                    }
                    notifyListeners(name, EventType.MODIFIED);
                    return true;
                } catch (IOException e) {
                    logger.warn("Configuration model '" + name + "' cannot be parsed correctly!", e);
                    resourceSet.getResources().remove(resource);
                }
            }
        }
        return false;
    }

    @Override
    public boolean removeModel(String name) {
        Resource resource = getResource(name);
        if (resource != null) {
            synchronized (resourceSet) {
                // do not physically delete it, but remove it from the resource set
                notifyListeners(name, EventType.REMOVED);
                resourceSet.getResources().remove(resource);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public Iterable<String> getAllModelNamesOfType(final String modelType) {
        synchronized (resourceSet) {
            // Make a copy to avoid ConcurrentModificationException
            List<Resource> resourceListCopy = new ArrayList<Resource>(resourceSet.getResources());
            Iterable<Resource> matchingResources = Iterables.filter(resourceListCopy, new Predicate<Resource>() {
                @Override
                public boolean apply(Resource input) {
                    if (input != null && input.getURI().lastSegment().contains(".") && input.isLoaded()) {
                        return modelType.equalsIgnoreCase(input.getURI().fileExtension());
                    } else {
                        return false;
                    }
                }
            });
            return Lists.newArrayList(Iterables.transform(matchingResources, new Function<Resource, String>() {
                @Override
                public String apply(Resource from) {
                    return from.getURI().path();
                }
            }));
        }
    }

    @Override
    public void reloadAllModelsOfType(final String modelType) {
        synchronized (resourceSet) {
            // Make a copy to avoid ConcurrentModificationException
            List<Resource> resourceListCopy = new ArrayList<Resource>(resourceSet.getResources());
            for (Resource resource : resourceListCopy) {
                if (resource != null && resource.getURI().lastSegment().contains(".") && resource.isLoaded()) {
                    if (modelType.equalsIgnoreCase(resource.getURI().fileExtension())) {
                        XtextResource xtextResource = (XtextResource) resource;
                        // It's not sufficient to discard the derived state.
                        // The quick & dirts solution is to reparse the whole resource.
                        // We trigger this by dummy updating the resource.
                        logger.debug("Refreshing resource '{}'", resource.getURI().lastSegment());
                        xtextResource.update(1, 0, "");
                        notifyListeners(resource.getURI().lastSegment(), EventType.MODIFIED);
                    }
                }
            }
        }
    }

    @Override
    public void addModelRepositoryChangeListener(ModelRepositoryChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeModelRepositoryChangeListener(ModelRepositoryChangeListener listener) {
        listeners.remove(listener);
    }

    private Resource getResource(String name) {
        return resourceSet.getResource(URI.createURI(name), false);
    }

    private void notifyListeners(String name, EventType type) {
        for (ModelRepositoryChangeListener listener : listeners) {
            listener.modelChanged(name, type);
        }
    }

}
